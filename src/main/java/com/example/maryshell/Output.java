package com.example.maryshell;

import javafx.application.Platform;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.control.Label;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Output {
    private static final String PARAMETER_ERROR = "incorrect parameters";
    private static final String SHELL_START = "~> ";
    private static final String ENTER = "\r";
    private static final String BACK_SPACE = "\b";
    private static final String SPACE = " ";
    private static final String COMMAND_ERROR = "command not recognized";
    private static final String EMPTY_STRING = "";

    private final Map<String, RunModule<List<String>>> commands;
    private final Label label;
    private final ScrollPane scrollPane;

    public static String getShellStart() {
        return SHELL_START;
    }

    public Output(Label newlabel, ScrollPane newScrollPane, Map<String, RunModule<List<String>>> commands) {
        label = newlabel;
        scrollPane = newScrollPane;
        this.commands = commands;

        label.heightProperty().addListener((obs, n1, n2) -> {
            scrollToBottom();
        });
    }

    public void start(){
        print(SHELL_START);
    }

    public String getText(){
        return label.getText();
    }

    private String getCommandName(){
        String lastString = getLastStr();
        String strippedLastString = stripLeft(lastString); //строка без передних пробелов
        int lastIndex = strippedLastString.contains(SPACE) ? strippedLastString.indexOf(SPACE) : strippedLastString.length() - 1;
        return strippedLastString.substring(0, lastIndex);
    }

    // обработка введённого символа
    public void handleSceneKeyType(KeyEvent event){
        var symbol = event.getCharacter();
        if (symbol.equals(ENTER)){
            printExtra(symbol);

            String commandName = getCommandName();

            try{
                RunModule<List<String>> command = commands.get(commandName);
                command.setParam(getParameters(commandName));
                command.run();
            } catch (NullPointerException e){
                printExtra(COMMAND_ERROR + ENTER);
            } catch (Exception e) {
                printExtra(PARAMETER_ERROR + ENTER);
            }

            printExtra(SHELL_START);
        } else if (symbol.equals(BACK_SPACE)) {
            eraseSymbol();
        } else {
            String newLabel = getText() + symbol;
            print(newLabel);
        }
    }


    private String stripLeft(String text){
        if (!text.contains(SPACE)){
            return text;
        }

        String regex = "^\\s+";
        Matcher matcher = Pattern.compile(regex).matcher(text);

        int len = 0;
        while (matcher.find()){
            String match = matcher.group();
            len = Math.max(match.length(), len);
        }

        return text.substring(len);
    }

    public void print(String text){
        label.setText(text);
    }

    public void printExtra(String text){
        String outputText = getText();
        label.setText(outputText + text);
    }

    private void eraseSymbol(){
        String outputStrings = getText();
        String lastString = getFullLastStr();

        if (!Objects.equals(lastString, SHELL_START)){
            print(outputStrings.substring(0, outputStrings.length() -1));
        }
    }

    public String getLastStr(){
        String text = getText();
        String[] splitedStrings = text.split(SHELL_START);
        return splitedStrings[splitedStrings.length - 1];
    }

    public List<String> getParameters(String command) throws Exception {
        String lastString = getLastStr();

        int commandEndIndex = lastString.indexOf(command) + command.length() + 1;
        if (lastString.length() == commandEndIndex){
            return Collections.emptyList();
        }

        String string = lastString.substring(commandEndIndex);

        String correctParamRegex = "\"[^\"]+\"";
        Matcher correctParamMatcher = Pattern.compile(correctParamRegex).matcher(string);
        List<String> parameters = new ArrayList<>(Collections.emptyList());

        while (correctParamMatcher.find()){
            parameters.add(correctParamMatcher.group());
        }

        for (String parameter : parameters) {
            string = string.replaceFirst(parameter, EMPTY_STRING);
        }

        String incorrectParamRegex = "[^\"\\s]+";
        Matcher incorrectParamMatcher = Pattern.compile(incorrectParamRegex).matcher(string);
        if (incorrectParamMatcher.find()) {
            throw new Exception();
        }

        return parameters;
    }

    String getFullLastStr(){
        String text = getText();
        if (text.contains(ENTER)){
            String[] splitedStrings = text.split(ENTER);
            return splitedStrings[splitedStrings.length - 1];
        } else {
            return text;
        }
    }

    private void scrollToBottom() {
        Platform.runLater(() -> {
            scrollPane.setVvalue(scrollPane.getVmax());
            Platform.runLater(() -> scrollPane.setVvalue(scrollPane.getVmax()));
        });
    }
}
package com.example.maryshell.ui;

import com.example.maryshell.functionality.RunModule;
import com.example.maryshell.launch.Shell;
import javafx.application.Platform;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.control.Label;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Модуль вывода эмулятора.
 * <p>
 * Связывает {@link Label} (текст вывода) и {@link ScrollPane} (прокрутка)
 * с картой зарегистрированных команд. Обрабатывает ввод пользователя
 * посимвольно, разбирает аргументы команды и вызывает соответствующую
 * команду через {@link RunModule}.
 * <p>
 * Отображает приглашение {@value #SHELL_START}, сообщения об ошибках
 * ({@value #COMMAND_ERROR}, {@value #PARAMETER_ERROR}) и автоматически
 * прокручивает вывод вниз при добавлении текста.
 *
 * @author Cortofanchic
 * @version 1.0
 * @see Shell
 * @see RunModule
*/
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
    private final Shell shellModule;

    /**
     * Возвращает начальный символ командной строки {@value SHELL_START}.
     *
     * @return строка начала ввода консоли
     */
    public static String getShellStart() {
        return SHELL_START;
    }

    /**
     * Создаёт модуль вывода.
     *
     * @param newlabel поле вывода
     * @param newScrollPane прокручиваемая область
     * @param commands карта команд
     * @param shell ссылка на приложение
     */
    public Output(Label newlabel, ScrollPane newScrollPane, Map<String, RunModule<List<String>>> commands, Shell shell) {
        label = newlabel;
        scrollPane = newScrollPane;
        this.commands = commands;
        shellModule = shell;

        label.heightProperty().addListener((obs, n1, n2) -> {
            scrollToBottom();
        });
    }

    /**
     * Выводит строка начала ввода командной строки.
     */
    public void start(){
        print(SHELL_START);
    }

    /**
     * Возвращает текущее содержимое поля вывода.
     *
     * @return текст поля вывода
     */
    public String getText(){
        return label.getText();
    }

    /**
     * Возвращает имя текущей вводимой команды.
     * <p>
     * Имя — это первое слово в последней строке после приглашения.
     *
     * @return имя команды
     */
    private String getCommandName(){
        String lastString = getLastStr();
        String strippedLastString = stripLeft(lastString); //строка без передних пробелов
        int lastIndex = strippedLastString.contains(SPACE) ? strippedLastString.indexOf(SPACE) : strippedLastString.length() - 1;
        return strippedLastString.substring(0, lastIndex);
    }

    /**
     * Возвращает символ "{@value ENTER}" (пробел).
     *
     * @return строка с символом Enter
     */
    public static String getENTER() {
        return ENTER;
    }

    /**
     * Обрабатывает введённый символ.
     * <p>
     * Поведение зависит от символа:
     * <ul>
     *   <li>{@code ENTER} — выполнить команду</li>
     *   <li>{@code BACK_SPACE} — удалить последний символ</li>
     *   <li>иначе — добавить символ в поле вывода</li>
     * </ul>
     * При ошибке выполнения команды выводит сообщение
     * ({@value #COMMAND_ERROR} или {@value #PARAMETER_ERROR}).
     *
     * @param event событие нажатия клавиши
     */
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

    /**
     * Удаляет ведущие пробелы из строки.
     *
     * @param text исходная строка
     * @return строка без ведущих пробелов
     */
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

    /**
     * Устанавливает текст поля вывода.
     *
     * @param text новый текст
     */
    public void print(String text){
        label.setText(text);
    }

    /**
     * Дописывает текст в поле вывода.
     *
     * @param text добавляемый текст
     */
    public void printExtra(String text){
        String outputText = getText();
        label.setText(outputText + text);
    }

    /**
     * Удаляет последний символ из поля вывода.
     * <p>
     * Не удаляет символ, если последняя строка — приглашение.
     */
    private void eraseSymbol(){
        String outputStrings = getText();
        String lastString = getFullLastStr();

        if (!Objects.equals(lastString, SHELL_START)){
            print(outputStrings.substring(0, outputStrings.length() -1));
        }
    }

    /**
     * Возвращает последнюю строку после строки начала ввода консоли.
     *
     * @return последняя строка ввода
     */
    public String getLastStr(){
        String text = getText();
        String[] splitStrings = text.split(SHELL_START);
        return splitStrings[splitStrings.length - 1];
    }

    /**
     * Разбирает аргументы команды из текущей строки ввода.
     * <p>
     * Поддерживает аргументы в кавычках ({@code "..."}).
     * Аргументы без кавычек считаются ошибкой.
     *
     * @param command имя команды
     * @return список аргументов
     * @throws Exception если аргументы некорректны
     */
    public List<String> getParameters(String command) throws Exception {
        String lastString = getLastStr();
        int commandEndIndex = lastString.indexOf(command) + command.length() + 1;
        if (lastString.length() == commandEndIndex){
            return Collections.emptyList();
        }

        String string = lastString.substring(commandEndIndex);

        String correctParamRegex = "\"[^\"]*\"";
        Matcher correctParamMatcher = Pattern.compile(correctParamRegex).matcher(string);
        List<String> parameters = new ArrayList<>();

        while (correctParamMatcher.find()){
            String parameter = correctParamMatcher.group();
            parameters.add(parameter.substring(1, parameter.length() - 1));
        }

        for (String parameter : parameters) {
            string = string.replaceFirst(String.format("\"%s\"", Pattern.quote(parameter)), EMPTY_STRING);
        }

        String incorrectParamRegex = "[^\"\\s]+";
        Matcher incorrectParamMatcher = Pattern.compile(incorrectParamRegex).matcher(string);
        if (incorrectParamMatcher.find()) {
            throw new Exception();
        }

        return parameters;
    }

    /**
     * Возвращает последнюю строку ввода целиком
     * (со строкой начала ввода командной строки).
     *
     * @return последняя строка
     */
    String getFullLastStr(){
        String text = getText();
        if (text.contains(ENTER)){
            String[] splitedStrings = text.split(ENTER);
            return splitedStrings[splitedStrings.length - 1];
        } else {
            return text;
        }
    }

    /**
     * Прокручивает вывод вниз.
     * <p>
     * Выполняется в потоке JavaFX через {@link Platform#runLater}.
     * Двойной вызов — для учёта пересчёта layout.
     */
    private void scrollToBottom() {
        Platform.runLater(() -> {
            scrollPane.setVvalue(scrollPane.getVmax());
            Platform.runLater(() -> scrollPane.setVvalue(scrollPane.getVmax()));
        });
    }

    /**
     * Возвращает ссылку на приложение.
     *
     * @return приложение
     */
    public Shell getShellModule() {
        return shellModule;
    }
}
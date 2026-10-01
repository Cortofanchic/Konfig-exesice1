package com.example.maryshell.functionality;

import com.example.maryshell.launch.Shell;
import com.example.maryshell.ui.Output;
import javafx.application.Platform;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class Commands {
    private static Output outputModule;
    private static Shell shell;

    public static void setOutputModule(Output output) {
        outputModule = output;
        shell = outputModule.getShellModule();
    }

    public static Map<String, RunModule<List<String>>> getCommands(){
        Map<String, RunModule<List<String>>> commands = new HashMap<>(); // имя метода - метод

        List<Consumer<List<String>>> commandsPointers = List.of(
                Commands::cd,
                Commands::ls,
                Commands::exit,
                Commands::confDump
        );

        List<String> names = List.of(
                "cd",
                "ls",
                "exit",
                "conf-dump"
        );

        for (int pointer = 0; pointer < commandsPointers.size(); pointer++){
            RunModule<List<String>> command = new RunModule<>(commandsPointers.get(pointer));
            command.setName(names.get(pointer));
            commands.put(command.toString() , command);
        }

        return commands;
    }

    public static void ls(List<String> parameters){
        outputModule.printExtra(String.format("this is ls function, parameters - %s\n", parameters.toString()));
    }

    public static void cd(List<String> parameters){
        outputModule.printExtra(String.format("this is cd function, parameters - %s\n", parameters.toString()));
    }

    public static void exit(List<String> parameters){
        if (parameters.isEmpty()){
            Platform.exit();
            System.exit(0);
        } else {
            outputModule.printExtra("Error: exit command don't need arguments" + Output.getENTER());
        }
    }

    public static void confDump(List<String> parameters){
        if (parameters.isEmpty()){
            try {
                outputModule.printExtra(String.format("vfs.root=%s, vfs.script=%s", shell.getVfsPath(), shell.getStartScriptPath()) + Output.getENTER());
            } catch (Exception e){
                System.out.println(e.getMessage());
            }
        } else {
            outputModule.printExtra("Error: conf-dump command don't need arguments" + Output.getENTER());
        }
    }
}
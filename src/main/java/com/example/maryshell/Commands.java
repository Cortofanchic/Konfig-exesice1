package com.example.maryshell;

import javafx.application.Platform;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class Commands {
    private static Output outputModule;

    public static void setOutputModule(Output outputModule){
        Commands.outputModule = outputModule;
    }

    public static Map<String, RunModule<List<String>>> getCommands(){
        Map<String, RunModule<List<String>>> commands = new HashMap<>(); // имя метода - метод

        List<Consumer<List<String>>> commandsPointers = List.of(
                Commands::cd,
                Commands::ls,
                Commands::exit
        );

        List<String> names = List.of(
                "cd",
                "ls",
                "exit"
        );

        for (int pointer = 0; pointer < commandsPointers.size(); pointer++){
            RunModule<List<String>> command = new RunModule<>(commandsPointers.get(pointer));
            command.setName(names.get(pointer));
            commands.put(command.toString() , command);
        }

        return commands;
    }

    public static void ls(List<String> parameters){
        outputModule.printExtra(String.format("this is ls function, parameters - [%s]\n", parameters.toString()));
    }

    public static void cd(List<String> parameters){
        outputModule.printExtra(String.format("this is cd function, parameters - [%s]\n", parameters.toString()));
    }

    public static void exit(List<String> parameters){
        Platform.exit();
        System.exit(0);
    }
}
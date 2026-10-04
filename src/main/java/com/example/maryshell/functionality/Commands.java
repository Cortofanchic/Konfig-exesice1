package com.example.maryshell.functionality;

import com.example.maryshell.launch.Shell;
import com.example.maryshell.ui.Output;
import com.example.maryshell.vfs.VfsNode;
import javafx.application.Platform;

import java.util.*;
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
        if (!parameters.isEmpty()){
            outputModule.printExtra("Error: ls command don't need arguments.");
        } else {
            try {
                VfsNode vfs = shell.getVfs();
                outputModule.printExtra(vfs.getComponent(vfs.getCurrentJsonNode(), 0));
            } catch (Exception e) {
                outputModule.printExtra("Error: can't read dir.");
            }
        }
        outputModule.printExtra(Output.getENTER());
    }

    public static void cd(List<String> parameters){
        if (parameters.isEmpty()) {
            outputModule.printExtra("Error: don't have needed parameters.");
        } else if (parameters.size() == 1){
            VfsNode vfs = shell.getVfs();
            String parameter = parameters.get(0);
            try {
                if (Objects.equals(parameter, "..")){
                    vfs.goParentDir();
                } else {
                    List<String> path = Arrays.stream(parameters.get(0).split("/")).toList();
                    vfs.goToPath(path);
                }
                String currentPath = String.join("/", vfs.getCurrentDir());
                outputModule.printExtra(String.format("Current path: %s", currentPath));
            } catch (Exception e) {
                outputModule.printExtra("Error: can't find path.");
            }
        } else {
            outputModule.printExtra("Error: extra don't needed parameters.");
        }
        outputModule.printExtra(Output.getENTER());
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

    private static void cal(List<String> parameters){

    }

    private static void du(List<String> parameters){

    }

    private static void find(List<String> parameters){

    }
}
package com.example.maryshell.functionality;

import com.example.maryshell.launch.Shell;
import com.example.maryshell.ui.Output;
import com.example.maryshell.vfs.OwnershipRegistry;
import com.example.maryshell.vfs.VfsNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Consumer;

public class Commands {
    private static Output outputModule;
    private static Shell shell;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static JsonNode root;
    private static OwnershipRegistry ownership;

    public static void setVfs(InputStream in) throws IOException {
        Commands.root = MAPPER.readTree(in);
        Commands.ownership = new OwnershipRegistry(root);
    }

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
                Commands::confDump,
                Commands::cal,
                Commands::du,
                Commands::find,
                Commands::chown
        );

        List<String> names = List.of(
                "cd",
                "ls",
                "exit",
                "conf-dump",
                "cal",
                "du",
                "find",
                "chown"
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
                    JsonNode parentNode = vfs.goParentDir(vfs.getCurrentJsonNode());
                    List<String> findPath = vfs.findPath(parentNode);
                    findPath.remove(0);
                    vfs.setCurrentDir(findPath);
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
            outputModule.printExtra("Error: cd command doesn't need parameters.");
        }
        outputModule.printExtra(Output.getENTER());
    }

    public static void exit(List<String> parameters){
        if (parameters.isEmpty()){
            Platform.exit();
            System.exit(0);
        } else {
            outputModule.printExtra("Error: exit command doesn't need arguments" + Output.getENTER());
        }
    }

    public static void confDump(List<String> parameters){
        if (parameters.isEmpty()){
            try {
                outputModule.printExtra(String.format("vfs.root=%s, vfs.script=%s", shell.getVfsPath(), shell.getStartScriptPath()) + Output.getENTER());
            } catch (Exception e){
                outputModule.printExtra(String.format("Error: can't read vfs dir." + Output.getENTER()));
            }
        } else {
            outputModule.printExtra("Error: conf-dump command doesn't need arguments" + Output.getENTER());
        }
    }

    private static void cal(List<String> parameters){
        if (parameters.size() != 1){
            outputModule.printExtra("Error: incorrect parameters.");
        } else {
            try {
                List<String> args = Arrays.stream(parameters.get(0).split(" ")).toList();
                Calendar cal = new Calendar(args);
                outputModule.printExtra(cal.printCal());
            } catch (Exception e) {
                outputModule.printExtra("Error: incorrect parameters.");
            }
        }
        outputModule.printExtra(Output.getENTER());
    }

    private static void du(List<String> parameters){
        VfsNode vfs = shell.getVfs();
        try {
            int bytes;
            if (parameters.size() < 2) {
                if (parameters.isEmpty()) {
                    JsonNode currentNode = vfs.getCurrentJsonNode();
                    bytes = vfs.findDiskUsage(currentNode);
                } else {
                    List<String> startDir = vfs.getCurrentDir();
                    List<String> path = Arrays.stream(parameters.get(0).split("/")).toList();
                    bytes = vfs.findDiskUsage(vfs.goToPath(path));
                    vfs.goToPath(startDir);
                }
                outputModule.printExtra(String.format("Disk usage of current dir: %d bytes.", bytes));
            } else {
                outputModule.printExtra("Error: two many parameters.");
            }
        } catch (Exception e){
            outputModule.printExtra("Error: can't read vfs.");
        }
        outputModule.printExtra(Output.getENTER());
    }

    private static void find(List<String> parameters){
        if (parameters.size() == 1) {
            List<String> args = Arrays.stream(parameters.get(0).split(" ")).toList();
            try {
                VfsNode vfs = shell.getVfs();
                if (args.size() == 1) {
                    List<String> path = new ArrayList<>();
                    vfs.findFile(args.get(0), vfs.getCurrentJsonNode(), path);
                    if (path.isEmpty()){
                        outputModule.printExtra("Don't find file.");
                    } else {
                        outputModule.printExtra(String.format("Found file: %s.", String.join("/", path)));
                    }
                } else {
                    outputModule.printExtra("Error: extra arguments.");
                }
            } catch (Exception e){
                outputModule.printExtra("Error: can't read vfs.");
            }
        } else {
            outputModule.printExtra("Error: missing argument.");
        }
        outputModule.printExtra(Output.getENTER());
    }

    private static void chown(List<String> parameters) {
        if (parameters.size() == 2) {
            try {
                VfsNode vfs = shell.getVfs();
                OwnershipRegistry ownershipRegistry = shell.getRegistry();
                String newOwner = parameters.get(0);
                List<String> path = Arrays.stream(parameters.get(1).split("/")).toList();
                List<String> curDir = vfs.getCurrentDir();
                JsonNode jsonNode;
                try {
                    jsonNode = vfs.goToPath(path);
                } catch (IOException e) {
                    outputModule.printExtra("Error: incorrect path.");
                    outputModule.printExtra(Output.getENTER());
                    return;
                }
                vfs.goToPath(curDir);
                if (newOwner.contains(":")){
                    String newUser = newOwner.split(":")[0];
                    String newGroup = newOwner.split(":")[1];
                    ownershipRegistry.set(jsonNode, newUser, newGroup);
                    outputModule.printExtra(String.format("Set %s to new user %s and group %s.", parameters.get(1), newUser, newGroup));
                } else {
                    ownershipRegistry.setUser(jsonNode, newOwner);
                    outputModule.printExtra(String.format("Set %s to new user %s.", parameters.get(1), newOwner));
                }
            } catch (Exception e){
                outputModule.printExtra("Error: can't read vfs.");
            }
        } else if (parameters.size() == 3 && Objects.equals(parameters.get(0), "-R")) {
            try {
                chownRecursive(parameters);
            } catch (Exception e){
                outputModule.printExtra("Error: can't read vfs.");
            }
        }else {
            outputModule.printExtra("Error: for using command write - chown \"owner[:group]\" \"path\"");
        }
        outputModule.printExtra(Output.getENTER());
    }

    private static void chownRecursive(List<String> parameters) throws IOException {
        VfsNode vfs = shell.getVfs();
        OwnershipRegistry ownershipRegistry = shell.getRegistry();
        String newOwner = parameters.get(1);
        List<String> path = Arrays.stream(parameters.get(2).split("/")).toList();
        List<String> curDir = vfs.getCurrentDir();
        JsonNode jsonNode;
        try {
            jsonNode = vfs.goToPath(path);
        } catch (IOException e) {
            outputModule.printExtra("Error: incorrect path.");
            outputModule.printExtra(Output.getENTER());
            return;
        }
        vfs.goToPath(curDir);
        if (newOwner.contains(":")){
            String newUser = newOwner.split(":")[0];
            String newGroup = newOwner.split(":")[1];
            ownershipRegistry.setRecursive(jsonNode, newUser, newGroup);
            outputModule.printExtra(String.format("Set recursive %s to new user %s and group %s.", parameters.get(2), newUser, newGroup));
        } else {
            ownershipRegistry.setRecursive(jsonNode, newOwner, ownershipRegistry.getGroup(jsonNode));
            outputModule.printExtra(String.format("Set recursive %s to new user %s.", parameters.get(2), newOwner));
        }
    }
}
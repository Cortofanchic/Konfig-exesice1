package com.example.maryshell.build;

import com.example.maryshell.launch.Shell;
import javafx.application.Application;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;


public class Test {
    private static List<String> readResource(String path) throws Exception {
        File file = new File(path);

        if (file.exists()) {
            try{
                return Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new Exception(String.format("Error: can't read test script in  path \"%s\"", path));
            }
        } else {
            throw new Exception(String.format("Error: can't open test script in  path \"%s\"", path));
        }
    }

    public static void main(String[] args) {
        List<String> shellArgs = new ArrayList<>();
        List<String> testDirArgs = new ArrayList<>();

        String JOIN_ARGS_SEP = "\n";
        String TEST_COMMANDS_TEG = "TEST=";
        String VFS_TEG = "VFS=";
        String SCRIPT_TAG = "SCRIPT=";
        String ENTER = "\r";

        for (String arg: args){
            if (arg.startsWith(VFS_TEG) || arg.startsWith(SCRIPT_TAG)){
                shellArgs.add(arg);
            } else {
                testDirArgs.add(arg);
            }
            System.out.println(arg);
        }

        if (!testDirArgs.isEmpty()){
            List<String> allTestCommands = new ArrayList<>();
            for (String testDir: testDirArgs){
                try {
                    List<String> resourceLines = readResource(testDir);
                    allTestCommands.add(String.join(JOIN_ARGS_SEP, resourceLines));
                } catch (Exception exception){
                    shellArgs.add(exception.getMessage() + ENTER);
                }
            }
            String resultTestCommands = String.join(ENTER, allTestCommands);
            shellArgs.add(TEST_COMMANDS_TEG + resultTestCommands);
        } else {
            String defaultTestDirPath = "src/main/resources/com/example/maryshell/tests";
            File dir = new File(defaultTestDirPath);
            String[] testFiles = dir.list();

            List<String> commandLinesList = new ArrayList<>();

            if (testFiles == null) {
                Application.launch(Shell.class);
                return;
            }

            for (String testFile : testFiles) {
                try {
                    commandLinesList.add(String.join(JOIN_ARGS_SEP, readResource(String.format("%s/%s", defaultTestDirPath, testFile))));
                } catch (Exception exception){
                    shellArgs.add(exception.getMessage() + ENTER);
                }
            }

            String resultTestCommands = String.join(ENTER, commandLinesList);
            shellArgs.add(TEST_COMMANDS_TEG + resultTestCommands);
        }

        Application.launch(Shell.class, shellArgs.toArray(new String[0]));
    }
}
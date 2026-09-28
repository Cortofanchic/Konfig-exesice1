package com.example.maryshell.tests;

import com.example.maryshell.Shell;
import javafx.application.Application;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Test {
    private static List<String> readResource(String path) {
        InputStream in = Test.class.getResourceAsStream(path);
        if (in == null) {
            throw new IllegalArgumentException("Ресурс не найден: " + path);
        }
        List<String> lines = new ArrayList<>();
        try (Scanner scanner = new Scanner(in, StandardCharsets.UTF_8)) {
            while (scanner.hasNextLine()) {
                lines.add(scanner.nextLine());
            }
        }
        return lines;

    }

    public static void main(String[] args) {
        String testDirPath = "src/main/resources/com/example/maryshell/tests";
        File dir = new File(testDirPath);
        String[] testFiles = dir.list();

        List<String> commandLinesList = new ArrayList<>();

        if (testFiles == null){
            Application.launch(Shell.class);
            return;
        }

        for (String testFile: testFiles){
            List<String> commandLines = readResource(testFile);
            commandLinesList.addAll(commandLines);
        }
        Application.launch(Shell.class, commandLinesList.toArray(new String[0]));
    }
}
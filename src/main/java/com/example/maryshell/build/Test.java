package com.example.maryshell.build;

import com.example.maryshell.launch.Shell;
import javafx.application.Application;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;


/**
 * Точка входа эмулятора оболочки UNIX в тестовом режиме.
 * <p>
 * Отбирает из аргументов командной строки параметры {@value #VFS_TAG},
 * {@value #SCRIPT_TAG} и путь тестового скрипта. Содержимое тестовых скриптов
 * склеивается в один аргумент {@code TEST=...} и передаётся в {@link Shell}
 * вместе с другими аргументами коммандной строки.
 * {@link Application#launch(Class, String...)}.
 *
 * @author Cortofanchic
 * @version 1.0
 * @see Shell
 */
public class Test {
    private static final String JOIN_ARGS_SEP = "\n";
    private static final String TEST_COMMANDS_TEG = "TEST=";
    private static final String VFS_TAG = "VFS=";
    private static final String SCRIPT_TAG = "SCRIPT=";
    private static final String ENTER = "\r";

    /**
     * Читает файл по переданному пути.
     * <p>
     * Пробует прочитать файл по введённому пути,
     * в случае успеха считывания возвращает содержимое ввиде построчного спика.
     *
     * @param path путь до читаемого файла
     * @return список со строками файла
     * @throws Exception если файл не найден или не читается
     */
    private static List<String> readResource(String path) throws Exception {
        File file = new File(path);

        if (file.exists()) {
            try{
                return Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new Exception(String.format("Error: can't read test script in path \"%s\"", path));
            }
        } else {
            throw new Exception(String.format("Error: can't open test script in path \"%s\"", path));
        }
    }

    /**
     * Запускает оболочку UNIX в тестовом режиме.
     * <p>
     * Проходит по {@code args}, оставляет только три: те, что начинаются
     * с {@value #VFS_TAG} и {@value #SCRIPT_TAG} и {@code параметр тестового скрипта}, и передаёт их в
     * {@link Shell}.
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        List<String> shellArgs = new ArrayList<>();
        List<String> testDirArgs = new ArrayList<>();

        for (String arg: args){
            if (arg.startsWith(VFS_TAG) || arg.startsWith(SCRIPT_TAG)){
                shellArgs.add(arg);
            } else {
                testDirArgs.add(arg);
            }
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
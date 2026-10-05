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

/**
 * Реестр и реализация команд эмулятора оболочки UNIX.
 * <p>
 * Содержит статические методы-команды ({@code ls}, {@code cd}, {@code cat},
 * {@code find}, {@code du}, {@code chown}, {@code cal}, {@code conf-dump},
 * {@code exit}), а также фабрику {@link #getCommands()} для регистрации
 * команд в {@link Output}.
 * <p>
 * Все команды работают с виртуальной файловой системой (VFS),
 * загруженной в память. Исходный JSON-файл VFS не модифицируется —
 * изменения (например, смена владельца в {@code chown}) хранятся
 * только в {@link OwnershipRegistry}.
 *
 * @author Cortofanchic
 * @version 1.0
 * @see Shell
 * @see Output
 * @see VfsNode
 * @see OwnershipRegistry
 */
public class Commands {
    private static Output outputModule;
    private static Shell shell;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static JsonNode root;
    private static OwnershipRegistry ownership;

    /**
     * Загружает VFS из JSON-потока и создаёт реестр владельцев.
     * <p>
     * Вызывается один раз при старте приложения.
     *
     * @param in поток с JSON-содержимым VFS
     * @throws IOException если JSON не читается или имеет неверный формат
     */
    public static void setVfs(InputStream in) throws IOException {
        Commands.root = MAPPER.readTree(in);
        Commands.ownership = new OwnershipRegistry(root);
    }

    /**
     * Устанавливает модуль вывода и получает ссылку на приложение.
     *
     * @param output модуль вывода
     */
    public static void setOutputModule(Output output) {
        outputModule = output;
        shell = outputModule.getShellModule();
    }

    /**
     * Создаёт карту зарегистрированных команд.
     * <p>
     * Ключ — имя команды, значение — {@link RunModule} с ссылкой
     * на соответствующий метод {@code Commands}.
     *
     * @return карта команд
     */
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

    /**
     * Выводит список файлов и папок в текущей директории VFS.
     * <p>
     * Если VFS недоступен — выводит
     * сообщение об ошибке.
     *
     * @param parameters аргументы команды (должны быть пусты)
     */
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

    /**
     * Переходит в указанную директорию VFS.
     * <p>
     * Поддерживает:
     * <ul>
     *   <li>{@code cd ".."} — подняться на уровень вверх</li>
     *   <li>{@code cd "path"} — перейти по относительному или абсолютному пути</li>
     * </ul>
     *
     * @param parameters аргументы команды (один путь)
     */
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

    /**
     * Завершает работу эмулятора.
     * <p>
     * Вызывает {@link Platform#exit()} и
     * {@link System#exit(int)}.
     *
     * @param parameters аргументы команды (должны быть пусты)
     */
    public static void exit(List<String> parameters){
        if (parameters.isEmpty()){
            Platform.exit();
            System.exit(0);
        } else {
            outputModule.printExtra("Error: exit command doesn't need arguments" + Output.getENTER());
        }
    }

    /**
     * Выводит параметры эмулятора в формате {@code ключ=значение}.
     * <p>
     * Служебная команда. Выводит путь к VFS и путь к стартовому скрипту.
     *
     * @param parameters аргументы команды (должны быть пусты)
     */
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

    /**
     * Выводит календарь на текущий месяц или год.
     * <p>
     * Принимает один аргумент — строку с параметрами календаря
     * (год, месяц, день, относительная дата).
     *
     * @param parameters аргументы команды (одна строка)
     * @see Calendar
     */
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

    /**
     * Выводит размер директории VFS в байтах.
     * <p>
     * Без аргументов — размер текущей директории.
     * С аргументом — размер указанной директории.
     *
     * @param parameters аргументы команды (опционально путь)
     */
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

    /**
     * Рекурсивно ищет файлы по имени в VFS.
     * <p>
     * Принимает один аргумент — маску имени ({@code *}, {@code ?}).
     *
     * @param parameters аргументы команды (маска)
     */
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

    /**
     * Меняет владельца файла или директории VFS.
     * <p>
     * Поддерживает форматы:
     * <ul>
     *   <li>{@code chown "user" "path"} — сменить владельца</li>
     *   <li>{@code chown "user:group" "path"} — сменить владельца и группу</li>
     *   <li>{@code chown "-R" "user" "path"} — рекурсивно</li>
     * </ul>
     * Изменения хранятся только в {@link OwnershipRegistry} — исходный
     * JSON-файл VFS не модифицируется.
     *
     * @param parameters аргументы команды
     */
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

    /**
     * Рекурсивно меняет владельца директории и всех вложенных узлов.
     * <p>
     * Вспомогательный метод для {@code chown "-R"}.
     *
     * @param parameters аргументы: {@code "-R" "owner[:group]" "path"}
     * @throws IOException если путь не найден в VFS
     */
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
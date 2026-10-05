package com.example.maryshell.vfs;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static java.util.Collections.reverse;

/**
 * Узел виртуальной файловой системы (VFS).
 * <p>
 * Загружает JSON-файл VFS в память и предоставляет операции
 * навигации ({@link #goToPath(List)}, {@link #goParentDir(JsonNode)}),
 * просмотра ({@link #getComponent(JsonNode, int)}, {@link #readMotd()})
 * и поиска ({@link #findFile(String, JsonNode, List)},
 * {@link #findDiskUsage(JsonNode)}).
 * <p>
 * VFS хранится <b>только в памяти</b> — исходный JSON-файл не
 * модифицируется. Текущая директория хранится как список сегментов
 * пути относительно корня.
 * <p>
 * Корень VFS обозначается как {@value #PATH_START} (виртуальный
 * диск {@code C:}).
 *
 * @author Cortofanchic
 * @version 1.0
 * @see JsonTree
 * @see OwnershipRegistry
 */
public class VfsNode {
    private final String rootPath;
    private List<String> currentDir;
    private final JsonTree jsonTree;
    private JsonNode cachedRoot;

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String ENTER = "\n";
    private static final String PATH_START ="C:";

    /**
     * Создаёт узел VFS и загружает JSON-файл.
     *
     * @param rootPath путь к JSON-файлу VFS
     * @throws IOException если файл не найден или не читается
     */
    public VfsNode(String rootPath) throws IOException {
        this.rootPath = rootPath;
        this.currentDir = new ArrayList<>();
        jsonTree = new JsonTree(getCurrentJsonNode());
    }

    /**
     * Возвращает полный текущий путь, включая {@value #PATH_START}.
     *
     * @return список сегментов: {@code ["C:", ...]}
     */
    public List<String> getCurrentDir() {
        List<String> fullCurrentDir = new ArrayList<>();
        fullCurrentDir.add(PATH_START);
        fullCurrentDir.addAll(currentDir);
        return fullCurrentDir;
    }

    /**
     * Возвращает JSON-узел текущей директории.
     *
     * @return узел VFS
     * @throws IOException если путь некорректен
     */
    public JsonNode getCurrentJsonNode() throws IOException {
        List<String> fullCurrentDir = new ArrayList<>(List.of(PATH_START));
        fullCurrentDir.addAll(currentDir);
        return goToPath(fullCurrentDir);
    }

    /**
     * Возвращает строковое представление узла и его потомков
     * в виде дерева с отступами.
     *
     * @param el узел VFS
     * @param level уровень вложенности (для отступов)
     * @return строка с деревом
     */
    public String getComponent(JsonNode el, int level){
        String SPACES = "..";
        String sep = SPACES.repeat(Math.max(0, level));

        if (el.has("children")){
            StringBuilder components = new StringBuilder();
            components.append(sep)
                    .append("/")
                    .append(el.get("name").textValue())
                    .append(ENTER);

            el = el.get("children");
            for (JsonNode child: el){
                components.append(getComponent(child, level + 1)).append("\n");
            }
            components.delete(components.length() - 1, components.length());
            return components.toString();
        } else {
            return sep + el.get("name").textValue();
        }
    }

    /**
     * Переходит по указанному пути.
     * <p>
     * Если путь начинается с {@value #PATH_START} — считается
     * абсолютным. Иначе — относительным от текущей директории.
     * <p>
     * При успешном переходе обновляет {@link #currentDir}.
     *
     * @param path список сегментов пути
     * @return JSON-узел конечной директории
     * @throws IOException если путь некорректен или не существует
     */
    public JsonNode goToPath(List<String> path) throws IOException {
        List<String> newPath;
        JsonNode dirPath = readJson();

        if (path.contains("") && path.size() > 1) {
            throw new IOException("Error: incorrect path.");
        }

        if (!path.isEmpty() && Objects.equals(path.get(0), PATH_START)){
            newPath = new ArrayList<>(path);
            newPath.remove(0);
        } else {
            newPath = new ArrayList<>(currentDir);

            if (Objects.equals(path.get(0), "")){
                newPath.addAll(path.subList(1, path.size()));
            } else {
                newPath.addAll(path);
            }
        }

        List<String> fullDirPath = new ArrayList<>(newPath);
        while (!newPath.isEmpty()) {
            dirPath = goToChild(dirPath, newPath.get(0));
            newPath.remove(0);
        }

        currentDir = new ArrayList<>(fullDirPath);
        return dirPath;
    }

    /**
     * Переходит к дочернему узлу по имени.
     *
     * @param json родительский узел
     * @param path имя дочернего узла
     * @return дочерний узел
     * @throws IOException если узел не найден
     */
    private JsonNode goToChild(JsonNode json, String path) throws IOException {
        if (!Objects.equals(path, "")){
            JsonNode children = json.get("children");
            if (children != null && children.isArray()) {
                for (JsonNode child : children) {
                    if (path.equals(child.get("name").textValue())) {
                        return child;
                    }
                }
            }
            throw new IOException("Error: path doesn't exist.");
        }
        return json;
    }

    /**
     * Возвращает родителя узла.
     *
     * @param jsonNode узел VFS
     * @return родительский узел
     */
    public JsonNode goParentDir(JsonNode jsonNode) {
        return jsonTree.getParent(jsonNode);
    }

    /**
     * Возвращает путь к JSON-файлу VFS.
     *
     * @return путь к VFS
     */
    public String getRootPath() {
        return rootPath;
    }

    /**
     * Читает JSON-файл VFS (с кэшированием).
     * <p>
     * При первом вызове читает файл, при последующих —
     * возвращает кэшированное дерево.
     *
     * @return корень JSON-дерева
     * @throws IOException если файл не читается
     */
    public JsonNode readJson() throws IOException {
        if (cachedRoot == null) {
            try (InputStream in = Files.newInputStream(Paths.get(rootPath))) {
                cachedRoot = MAPPER.readTree(in);
            } catch (Exception e) {
                throw new IOException("Error: can't open root path.", e);
            }
        }
        return cachedRoot;
    }

    /**
     * Читает содержимое файла {@code motd} из корня VFS.
     *
     * @return содержимое {@code motd} или {@code "no motd file"},
     * если файл отсутствует
     * @throws IOException если VFS не читается
     */
    public String readMotd() throws IOException {
        JsonNode json = readJson();
        for (JsonNode child : json.path("children")) {
            if ("motd".equals(child.path("name").asText())) {
                return child.get("content").asText();
            }
        }
        return "no motd file";
    }

    /**
     * Рекурсивно подсчитывает размер директории в байтах.
     *
     * @param node узел VFS
     * @return суммарный размер файлов поддерева
     */
    public int findDiskUsage(JsonNode node){
        if (node.has("children")){
            int endUsage = 0;
            for (JsonNode child: node.get("children")){
                endUsage += findDiskUsage(child);
            }
            return endUsage;
        } else {
            return node.get("content").textValue().length();
        }
    }

    /**
     * Рекурсивно ищет файлы по маске.
     * <p>
     * Маска поддерживает {@code *} и {@code ?}.
     *
     * @param pattern маска имени
     * @param json узел для поиска
     * @param acc накопитель найденных путей
     */
    public void findFile(String pattern, JsonNode json, List<String> acc) {
        String name = json.path("name").textValue();

        if (matches(name, pattern)) {
            acc.add(String.join("/", findPath(json)));
        }

        if (json.has("children")) {
            for (JsonNode child : json.get("children")) {
                findFile(pattern, child, acc);
            }
        }
    }

    /**
     * Возвращает путь от корня до узла.
     *
     * @param jsonNode узел VFS
     * @return список сегментов пути от корня
     */
    public List<String> findPath(JsonNode jsonNode) {
        List<String> path = new ArrayList<>();

        while (jsonNode.has("name") && !Objects.equals(jsonNode.get("name").textValue(), "/")){
            path.add(jsonNode.get("name").textValue());
            jsonNode = goParentDir(jsonNode);
        }

        path.add(PATH_START);
        reverse(path);
        return path;
    }

    /**
     * Проверяет соответствие имени маске.
     * <p>
     * Поддерживает {@code *} (любое число символов) и
     * {@code ?} (один символ).
     *
     * @param name    имя файла
     * @param pattern маска
     * @return {@code true}, если имя соответствует маске
     */
    private static boolean matches(String name, String pattern) {
        String regex = pattern
                .replace(".", "\\.")
                .replace("*", ".*")
                .replace("?", ".");
        return name.matches(regex);
    }

    /**
     * Устанавливает текущую директорию.
     *
     * @param currentDir список сегментов пути (без {@value #PATH_START})
     */
    public void setCurrentDir(List<String> currentDir) {
        this.currentDir = currentDir;
    }
}

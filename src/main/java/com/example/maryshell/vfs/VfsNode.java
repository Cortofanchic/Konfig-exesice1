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

public class VfsNode {
    private final String rootPath;
    private List<String> currentDir;

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String ENTER = "\n";
    private static final String PATH_START ="C:";

    public VfsNode(String rootPath) throws IOException {
        this.rootPath = rootPath;
        this.currentDir = List.of();
    }

    public List<String> getCurrentDir() {
        List<String> fullCurrentDir = new ArrayList<>();
        fullCurrentDir.add(PATH_START);
        fullCurrentDir.addAll(currentDir);
        return fullCurrentDir;
    }

    public JsonNode getCurrentJsonNode() throws IOException {
        List<String> fullCurrentDir = new ArrayList<>(List.of(PATH_START));
        fullCurrentDir.addAll(currentDir);
        return goToPath(fullCurrentDir);
    }

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

    public JsonNode goParentDir() throws IOException {
        if (currentDir.isEmpty()) {
            return goToPath(getCurrentDir());
        }

        List<String> parentPath = new ArrayList<>(getCurrentDir());
        parentPath.remove(parentPath.size() - 1);

        JsonNode result = goToPath(parentPath);
        parentPath.remove(0);
        currentDir = parentPath;
        return result;
    }

    public String getRootPath() {
        return rootPath;
    }

    public JsonNode readJson() throws IOException {
        try (InputStream in = Files.newInputStream(Paths.get(rootPath))){
            return MAPPER.readTree(in);
        } catch (Exception e){
            throw new IOException("Error: can't open root path.");
        }
    }

    public String readMotd() throws IOException {
        JsonNode json = readJson();
        for (JsonNode child : json.path("children")) {
            if ("motd".equals(child.path("name").asText())) {
                return child.get("content").asText();
            }
        }
        return "no motd file";
    }
}

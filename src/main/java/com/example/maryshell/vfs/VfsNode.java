package com.example.maryshell.vfs;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class VfsNode {
    private String rootPath;
    private InputStream rootInStream;

    public VfsNode(String rootPath, InputStream in){
        this.rootPath = rootPath;
        this.rootInStream = in;
    }

    public String getRootPath() {
        return rootPath;
    }

    private JsonNode readJson() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(rootInStream);
    }

    public String readMotd() throws IOException {
        JsonNode json = readJson();
        for (JsonNode child : json.path("children")) {
            if ("motd".equals(child.path("name").asText())) {
                return child.get("content").asText();
            }
        }
        return "no content";
    }
}

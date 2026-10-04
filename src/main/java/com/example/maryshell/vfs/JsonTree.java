package com.example.maryshell.vfs;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.IdentityHashMap;
import java.util.Map;

public class JsonTree {
    private final JsonNode root;
    private final Map<JsonNode, JsonNode> parents = new IdentityHashMap<>();

    public JsonTree(JsonNode root) {
        this.root = root;
        index(root, root);
    }

    private void index(JsonNode node, JsonNode parent) {
        parents.put(node, parent);
        if (node.has("children")) {
            for (JsonNode child : node.get("children")) {
                index(child, node);
            }
        }
    }

    public JsonNode getParent(JsonNode node) {
        return parents.get(node);
    }

    public JsonNode getRoot() {
        return root;
    }
}

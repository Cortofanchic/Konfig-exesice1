package com.example.maryshell.vfs;


import com.fasterxml.jackson.databind.JsonNode;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;


public class OwnershipRegistry {

    private final Map<JsonNode, Owner> owners;
    private final String defaultUser;
    private final String defaultGroup;

    public OwnershipRegistry(JsonNode root) {
        this(root, "root", null);
    }

    public OwnershipRegistry(JsonNode root, String defaultUser, String defaultGroup) {
        this.owners = new IdentityHashMap<>();
        this.defaultUser = defaultUser != null ? defaultUser : "root";
        this.defaultGroup = defaultGroup;

        index(root);
    }


    private void index(JsonNode node) {
        owners.put(node, new Owner(defaultUser, defaultGroup));

        JsonNode children = node.get("children");
        if (children != null && children.isArray()) {
            for (JsonNode child : children) {
                index(child);
            }
        }
    }

    public Owner get(JsonNode node) {
        if (node == null) return new Owner(defaultUser, defaultGroup);
        return owners.getOrDefault(node, new Owner(defaultUser, defaultGroup));
    }

    public String getUser(JsonNode node) {
        return get(node).user();
    }

    public String getGroup(JsonNode node) {
        return get(node).group();
    }

    public boolean has(JsonNode node) {
        return owners.containsKey(node);
    }


    public void set(JsonNode node, String user, String group) {
        if (node == null) return;
        owners.put(node, new Owner(user, group));
    }

    public void setUser(JsonNode node, String user) {
        Owner old = get(node);
        owners.put(node, old.withUser(user));
    }

    public void setGroup(JsonNode node, String group) {
        Owner old = get(node);
        owners.put(node, old.withGroup(group));
    }

    public int setRecursive(JsonNode node, String user, String group) {
        if (node == null) return 0;

        owners.put(node, new Owner(user, group));
        int count = 1;

        JsonNode children = node.get("children");
        if (children != null && children.isArray()) {
            for (JsonNode child : children) {
                count += setRecursive(child, user, group);
            }
        }
        return count;
    }

    public void remove(JsonNode node) {
        owners.remove(node);
    }

    public void clear() {
        owners.clear();
    }

    public int size() {
        return owners.size();
    }

    public Map<JsonNode, Owner> asMap() {
        return Collections.unmodifiableMap(owners);
    }
}

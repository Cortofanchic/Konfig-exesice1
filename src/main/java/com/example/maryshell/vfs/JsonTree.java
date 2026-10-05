package com.example.maryshell.vfs;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Дерево JSON с индексом родителей.
 * <p>
 * Строит карту «узел → родитель» для всех узлов дерева
 * за один обход. Использует {@link IdentityHashMap} для сравнения
 * узлов <b>по ссылке</b> — это важно, поскольку {@link JsonNode#equals}
 * в Jackson сравнивает <b>содержимое</b>, и два разных узла
 * с одинаковым JSON считались бы «равными».
 * <p>
 * Позволяет получить родителя любого узла за O(1) — то, чего
 * нет в стандартном API Jackson.
 *
 * @author Cortofanchic
 * @version 1.0
 * @see JsonNode
 */
public class JsonTree {
    private final JsonNode root;
    private final Map<JsonNode, JsonNode> parents = new IdentityHashMap<>();

    /**
     * Создаёт дерево и строит индекс родителей.
     *
     * @param root корень JSON-дерева
     */
    public JsonTree(JsonNode root) {
        this.root = root;
        index(root, root);
    }

    /**
     * Рекурсивно индексирует узел и всех его потомков.
     * <p>
     * Для корня родителем устанавливается он сам — так
     * {@link #getParent(JsonNode)} для корня вернёт корень,
     * а не {@code null}.
     *
     * @param node   текущий узел
     * @param parent родитель текущего узла
     */
    private void index(JsonNode node, JsonNode parent) {
        parents.put(node, parent);
        if (node.has("children")) {
            for (JsonNode child : node.get("children")) {
                index(child, node);
            }
        }
    }

    /**
     * Возвращает родителя узла.
     * <p>
     * Для корня возвращает сам корень. Если узел не найден
     * в индексе — возвращает {@code null}.
     *
     * @param node узел
     * @return родитель узла или {@code null}
     */
    public JsonNode getParent(JsonNode node) {
        return parents.get(node);
    }

    /**
     * Возвращает корень дерева.
     *
     * @return корень дерева
     */
    public JsonNode getRoot() {
        return root;
    }
}

package com.example.maryshell.vfs;


import com.fasterxml.jackson.databind.JsonNode;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Реестр владельцев узлов виртуальной файловой системы.
 * <p>
 * Хранит отображение «узел - {@link Owner}». Строится один раз
 * при загрузке VFS, обходя дерево. В JSON-файл ничего не добавляется —
 * владельцы живут только в памяти.
 * <p>
 * Ключ — {@link JsonNode}, сравнение выполняется <b>по ссылке</b>
 * через {@link IdentityHashMap}. Это важно, поскольку
 * {@link JsonNode#equals} в Jackson сравнивает <b>содержимое</b>,
 * и два разных узла с одинаковым JSON считались бы «равными».
 * <p>
 * Все операции с изменением владельца ({@link #set}, {@link #setUser},
 * {@link #setGroup}, {@link #setRecursive}) работают только в памяти
 * и не влияют на исходный JSON.
 *
 * @author Cortofanchic
 * @version 1.0
 * @see Owner
 * @see JsonNode
 */
public class OwnershipRegistry {

    private final Map<JsonNode, Owner> owners;
    private final String defaultUser;
    private final String defaultGroup;

    /**
     * Создаёт реестр с владельцем по умолчанию {@code root} без группы.
     *
     * @param root корень JSON-дерева VFS
     */
    public OwnershipRegistry(JsonNode root) {
        this(root, "root", null);
    }

    /**
     * Создаёт реестр с указанными значениями по умолчанию.
     * <p>
     * Если {@code defaultUser} равен {@code null} —
     * используется {@code "root"}.
     *
     * @param root корень JSON-дерева VFS
     * @param defaultUser имя пользователя по умолчанию
     * @param defaultGroup имя группы по умолчанию (может быть {@code null})
     */
    public OwnershipRegistry(JsonNode root, String defaultUser, String defaultGroup) {
        this.owners = new IdentityHashMap<>();
        this.defaultUser = defaultUser != null ? defaultUser : "root";
        this.defaultGroup = defaultGroup;

        index(root);
    }

    /**
     * Рекурсивно индексирует узел и всех его потомков,
     * устанавливая им владельца по умолчанию.
     *
     * @param node текущий узел
     */
    private void index(JsonNode node) {
        owners.put(node, new Owner(defaultUser, defaultGroup));

        JsonNode children = node.get("children");
        if (children != null && children.isArray()) {
            for (JsonNode child : children) {
                index(child);
            }
        }
    }

    /**
     * Возвращает владельца узла.
     * <p>
     * Если узел не зарегистрирован или равен {@code null} —
     * возвращает владельца по умолчанию.
     *
     * @param node узел VFS
     * @return владелец узла
     */
    public Owner get(JsonNode node) {
        if (node == null) return new Owner(defaultUser, defaultGroup);
        return owners.getOrDefault(node, new Owner(defaultUser, defaultGroup));
    }

    /**
     * Возвращает имя пользователя-владельца узла.
     *
     * @param node узел VFS
     * @return имя пользователя
     */
    public String getUser(JsonNode node) {
        return get(node).user();
    }

    /**
     * Возвращает имя группы-владельца узла.
     *
     * @param node узел VFS
     * @return имя группы или {@code null}
     */
    public String getGroup(JsonNode node) {
        return get(node).group();
    }

    /**
     * Проверяет, зарегистрирован ли узел в реестре.
     *
     * @param node узел VFS
     * @return {@code true}, если узел есть в реестре
     */
    public boolean has(JsonNode node) {
        return owners.containsKey(node);
    }

    /**
     * Устанавливает владельца узла.
     * <p>
     * Если {@code node == null} — ничего не делает.
     *
     * @param node  узел VFS
     * @param user  имя пользователя
     * @param group имя группы (может быть {@code null})
     */
    public void set(JsonNode node, String user, String group) {
        if (node == null) return;
        owners.put(node, new Owner(user, group));
    }

    /**
     * Меняет только имя пользователя-владельца узла,
     * сохраняя текущую группу.
     *
     * @param node узел VFS
     * @param user новое имя пользователя
     */
    public void setUser(JsonNode node, String user) {
        Owner old = get(node);
        owners.put(node, old.withUser(user));
    }

    /**
     * Меняет только имя группы-владельца узла,
     * сохраняя текущего пользователя.
     *
     * @param node  узел VFS
     * @param group новое имя группы
     */
    public void setGroup(JsonNode node, String group) {
        Owner old = get(node);
        owners.put(node, old.withGroup(group));
    }

    /**
     * Рекурсивно меняет владельца узла и всех его потомков.
     *
     * @param node  корень поддерева
     * @param user  имя пользователя
     * @param group имя группы (может быть {@code null})
     * @return количество изменённых узлов
     */
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

    /**
     * Удаляет узел из реестра.
     *
     * @param node узел VFS
     */
    public void remove(JsonNode node) {
        owners.remove(node);
    }

    /**
     * Очищает реестр.
     */
    public void clear() {
        owners.clear();
    }

    /**
     * Возвращает количество узлов в реестре.
     *
     * @return количество узлов
     */
    public int size() {
        return owners.size();
    }

    /**
     * Возвращает неизменяемое представление карты владельцев.
     * <p>
     * Только для чтения и отладки.
     *
     * @return карта «узел - владелец»
     */
    public Map<JsonNode, Owner> asMap() {
        return Collections.unmodifiableMap(owners);
    }
}

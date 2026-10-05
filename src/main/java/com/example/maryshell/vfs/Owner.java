package com.example.maryshell.vfs;


/**
 * Владелец узла виртуальной файловой системы.
 * <p>
 * Хранит имя пользователя и имя группы.
 * Иммутабельная запись — все изменения возвращают новый экземпляр
 * через {@link #withUser(String)} и {@link #withGroup(String)}.
 * <p>
 * Если {@code user} равен {@code null} или пустой строке —
 * автоматически заменяется на {@code "root"}.
 *
 * @param user  имя пользователя (не {@code null}, не пустое)
 * @param group имя группы (может быть {@code null})
 * @author Cortofanchic
 * @version 1.0
 */
public record Owner(String user, String group) {
    /**
     * Конструктор — нормализует {@code user}.
     * <p>
     * Если {@code user} равен {@code null} или пуст —
     * заменяется на {@code "root"}.
     */
    public Owner {
        if (user == null || user.isEmpty()) {
            user = "root";
        }
    }

    /**
     * Возвращает копию с другим именем пользователя.
     *
     * @param user новое имя пользователя
     * @return новый владелец
     */
    public Owner withUser(String user) {
        return new Owner(user, this.group);
    }

    /**
     * Возвращает копию с другой группой.
     *
     * @param group новое имя группы (может быть {@code null})
     * @return новый владелец
     */
    public Owner withGroup(String group) {
        return new Owner(this.user, group);
    }

    /**
     * Возвращает строковое представление владельца.
     * <p>
     * Формат:
     * <ul>
     *   <li>{@code user} — если группа не задана</li>
     *   <li>{@code user:group} — если группа задана</li>
     * </ul>
     *
     * @return строка вида {@code user} или {@code user:group}
     */
    @Override
    public String toString() {
        return group == null ? user : user + ":" + group;
    }
}

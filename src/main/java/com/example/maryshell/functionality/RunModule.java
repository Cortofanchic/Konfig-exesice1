package com.example.maryshell.functionality;

import java.util.function.Consumer;

/**
 * Обёртка команды эмулятора.
 * <p>
 * Хранит действие ({@link Consumer}), параметр и имя команды.
 * Используется в {@link Commands#getCommands()} как элемент карты команд.
 * <p>
 * Реализует {@link Runnable} — вызов {@link #run()} применяет
 * {@code action} к сохранённому {@code param}.
 *
 * @param <T> тип параметра команды (обычно {@code List<String>})
 * @author Cortofanchic
 * @version 1.0
 * @see Commands
 */
public class RunModule<T> implements Runnable {
    private final Consumer<T> action;
    private T param;
    private String name;

    /**
     * Создаёт модуль с указанным действием.
     *
     * @param action действие команды
     */
    public RunModule(Consumer<T> action) {
        this.action = action;
    }

    /**
     * Устанавливает параметр команды.
     *
     * @param param параметр (например, список аргументов)
     */
    public void setParam(T param) {
        this.param = param;
    }

    /**
     * Устанавливает имя команды.
     *
     * @param name имя команды
     */
    public void setName(String name){
        this.name = name;
    }

    /**
     * Возвращает имя команды.
     *
     * @return имя команды или {@code null}, если не задано
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Выполняет команду: применяет {@code action} к {@code param}.
     * <p>
     * Если {@code param} не был установлен через {@link #setParam(Object)},
     * в действие уйдёт {@code null}.
     */
    @Override
    public void run() {
        action.accept(param);
    }
}
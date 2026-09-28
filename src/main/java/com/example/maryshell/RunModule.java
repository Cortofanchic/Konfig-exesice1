package com.example.maryshell;

import java.util.function.Consumer;

public class RunModule<T> implements Runnable {
    private final Consumer<T> action;
    private T param;
    private String name;

    public RunModule(Consumer<T> action) {
        this.action = action;
    }

    public void setParam(T param) {
        this.param = param;
    }

    public void setName(String name){
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public void run() {
        action.accept(param);
    }
}
package com.example.maryshell.build;

import com.example.maryshell.launch.Shell;
import javafx.application.Application;

import java.util.ArrayList;
import java.util.List;

/**
 * Точка входа эмулятора оболочки UNIX.
 * <p>
 * Отбирает из аргументов командной строки параметры {@value #VFS_TAG} и
 * {@value #SCRIPT_TAG} и передаёт их в {@link Shell} через
 * {@link Application#launch(Class, String...)}.
 *
 * @author Cortofanchic
 * @version 1.0
 * @see Shell
 */
public class Build {
    private static final String VFS_TAG = "VFS=";
    private static final String SCRIPT_TAG = "SCRIPT=";

    /**
     * Запускает оболочку UNIX.
     * <p>
     * Проходит по {@code args}, оставляет только те, что начинаются
     * с {@value #VFS_TAG} или {@value #SCRIPT_TAG}, и передаёт их в
     * {@link Shell}.
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        List<String> shellArgs = new ArrayList<>();

        for (String arg: args){
            if (arg.startsWith(VFS_TAG) || arg.startsWith(SCRIPT_TAG)){
                shellArgs.add(arg);
            }
        }

        Application.launch(Shell.class, shellArgs.toArray(new String[0]));
    }
}

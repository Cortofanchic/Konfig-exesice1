package com.example.maryshell.build;

import com.example.maryshell.launch.Shell;
import javafx.application.Application;

import java.util.ArrayList;
import java.util.List;

public class Build {
    public static void main(String[] args) {
        List<String> shellArgs = new ArrayList<>();

        String VFS_TEG = "VFS=";
        String SCRIPT_TAG = "SCRIPT=";

        for (String arg: args){
            if (arg.startsWith(VFS_TEG) || arg.startsWith(SCRIPT_TAG)){
                shellArgs.add(arg);
            }
        }

        Application.launch(Shell.class, shellArgs.toArray(new String[0]));
    }
}

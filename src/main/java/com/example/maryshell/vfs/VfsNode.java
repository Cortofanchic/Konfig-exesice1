package com.example.maryshell.vfs;

import java.io.InputStream;

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
}

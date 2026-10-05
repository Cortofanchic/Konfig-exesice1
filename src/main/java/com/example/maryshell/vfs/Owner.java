package com.example.maryshell.vfs;


public record Owner(String user, String group) {

    public static final Owner DEFAULT = new Owner("root", null);

    public Owner {
        if (user == null || user.isEmpty()) {
            user = "root";
        }
    }

    public Owner withUser(String user) {
        return new Owner(user, this.group);
    }

    public Owner withGroup(String group) {
        return new Owner(this.user, group);
    }

    @Override
    public String toString() {
        return group == null ? user : user + ":" + group;
    }
}

package com.example.banking;

public class purposespin {
    private String name;
    private  String userId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPurposeId() {
        return userId;
    }

    public void setPurposeId(String userId) {
        this.userId = userId;
    }
    @Override
    public String toString() {
        return name;
    }
}

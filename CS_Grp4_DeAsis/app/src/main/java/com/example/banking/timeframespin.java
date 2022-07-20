package com.example.banking;

public class timeframespin {
    private String name;
    private  String userId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTimeframeId() {
        return userId;
    }

    public void setTimeframeId(String userId) {
        this.userId = userId;
    }
    @Override
    public String toString() {
        return name;
    }
}

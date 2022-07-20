package com.example.banking;

public class billerspin {
    private String name;
    private  String userId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBillerId() {
        return userId;
    }

    public void setBillerId(String userId) {
        this.userId = userId;
    }
    @Override
    public String toString() {
        return name;
    }
}

package com.seckin.stockmanager.model;

public enum CustomerRole {
    CUSTOMER("CUSTOMER"),
    ADMIN("ADMIN");

    private final String name;

    private CustomerRole(String s) {
        this.name = s;
    }

    public boolean equalsName(String otherName) {
        return name.equals(otherName);
    }

    public String toString() {
        return this.name;
    }
}

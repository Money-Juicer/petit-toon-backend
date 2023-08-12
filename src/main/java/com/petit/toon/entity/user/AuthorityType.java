package com.petit.toon.entity.user;

public enum AuthorityType {
    USER("ROLE_USER"),
    ADMIN("ROLE_ADMIN");

    public String roleName;

    AuthorityType(String roleName) {
        this.roleName = roleName;
    }
}

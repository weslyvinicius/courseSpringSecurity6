package com.academy.springsecurity6full.service;

public class Resource {

    private String owner;

    private String content;

    public Resource(String owner, String content) {
        this.owner = owner;
        this.content = content;
    }

    public String getOwner() {
        return owner;
    }

    public String getContent() {
        return content;
    }
}

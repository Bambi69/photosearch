package com.gyd.photosearch.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class User extends GenericEntity implements Serializable {

    private String userName;
    private String password;
    private String role;
    private List<String> authorizedFaces = new ArrayList<>();

    public User() {
    }

    public User(String userName, String password, String role, List<String> authorizedFaces) {
        this.userName = userName;
        this.password = password;
        this.role = role;
        this.authorizedFaces = authorizedFaces;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<String> getAuthorizedFaces() {
        return authorizedFaces;
    }

    public void setAuthorizedFaces(List<String> authorizedFaces) {
        this.authorizedFaces = authorizedFaces;
    }
}

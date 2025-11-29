package com.exp.common.auth.dto;

import java.util.List;

/**
 * 登录成功后返回给前端的数据结构，需与前端的 LoginResult 类型保持一致。
 */
public class LoginResult {

    private String token;

    private String userId;

    private String username;

    private List<String> roles;

    private List<String> permissions;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }
}



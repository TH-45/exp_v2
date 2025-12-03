package jh.exp.common.auth.dto;

import java.util.List;

/**
 * 内部登录校验返回的用户信息，不包含 token，
 * 用于网关或其他服务在拿到用户信息后自行签发 JWT。
 */
public class LoginUserInfo {

    private String userId;

    private String username;

    private List<String> roles;

    private List<String> permissions;

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













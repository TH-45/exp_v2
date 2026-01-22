package jh.exp.common.core.auth.dto;

import lombok.Data;
import lombok.Getter;

import java.util.List;

/**
 * 获取当前登录用户信息（profile）时使用的数据结构，
 * 对应 docs 和前端约定的 ProfileResult。
 */
public class ProfileResult {

    private String userId;

    private String username;

    private String deptId;

    private String deptName;

    private List<String> roles;

    private List<String> permissions;

    private List<String> menus;

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

    public String getDeptId() {
        return deptId;
    }

    public void setDeptId(String deptId) {
        this.deptId = deptId;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
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

    public List<String> getMenus() {
        return menus;
    }

    public void setMenus(List<String> menus) {
        this.menus = menus;
    }
}



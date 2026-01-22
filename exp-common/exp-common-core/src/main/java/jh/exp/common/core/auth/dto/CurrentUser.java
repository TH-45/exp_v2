package jh.exp.common.core.auth.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 当前登录用户在各业务服务中的统一上下文模型。
 * <p>
 * 说明：
 * - 数据来源通常为网关在完成认证后注入的请求头（X-User-*）；
 * - 仅作为“已解析后的用户快照”在单次请求内使用，不做持久化；
 * - 可配合 {@code CurrentUserHolder} 使用，避免在业务代码中层层传递 userId 等字段。
 */
@Data
@NoArgsConstructor
public class CurrentUser implements Serializable {

    /**
     * 用户唯一标识，一般为账号 ID 或人员 ID。
     */
    private Long userId;

    /**
     * 用户展示名，例如真实姓名或账号名。
     */
    private String username;

    /**
     * 所属部门/组织 ID。
     */
    private String deptId;

    /**
     * 所属部门/组织名称。
     */
    private String deptName;

    /**
     * 角色编码列表，例如：ADMIN、BID_MANAGER。
     */
    private List<String> roles;

    /**
     * 权限编码列表，例如：system:user:view、bidding:project:edit。
     */
    private List<String> permissions;

    /**
     * 数据权限范围描述（可选），例如：SELF、DEPT、DEPT_AND_CHILDREN、ALL。
     * 也可以扩展为更复杂的结构，这里先用字符串保存。
     */
    private String dataScope;

    public CurrentUser(Long userId, Set<String> permissions) {
        this.userId = userId;
        this.permissions = permissions == null ? Collections.emptyList() : List.copyOf(permissions);
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
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
        return roles == null ? Collections.emptyList() : roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public List<String> getPermissions() {
        return permissions == null ? Collections.emptyList() : permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    public String getDataScope() {
        return dataScope;
    }

    public void setDataScope(String dataScope) {
        this.dataScope = dataScope;
    }
}



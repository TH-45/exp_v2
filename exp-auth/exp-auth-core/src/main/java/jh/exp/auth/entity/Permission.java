package jh.exp.auth.entity;

import jakarta.persistence.*;

/**
 * 权限资源表，对应 docs 中的：
 * 权限资源表 exp_permission
 */
@Entity
@Table(name = "exp_permission")
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "perm_id")
    private Long permId;

    @Column(name = "perm_code", nullable = false, unique = true, length = 128)
    private String permCode;

    @Column(name = "perm_name", nullable = false, length = 100)
    private String permName;

    @Column(name = "perm_type", length = 32)
    private String permType;

    @Column(name = "module_code", length = 64)
    private String moduleCode;

    @Column(name = "menu_group", length = 128)
    private String menuGroup;

    @Column(name = "action_code", length = 32)
    private String actionCode;

    @Column(name = "url_path", length = 256)
    private String urlPath;

    @Column(name = "status", length = 32)
    private String status;

    @Column(name = "sort_no")
    private Integer sortNo;

    @Column(name = "remark", length = 500)
    private String remark;

    public Long getPermId() {
        return permId;
    }

    public void setPermId(Long permId) {
        this.permId = permId;
    }

    public String getPermCode() {
        return permCode;
    }

    public void setPermCode(String permCode) {
        this.permCode = permCode;
    }

    public String getPermName() {
        return permName;
    }

    public void setPermName(String permName) {
        this.permName = permName;
    }

    public String getPermType() {
        return permType;
    }

    public void setPermType(String permType) {
        this.permType = permType;
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
    }

    public String getMenuGroup() {
        return menuGroup;
    }

    public void setMenuGroup(String menuGroup) {
        this.menuGroup = menuGroup;
    }

    public String getActionCode() {
        return actionCode;
    }

    public void setActionCode(String actionCode) {
        this.actionCode = actionCode;
    }

    public String getUrlPath() {
        return urlPath;
    }

    public void setUrlPath(String urlPath) {
        this.urlPath = urlPath;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getSortNo() {
        return sortNo;
    }

    public void setSortNo(Integer sortNo) {
        this.sortNo = sortNo;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}


package jh.exp.auth.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * 岗位定义表，对应 docs 中的：
 * 岗位定义表 exp_post
 */
@Entity
@Table(name = "exp_post")
public class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    @Column(name = "post_code", nullable = false, unique = true, length = 64)
    private String postCode;

    @Column(name = "post_name", nullable = false, length = 100)
    private String postName;

    @Column(name = "post_type", length = 32)
    private String postType;

    @Column(name = "post_level", length = 32)
    private String postLevel;

    @Column(name = "post_category", length = 32)
    private String postCategory;

    @Column(name = "post_desc", columnDefinition = "TEXT")
    private String postDesc;

    @Column(name = "status", length = 32)
    private String status;

    @Column(name = "default_role_id")
    private Long defaultRoleId;

    @Column(name = "default_data_scope", length = 64)
    private String defaultDataScope;

    @Column(name = "is_system")
    private Integer isSystem;

    @Column(name = "sort_no")
    private Integer sortNo;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    @Column(name = "remark", length = 500)
    private String remark;

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    public String getPostName() {
        return postName;
    }

    public void setPostName(String postName) {
        this.postName = postName;
    }

    public String getPostType() {
        return postType;
    }

    public void setPostType(String postType) {
        this.postType = postType;
    }

    public String getPostLevel() {
        return postLevel;
    }

    public void setPostLevel(String postLevel) {
        this.postLevel = postLevel;
    }

    public String getPostCategory() {
        return postCategory;
    }

    public void setPostCategory(String postCategory) {
        this.postCategory = postCategory;
    }

    public String getPostDesc() {
        return postDesc;
    }

    public void setPostDesc(String postDesc) {
        this.postDesc = postDesc;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getDefaultRoleId() {
        return defaultRoleId;
    }

    public void setDefaultRoleId(Long defaultRoleId) {
        this.defaultRoleId = defaultRoleId;
    }

    public String getDefaultDataScope() {
        return defaultDataScope;
    }

    public void setDefaultDataScope(String defaultDataScope) {
        this.defaultDataScope = defaultDataScope;
    }

    public Integer getIsSystem() {
        return isSystem;
    }

    public void setIsSystem(Integer isSystem) {
        this.isSystem = isSystem;
    }

    public Integer getSortNo() {
        return sortNo;
    }

    public void setSortNo(Integer sortNo) {
        this.sortNo = sortNo;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}


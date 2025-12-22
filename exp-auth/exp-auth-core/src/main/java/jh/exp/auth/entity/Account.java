package jh.exp.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * 账号信息表，对应 docs 中的：
 * 账号信息表 exp_account
 */
@Entity
@Table(name = "exp_account")
@TableName("exp_account")
public class Account {



    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.ASSIGN_ID)
    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "account_name", nullable = false, unique = true, length = 64)
    private String accountName;

    @Column(name = "account_display", length = 100)
    private String accountDisplay;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "password_salt", length = 64)
    private String passwordSalt;

    @Column(name = "mobile", length = 32)
    private String mobile;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "person_id")
    private Long personId;

    @Column(name = "org_id")
    private Long orgId;

    @Column(name = "post_id")
    private Long postId;

    @Column(name = "status", length = 32)
    private String status;

    @Column(name = "login_fail_count")
    private Integer loginFailCount;

    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime;

    @Column(name = "last_login_ip", length = 64)
    private String lastLoginIp;

    @Column(name = "pwd_last_change_time")
    private LocalDateTime pwdLastChangeTime;

    @Column(name = "need_change_pwd")
    private Boolean needChangePwd;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    @Column(name = "remark", length = 500)
    private String remark;

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAccountDisplay() {
        return accountDisplay;
    }

    public void setAccountDisplay(String accountDisplay) {
        this.accountDisplay = accountDisplay;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPasswordSalt() {
        return passwordSalt;
    }

    public void setPasswordSalt(String passwordSalt) {
        this.passwordSalt = passwordSalt;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getPersonId() {
        return personId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    public Long getOrgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getLoginFailCount() {
        return loginFailCount;
    }

    public void setLoginFailCount(Integer loginFailCount) {
        this.loginFailCount = loginFailCount;
    }

    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public String getLastLoginIp() {
        return lastLoginIp;
    }

    public void setLastLoginIp(String lastLoginIp) {
        this.lastLoginIp = lastLoginIp;
    }

    public LocalDateTime getPwdLastChangeTime() {
        return pwdLastChangeTime;
    }

    public void setPwdLastChangeTime(LocalDateTime pwdLastChangeTime) {
        this.pwdLastChangeTime = pwdLastChangeTime;
    }

    public Boolean getNeedChangePwd() {
        return needChangePwd;
    }

    public void setNeedChangePwd(Boolean needChangePwd) {
        this.needChangePwd = needChangePwd;
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



package jh.exp.common.core.auth.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 个人信息聚合结果，供个人信息页面展示使用。
 */
public class ProfileDetailResult {

    private String userId;

    private String username;

    private PersonInfo personInfo;

    private AccountInfo accountInfo;

    private OrgInfo orgInfo;

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

    public PersonInfo getPersonInfo() {
        return personInfo;
    }

    public void setPersonInfo(PersonInfo personInfo) {
        this.personInfo = personInfo;
    }

    public AccountInfo getAccountInfo() {
        return accountInfo;
    }

    public void setAccountInfo(AccountInfo accountInfo) {
        this.accountInfo = accountInfo;
    }

    public OrgInfo getOrgInfo() {
        return orgInfo;
    }

    public void setOrgInfo(OrgInfo orgInfo) {
        this.orgInfo = orgInfo;
    }

    public static class PersonInfo {
        private Long personId;
        private String personCode;
        private String personName;
        private String gender;
        private String mobile;
        private String email;
        private String status;
        private LocalDate entryDate;
        private String jobTitle;

        public Long getPersonId() {
            return personId;
        }

        public void setPersonId(Long personId) {
            this.personId = personId;
        }

        public String getPersonCode() {
            return personCode;
        }

        public void setPersonCode(String personCode) {
            this.personCode = personCode;
        }

        public String getPersonName() {
            return personName;
        }

        public void setPersonName(String personName) {
            this.personName = personName;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
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

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public LocalDate getEntryDate() {
            return entryDate;
        }

        public void setEntryDate(LocalDate entryDate) {
            this.entryDate = entryDate;
        }

        public String getJobTitle() {
            return jobTitle;
        }

        public void setJobTitle(String jobTitle) {
            this.jobTitle = jobTitle;
        }
    }

    public static class AccountInfo {
        private Long accountId;
        private String accountName;
        private String accountDisplay;
        private String status;
        private LocalDateTime lastLoginTime;
        private Boolean needChangePwd;

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

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public LocalDateTime getLastLoginTime() {
            return lastLoginTime;
        }

        public void setLastLoginTime(LocalDateTime lastLoginTime) {
            this.lastLoginTime = lastLoginTime;
        }

        public Boolean getNeedChangePwd() {
            return needChangePwd;
        }

        public void setNeedChangePwd(Boolean needChangePwd) {
            this.needChangePwd = needChangePwd;
        }
    }

    public static class OrgInfo {
        private Long orgId;
        private String orgCode;
        private String orgName;
        private String orgType;
        private String managerName;
        private String contactPhone;
        private Long parentOrgId;
        private String parentOrgName;

        public Long getOrgId() {
            return orgId;
        }

        public void setOrgId(Long orgId) {
            this.orgId = orgId;
        }

        public String getOrgCode() {
            return orgCode;
        }

        public void setOrgCode(String orgCode) {
            this.orgCode = orgCode;
        }

        public String getOrgName() {
            return orgName;
        }

        public void setOrgName(String orgName) {
            this.orgName = orgName;
        }

        public String getOrgType() {
            return orgType;
        }

        public void setOrgType(String orgType) {
            this.orgType = orgType;
        }

        public String getManagerName() {
            return managerName;
        }

        public void setManagerName(String managerName) {
            this.managerName = managerName;
        }

        public String getContactPhone() {
            return contactPhone;
        }

        public void setContactPhone(String contactPhone) {
            this.contactPhone = contactPhone;
        }

        public Long getParentOrgId() {
            return parentOrgId;
        }

        public void setParentOrgId(Long parentOrgId) {
            this.parentOrgId = parentOrgId;
        }

        public String getParentOrgName() {
            return parentOrgName;
        }

        public void setParentOrgName(String parentOrgName) {
            this.parentOrgName = parentOrgName;
        }
    }
}

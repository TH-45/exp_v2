package jh.exp.auth.service.service;

import jh.exp.auth.core.entity.Account;
import jh.exp.auth.core.entity.res.AccountDetailRes;
import jh.exp.auth.core.entity.res.OrgUnitDetailRes;
import jh.exp.auth.core.entity.res.PersonDetailRes;
import jh.exp.auth.core.mapper.AccountMapper;
import jh.exp.auth.core.mapper.OrgUnitMapper;
import jh.exp.auth.core.mapper.PersonMapper;
import jh.exp.common.core.auth.dto.ProfileDetailResult;
import jh.exp.common.core.auth.dto.ProfileResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 提供内部 profile 查询能力，供网关等内部服务调用。
 */
@Service
public class ProfileService {

    @Autowired
    private AccountMapper accountMapper;
    @Autowired
    private PersonMapper personMapper;
    @Autowired
    private OrgUnitMapper orgUnitMapper;

    public ProfileResult getProfile(String userId) {
        Account account = resolveAccount(userId);
        return getResult(account);
    }

    public ProfileDetailResult getProfileDetail(String userId) {
        Account account = resolveAccount(userId);
        AccountDetailRes accountDetail = accountMapper.selectAccountDetailById(account.getAccountId());

        ProfileDetailResult result = new ProfileDetailResult();
        result.setUserId(String.valueOf(account.getAccountId()));
        result.setUsername(account.getAccountDisplay() != null ? account.getAccountDisplay() : account.getAccountName());

        ProfileDetailResult.AccountInfo accountInfo = new ProfileDetailResult.AccountInfo();
        accountInfo.setAccountId(account.getAccountId());
        accountInfo.setAccountName(account.getAccountName());
        accountInfo.setAccountDisplay(account.getAccountDisplay());
        accountInfo.setStatus(account.getStatus());
        accountInfo.setLastLoginTime(account.getLastLoginTime());
        accountInfo.setNeedChangePwd(account.getNeedChangePwd());
        result.setAccountInfo(accountInfo);

        if (accountDetail != null && accountDetail.getPersonId() != null) {
            PersonDetailRes personDetail = personMapper.selectPersonDetailById(accountDetail.getPersonId());
            if (personDetail != null) {
                ProfileDetailResult.PersonInfo personInfo = new ProfileDetailResult.PersonInfo();
                personInfo.setPersonId(personDetail.getPersonId());
                personInfo.setPersonCode(personDetail.getPersonCode());
                personInfo.setPersonName(personDetail.getPersonName());
                personInfo.setGender(personDetail.getGender());
                personInfo.setMobile(personDetail.getMobile());
                personInfo.setEmail(personDetail.getEmail());
                personInfo.setStatus(personDetail.getStatus());
                personInfo.setEntryDate(personDetail.getEntryDate());
                personInfo.setJobTitle(personDetail.getJobTitle());
                result.setPersonInfo(personInfo);
            }
        }

        if (accountDetail != null && accountDetail.getOrgId() != null) {
            OrgUnitDetailRes orgDetail = orgUnitMapper.selectOrgUnitDetailById(accountDetail.getOrgId());
            if (orgDetail != null) {
                ProfileDetailResult.OrgInfo orgInfo = new ProfileDetailResult.OrgInfo();
                orgInfo.setOrgId(orgDetail.getOrgId());
                orgInfo.setOrgCode(orgDetail.getOrgCode());
                orgInfo.setOrgName(orgDetail.getOrgName());
                orgInfo.setOrgType(orgDetail.getOrgType());
                orgInfo.setManagerName(orgDetail.getManagerName());
                orgInfo.setContactPhone(orgDetail.getContactPhone());
                orgInfo.setParentOrgId(orgDetail.getParentOrgId());

                if (orgDetail.getParentOrgId() != null) {
                    OrgUnitDetailRes parent = orgUnitMapper.selectOrgUnitDetailById(orgDetail.getParentOrgId());
                    orgInfo.setParentOrgName(parent == null ? null : parent.getOrgName());
                }
                result.setOrgInfo(orgInfo);
            }
        }
        return result;
    }

    private Account resolveAccount(String userId) {
        if (!StringUtils.hasText(userId)) {
            throw new IllegalArgumentException("userId 不能为空");
        }

        Long accountId;
        try {
            accountId = Long.parseLong(userId);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("userId 格式不正确");
        }

        Account account = accountMapper.selectById(accountId);
        if (account == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        if (!"ENABLED".equalsIgnoreCase(account.getStatus())) {
            throw new IllegalStateException("账号已被禁用或锁定");
        }
        return account;
    }

    private static ProfileResult getResult(Account account) {
        ProfileResult result = new ProfileResult();
        result.setUserId(String.valueOf(account.getAccountId()));
        result.setUsername(account.getAccountDisplay() != null ? account.getAccountDisplay() : account.getAccountName());
        result.setDeptId(account.getOrgId() == null ? null : String.valueOf(account.getOrgId()));
        result.setDeptName(null);
        result.setRoles(List.of("ADMIN"));
        result.setPermissions(List.of("system:user:view", "system:user:edit"));
        result.setMenus(List.of("dashboard", "system:user", "bidding:project", "contracts:list"));
        return result;
    }
}


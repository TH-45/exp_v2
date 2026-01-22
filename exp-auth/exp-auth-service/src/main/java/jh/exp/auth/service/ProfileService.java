package jh.exp.auth.service;

import jh.exp.auth.core.mapper.AccountMapper;

import jh.exp.auth.entity.Account;
import jh.exp.common.auth.dto.ProfileResult;
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

    public ProfileResult getProfile(String userId) {
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

        return getResult(account);
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


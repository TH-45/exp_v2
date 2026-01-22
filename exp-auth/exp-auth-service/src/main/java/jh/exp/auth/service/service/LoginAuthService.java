package jh.exp.auth.service.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jh.exp.auth.core.entity.Account;
import jh.exp.auth.core.mapper.AccountMapper;

import jh.exp.common.core.auth.dto.LoginRequest;
import jh.exp.common.core.auth.dto.LoginUserInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;

/**
 * 提供账号密码校验的核心逻辑，由网关或其他服务通过内部接口调用。
 */
@Service
public class LoginAuthService {

    private static final Logger log = LoggerFactory.getLogger(LoginAuthService.class);

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public LoginUserInfo login(LoginRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();

        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            log.warn("用户登录参数非法，username 为空或密码为空");
            throw new IllegalArgumentException("用户名或密码不能为空");
        }

        log.info("用户尝试登录，username={}", username);

        QueryWrapper<Account> qw = new QueryWrapper<>();
        qw.eq("account_name", username);
        Account account = accountMapper.selectOne(qw);
        if (account == null) {throw new IllegalArgumentException("账号或密码错误");}

        // 只允许启用状态登录
        if (!"ENABLED".equalsIgnoreCase(account.getStatus())) {
            log.warn("账号状态异常，禁止登录，accountId={}，status={}", account.getAccountId(), account.getStatus());
            throw new IllegalStateException("账号已被禁用或锁定");
        }

        if (!passwordEncoder.matches(password, account.getPasswordHash())) {
            Integer failCount = account.getLoginFailCount();
            account.setLoginFailCount(failCount == null ? 1 : failCount + 1);
            accountMapper.updateById(account);
            log.warn("用户登录失败，密码错误，username={}，当前失败次数={}", username, account.getLoginFailCount());
            throw new IllegalArgumentException("账号或密码错误");
        }

        // 登录成功，重置失败次数并记录登录时间
        account.setLoginFailCount(0);
        account.setLastLoginTime(LocalDateTime.now());
        accountMapper.updateById(account);

        LoginUserInfo info = new LoginUserInfo();
        info.setUserId(String.valueOf(account.getAccountId()));
        info.setUsername(account.getAccountDisplay() != null ? account.getAccountDisplay() : account.getAccountName());
        info.setRoles(Collections.singletonList("ADMIN"));
        info.setPermissions(Collections.singletonList("system:user:view"));

        log.info("用户登录成功，username={}，accountId={}", username, account.getAccountId());

        return info;
    }
}



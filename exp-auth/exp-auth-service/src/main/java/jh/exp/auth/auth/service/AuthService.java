package jh.exp.auth.auth.service;

import com.exp.common.auth.dto.LoginRequest;
import com.exp.common.auth.dto.LoginUserInfo;
import jh.exp.auth.account.domain.Account;
import jh.exp.auth.account.repository.AccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;

/**
 * 提供账号密码校验的核心逻辑，由网关或其他服务通过内部接口调用。
 */
@Service
public class AuthService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AccountRepository accountRepository, PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginUserInfo login(LoginRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();

        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw new IllegalArgumentException("用户名或密码不能为空");
        }

        Account account = accountRepository.findByAccountName(username)
                .orElseThrow(() -> new IllegalArgumentException("账号或密码错误"));

        // 只允许启用状态登录
        if (!"ENABLED".equalsIgnoreCase(account.getStatus())) {
            throw new IllegalStateException("账号已被禁用或锁定");
        }

        if (!passwordEncoder.matches(password, account.getPasswordHash())) {
            Integer failCount = account.getLoginFailCount();
            account.setLoginFailCount(failCount == null ? 1 : failCount + 1);
            accountRepository.save(account);
            throw new IllegalArgumentException("账号或密码错误");
        }

        // 登录成功，重置失败次数并记录登录时间
        account.setLoginFailCount(0);
        account.setLastLoginTime(LocalDateTime.now());
        accountRepository.save(account);

        LoginUserInfo info = new LoginUserInfo();
        info.setUserId(String.valueOf(account.getAccountId()));
        info.setUsername(account.getAccountDisplay() != null ? account.getAccountDisplay() : account.getAccountName());
        info.setRoles(Collections.singletonList("ADMIN"));
        info.setPermissions(Collections.singletonList("system:user:view"));
        return info;
    }
}



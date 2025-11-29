package com.exp.common.auth.client;

import com.exp.common.auth.dto.LoginRequest;
import com.exp.common.auth.dto.LoginUserInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 调用 auth 服务内部登录校验接口的 FeignClient。
 *
 * 提供方：exp-auth-system 服务中的 /internal/auth/login。
 */
@FeignClient(
        name = "exp-auth-system",
        contextId = "authInternalClient",
        path = "/internal/auth"
)
public interface AuthInternalClient {

    /**
     * 用户名 + 密码登录校验。
     */
    @PostMapping("/login")
    LoginUserInfo login(@RequestBody LoginRequest request);
}



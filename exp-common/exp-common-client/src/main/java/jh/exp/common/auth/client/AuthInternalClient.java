package jh.exp.common.auth.client;

import jh.exp.common.auth.dto.LoginRequest;
import jh.exp.common.auth.dto.LoginUserInfo;
import jh.exp.common.auth.dto.ProfileResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 调用 auth 服务内部登录校验接口的 FeignClient。
 *
 * 提供方：exp-auth 服务中的 /internal/auth/login。
 */
@FeignClient(
        name = "exp-auth",
        contextId = "authInternalClient",
        path = "/internal/auth"
)
public interface AuthInternalClient {

    /**
     * 用户名 + 密码登录校验。
     */
    @PostMapping("/login")
    LoginUserInfo login(@RequestBody LoginRequest request);

    /**
     * 根据 userId 获取完整的用户档案信息（角色、权限、菜单等）。
     */
    @GetMapping("/profile")
    ProfileResult profile(@RequestParam("userId") String userId);
}



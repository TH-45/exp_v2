package jh.exp.gateway.auth.client;

import jh.exp.common.core.auth.dto.LoginRequest;
import jh.exp.common.core.auth.dto.LoginUserInfo;
import jh.exp.common.core.auth.dto.ProfileResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

/**
 * 调用认证服务内部接口的 Reactive HTTP 客户端定义。
 *
 * 提供方：exp-auth 服务中的 /internal/auth/**。
 * 使用方：网关（以及其他有需要的服务），通过 WebClient + HttpServiceProxyFactory 生成代理。
 */
@HttpExchange("/internal/auth")
public interface AuthInternalHttpClient {

    /**
     * 用户名 + 密码登录校验。
     */
    @PostExchange("/login")
    Mono<LoginUserInfo> login(@RequestBody LoginRequest request);

    /**
     * 根据 userId 获取完整的用户档案信息（角色、权限、菜单等）。
     */
    @GetExchange("/profile")
    Mono<ProfileResult> profile(@RequestParam("userId") String userId);
}



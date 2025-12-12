package jh.exp.common.audit.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jh.exp.common.auth.CurrentUserHolder;
import jh.exp.common.auth.dto.CurrentUser;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;


import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 从网关透传的 Header 中提取用户信息，并设置到 CurrentUserHolder
 */
@Component
public class AuthContextInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 从网关透传的 Header 中获取用户信息
        String userIdStr = request.getHeader("X-User-Id");
        String userName = request.getHeader("X-User-Name");
        String permissionsStr = request.getHeader("X-User-Permissions");
        // ... 其他信息如 Roles

        // 2. 校验并设置用户信息
        if (userIdStr != null) {
            try {
                Long userId = Long.valueOf(userIdStr);
                Set<String> permissions = parseHeaderValue(permissionsStr);

                CurrentUser currentUser = new CurrentUser(userId, permissions);
                // 注意：这里需要确保 CurrentUser 类能接受这些参数

                // 设置到 ThreadLocal
                CurrentUserHolder.set(currentUser);
            } catch (NumberFormatException e) {
                // 网关传递的 Header 格式错误，通常不应该发生
                // 忽略或记录警告
            }
        }

        // 继续执行后续逻辑（包括 AOP 权限校验）
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 3. 务必清理 ThreadLocal
        CurrentUserHolder.clear();
    }

    private Set<String> parseHeaderValue(String headerValue) {
        if (headerValue == null || headerValue.isEmpty()) {
            return new HashSet<>();
        }
        // 网关使用 String.join(",", ...) 传递，所以这里用逗号分割
        return new HashSet<>(Arrays.asList(headerValue.split(",")));
    }
}
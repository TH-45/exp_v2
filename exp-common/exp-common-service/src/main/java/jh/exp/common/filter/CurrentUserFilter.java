package jh.exp.common.filter;

import jh.exp.common.auth.CurrentUserHolder;
import jh.exp.common.auth.dto.CurrentUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 从 HTTP 请求头中解析当前登录用户信息并填充到 {@link CurrentUserHolder} 的过滤器。
 * <p>
 * 约定的请求头（通常由网关在完成认证后注入）：
 * <ul>
 *     <li>X-User-Id：用户 ID</li>
 *     <li>X-User-Name：用户名称</li>
 *     <li>X-Dept-Id：部门 ID（可选）</li>
 *     <li>X-Dept-Name：部门名称（可选）</li>
 *     <li>X-Roles：角色编码，英文逗号分隔（可选）</li>
 *     <li>X-Permissions：权限编码，英文逗号分隔（可选）</li>
 *     <li>X-Data-Scope：数据权限范围标识（可选）</li>
 * </ul>
 */
@Component
public class CurrentUserFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(CurrentUserFilter.class);

    @Override
    protected void doFilterInternal(@NonNull jakarta.servlet.http.HttpServletRequest request,
                                    @NonNull jakarta.servlet.http.HttpServletResponse response,
                                    @NonNull jakarta.servlet.FilterChain filterChain) throws jakarta.servlet.ServletException, IOException {

        try {
            CurrentUser currentUser = resolveFromHeaders(request);
            if (currentUser != null) {
                CurrentUserHolder.set(currentUser);
            }
            filterChain.doFilter(request, response);
        } finally {
            // 确保每次请求结束都清理 ThreadLocal，避免内存泄漏
            CurrentUserHolder.clear();
        }
    }

    private CurrentUser resolveFromHeaders(jakarta.servlet.http.HttpServletRequest request) {
        String userId = request.getHeader("X-User-Id");
        String userName = request.getHeader("X-User-Name");

        // 如果连 userId 都没有，认为是未登录/匿名请求，不创建 CurrentUser
        if (!StringUtils.hasText(userId)) {
            return null;
        }

        CurrentUser currentUser = new CurrentUser();
        currentUser.setUserId(userId);
        currentUser.setUsername(userName);
        currentUser.setDeptId(request.getHeader("X-Dept-Id"));
        currentUser.setDeptName(request.getHeader("X-Dept-Name"));
        currentUser.setDataScope(request.getHeader("X-Data-Scope"));

        String rolesHeader = request.getHeader("X-Roles");
        currentUser.setRoles(splitToList(rolesHeader));

        String permsHeader = request.getHeader("X-Permissions");
        currentUser.setPermissions(splitToList(permsHeader));

        if (log.isDebugEnabled()) {
            log.debug("Resolved CurrentUser from headers: userId={}, username={}", userId, userName);
        }

        return currentUser;
    }

    private List<String> splitToList(String header) {
        if (!StringUtils.hasText(header)) {
            return List.of();
        }
        return Arrays.stream(header.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
    }
}



package jh.exp.common.service.aspect;


import jh.exp.common.core.annotation.RequiresLogin;
import jh.exp.common.core.annotation.RequiresMenuLevel;
import jh.exp.common.core.annotation.RequiresPermissions;
import jh.exp.common.core.auth.CurrentUserHolder;
import jh.exp.common.core.auth.dto.CurrentUser;
import jh.exp.common.core.exception.AuthException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 统一权限校验切面。
 * <p>
 * 支持：@RequiresLogin、@RequiresPermissions（特殊权限）、@RequiresMenuLevel（菜单等级）。
 */
@Aspect
@Component
public class AuthAspect {

    private static final String POINTCUT_METHOD =
            "@annotation(jh.exp.common.core.annotation.RequiresLogin) || " +
                    "@annotation(jh.exp.common.core.annotation.RequiresPermissions) || " +
                    "@annotation(jh.exp.common.core.annotation.RequiresMenuLevel)";

    @Before(POINTCUT_METHOD)
    public void before(JoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Class<?> targetClass = joinPoint.getTarget().getClass();

        RequiresLogin requiresLogin = AnnotationUtils.findAnnotation(method, RequiresLogin.class);
        RequiresPermissions requiresPermissions = AnnotationUtils.findAnnotation(method, RequiresPermissions.class);
        RequiresMenuLevel requiresMenuLevel = AnnotationUtils.findAnnotation(method, RequiresMenuLevel.class);

        if (requiresLogin == null && requiresPermissions == null && requiresMenuLevel == null) {
            requiresLogin = AnnotationUtils.findAnnotation(targetClass, RequiresLogin.class);
            requiresPermissions = AnnotationUtils.findAnnotation(targetClass, RequiresPermissions.class);
            requiresMenuLevel = AnnotationUtils.findAnnotation(targetClass, RequiresMenuLevel.class);
        }

        if (requiresLogin != null || requiresPermissions != null || requiresMenuLevel != null) {
            checkAuth(requiresLogin, requiresPermissions, requiresMenuLevel);
        }
    }

    private void checkAuth(RequiresLogin requiresLogin, RequiresPermissions requiresPermissions,
                          RequiresMenuLevel requiresMenuLevel) {
        CurrentUser currentUser = CurrentUserHolder.get();
        if (currentUser == null) {
            throw new AuthException("AUTH_FORBIDDEN", "未登录或登录已失效，请重新登录");
        }

        // 特殊权限校验：优先使用 funcPermissionSet，兼容旧 permissions
        if (requiresPermissions != null) {
            String[] required = requiresPermissions.value();
            if (required.length > 0) {
                Set<String> userPerms = currentUser.getFuncPermissionSet();
                if (userPerms == null || userPerms.isEmpty()) {
                    List<String> legacy = currentUser.getPermissions();
                    userPerms = legacy == null ? Collections.emptySet() : new HashSet<>(legacy);
                }
                boolean hasPermission = Arrays.stream(required).anyMatch(userPerms::contains);
                if (!hasPermission) {
                    String requiredStr = StringUtils.arrayToDelimitedString(required, " OR ");
                    throw new AuthException("AUTH_FORBIDDEN", "权限不足，需要以下权限之一: [" + requiredStr + "]");
                }
            }
        }

        // 菜单等级校验
        if (requiresMenuLevel != null && StringUtils.hasText(requiresMenuLevel.code())) {
            Map<String, Integer> menuLevelMap = currentUser.getMenuLevelMap();
            int userLevel = (menuLevelMap != null && menuLevelMap.containsKey(requiresMenuLevel.code()))
                    ? menuLevelMap.get(requiresMenuLevel.code()) : 0;
            int requiredLevel = requiresMenuLevel.level();
            if (userLevel < requiredLevel) {
                throw new AuthException("AUTH_FORBIDDEN", "权限不足，需要 [" + requiresMenuLevel.code() + "] 等级 " + requiredLevel + "，当前等级 " + userLevel);
            }
        }
    }
}
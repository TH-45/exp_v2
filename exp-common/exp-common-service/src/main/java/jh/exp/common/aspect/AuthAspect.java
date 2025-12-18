package jh.exp.common.aspect;


import jh.exp.common.Annotation.RequiresLogin;
import jh.exp.common.Annotation.RequiresPermissions;
import jh.exp.common.auth.CurrentUserHolder;
import jh.exp.common.auth.dto.CurrentUser;
import jh.exp.common.exception.AuthException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;

/**
 * 统一权限校验切面
 */
@Aspect
@Component
public class AuthAspect {

    /**
     * 定义切点：所有带有 @RequiresLogin 或 @RequiresPermissions 注解的方法
     * 也可以更精确地定义 Controller 层的方法
     */
    private static final String POINTCUT_METHOD =
            "@annotation(jh.exp.common.auth.annotation.RequiresLogin) || " +
                    "@annotation(jh.exp.common.auth.annotation.RequiresPermissions)";

    @Before(POINTCUT_METHOD)
    public void before(JoinPoint joinPoint) throws Throwable {
        // 1. 获取目标方法和类上的注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // 优先检查方法上的注解
        RequiresLogin requiresLogin = AnnotationUtils.findAnnotation(method, RequiresLogin.class);
        RequiresPermissions requiresPermissions = AnnotationUtils.findAnnotation(method, RequiresPermissions.class);

        // 如果方法上没有注解，再检查类上的注解 (通常权限注解放在方法上，这里作为可选扩展)
        if (requiresLogin == null && requiresPermissions == null) {
            Class<?> targetClass = joinPoint.getTarget().getClass();
            requiresLogin = AnnotationUtils.findAnnotation(targetClass, RequiresLogin.class);
            requiresPermissions = AnnotationUtils.findAnnotation(targetClass, RequiresPermissions.class);
        }

        // 2. 执行权限校验
        if (requiresLogin != null || requiresPermissions != null) {
            checkAuth(requiresLogin, requiresPermissions);
        }
    }

    /**
     * 执行具体的权限校验逻辑
     */
    private void checkAuth(RequiresLogin requiresLogin, RequiresPermissions requiresPermissions) {
        // 1. 校验登录
        CurrentUser currentUser = CurrentUserHolder.get();
        if (currentUser == null) {
            // 无论是 RequiresLogin 还是 RequiresPermissions，未登录都应该抛出异常
            throw new AuthException("未登录或登录已失效，请重新登录");
        }

        // 2. 校验权限
        if (requiresPermissions != null) {
            String[] requiredPermissions = requiresPermissions.value();
            if (requiredPermissions.length > 0) {
                Set<String> userPermissions = (Set<String>) currentUser.getPermissions();

                // 检查用户是否拥有所需权限中的任意一个 (OR 关系)
                boolean hasPermission = Arrays.stream(requiredPermissions)
                        .anyMatch(permission -> userPermissions.contains(permission));

                if (!hasPermission) {
                    String requiredStr = StringUtils.arrayToDelimitedString(requiredPermissions, " OR ");
                    throw new AuthException("权限不足，需要以下权限之一: [" + requiredStr + "]");
                }
            }
        }
    }
}
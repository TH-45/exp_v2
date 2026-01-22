package jh.exp.common.core.annotation;

import java.lang.annotation.*;

/**
 * 校验当前用户是否已登录
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresLogin {
}
package jh.exp.common.annotation;

import java.lang.annotation.*;

/**
 * 校验当前用户是否拥有指定权限
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresPermissions {

    /**
     * 所需权限标识，支持多个，用户需满足其中任意一个（OR 关系）。
     */
    String[] value();
}
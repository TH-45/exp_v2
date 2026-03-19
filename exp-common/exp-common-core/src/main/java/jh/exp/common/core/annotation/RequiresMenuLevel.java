package jh.exp.common.core.annotation;

import java.lang.annotation.*;

/**
 * 校验当前用户对指定菜单是否达到所需权限等级。
 * <p>
 * 菜单权限四态：0=无, 1=查看, 2=编辑, 3=管理。
 * level 语义：1=可进入页面/查看，2=可编辑，3=可管理（删除、批量操作等）。
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresMenuLevel {

    /**
     * 菜单权限编码，格式：模块:资源，如 contracts:contract、system:role
     */
    String code();

    /**
     * 所需最低等级：1=查看, 2=编辑, 3=管理
     */
    int level() default 1;
}

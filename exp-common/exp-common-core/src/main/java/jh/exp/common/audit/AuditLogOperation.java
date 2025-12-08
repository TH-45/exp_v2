package jh.exp.common.audit;

import java.lang.annotation.*;

/**
 * 标记在需要记录操作审计日志的方法上。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditLogOperation {

    /**
     * 业务模块，如：AUTH、BIDDING、CONTRACT 等
     */
    String module();

    /**
     * 操作名称，如：LOGIN_SUCCESS、PROJECT_CREATE 等
     */
    String action();

    /**
     * 目标对象 ID 的 SpEL 表达式（可选），例如 "#req.userId"
     */
    String targetId() default "";
}
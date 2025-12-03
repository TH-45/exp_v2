package jh.exp.common.exception.handler;

import jh.exp.common.api.ApiResponse;
import org.springframework.core.Ordered;

/**
 * 网关异常处理器接口，支持扩展多种异常处理策略。
 */
public interface GatewayExceptionHandler extends Ordered {

    /**
     * 当前处理器是否支持该异常。
     */
    boolean supports(Throwable throwable);

    /**
     * 处理异常并返回统一响应体。
     */
    ApiResponse<?> handle(Throwable throwable);
}


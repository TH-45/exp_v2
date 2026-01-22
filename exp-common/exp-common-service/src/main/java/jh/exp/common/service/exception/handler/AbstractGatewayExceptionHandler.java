package jh.exp.common.service.exception.handler;

import jh.exp.common.core.api.ApiResponse;

/**
 * 提供默认实现，便于快速创建处理器。
 */
public abstract class AbstractGatewayExceptionHandler implements GatewayExceptionHandler {

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public abstract boolean supports(Throwable throwable);

    @Override
    public abstract ApiResponse<?> handle(Throwable throwable);
}


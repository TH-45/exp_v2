package jh.exp.common.service.exception.handler;

import jh.exp.common.core.api.ApiResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * 404 路由不存在处理器。
 */
@Component
@Order(-3)
public class NotFoundExceptionHandler extends AbstractGatewayExceptionHandler {

    @Override
    public boolean supports(Throwable throwable) {
        return throwable instanceof ResponseStatusException rse
                && rse.getStatusCode() == HttpStatus.NOT_FOUND;
    }

    @Override
    public ApiResponse<?> handle(Throwable throwable) {
        return ApiResponse.fail("404", "访问的路径不存在请确认！");
    }
}


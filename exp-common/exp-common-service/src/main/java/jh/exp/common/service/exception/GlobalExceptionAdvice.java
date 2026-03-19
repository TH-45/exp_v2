package jh.exp.common.service.exception;

import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.service.exception.handler.GatewayExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Comparator;
import java.util.List;

/**
 * 全局异常处理：将 Controller 层抛出的异常转换为统一 ApiResponse。
 * 委托给 GatewayExceptionHandler 链处理，支持 AuthException、BizException 等产出标准错误码。
 */
@RestControllerAdvice
@Order(-100)
public class GlobalExceptionAdvice {

    private final List<GatewayExceptionHandler> handlers;

    public GlobalExceptionAdvice(List<GatewayExceptionHandler> handlers) {
        this.handlers = handlers.stream()
                .sorted(Comparator.comparingInt(GatewayExceptionHandler::getOrder))
                .toList();
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ApiResponse<?>> handleException(Throwable ex) {
        ApiResponse<?> body = handlers.stream()
                .filter(h -> h.supports(ex))
                .findFirst()
                .map(h -> h.handle(ex))
                .orElse(ApiResponse.fail("500", ex.getMessage() != null ? ex.getMessage() : "系统繁忙，请稍后再试"));
        return ResponseEntity.ok(body);
    }
}

//package jh.exp.common.exception;
//
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import jh.exp.common.api.ApiResponse;
//import jh.exp.common.exception.handler.GatewayExceptionHandler;
//import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
//import org.springframework.core.annotation.Order;
//import org.springframework.core.io.buffer.DataBuffer;
//import org.springframework.core.io.buffer.DataBufferFactory;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.http.server.reactive.ServerHttpResponse;
//import org.springframework.stereotype.Component;
//import org.springframework.web.server.ServerWebExchange;
//
//
//import java.nio.charset.StandardCharsets;
//import java.util.Comparator;
//import java.util.List;
//
///**
// * 统一的全局异常处理器，支持多种异常子处理器扩展。
// */
//@Component
//@Order(-2)
//public class GlobalGatewayExceptionHandler implements ErrorWebExceptionHandler {
//
//    private final ObjectMapper objectMapper;
//    private final List<GatewayExceptionHandler> handlers;
//
//    public GlobalGatewayExceptionHandler(ObjectMapper objectMapper,
//                                         List<GatewayExceptionHandler> handlers) {
//        this.objectMapper = objectMapper;
//        this.handlers = handlers.stream()
//                .sorted(Comparator.comparingInt(GatewayExceptionHandler::getOrder))
//                .toList();
//    }
//
//    @Override
//    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
//        ApiResponse<?> body = handlers.stream()
//                .filter(handler -> handler.supports(ex))
//                .findFirst()
//                .map(handler -> handler.handle(ex))
//                .orElse(ApiResponse.fail("500", "系统繁忙，请稍后再试"));
//
//        ServerHttpResponse response = exchange.getResponse();
//        if (response.isCommitted()) {
//            return Mono.error(ex);
//        }
//
//        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
//        response.setStatusCode(HttpStatus.OK); // 保持与前端约定
//
//        byte[] bytes;
//        try {
//            bytes = objectMapper.writeValueAsBytes(body);
//        } catch (JsonProcessingException e) {
//            bytes = String.format(
//                    "{\"success\":false,\"code\":\"%s\",\"message\":\"%s\",\"data\":null}",
//                    body.getCode(), body.getMessage()
//            ).getBytes(StandardCharsets.UTF_8);
//        }
//
//        DataBufferFactory bufferFactory = response.bufferFactory();
//        DataBuffer dataBuffer = bufferFactory.wrap(bytes);
//        return response.writeWith(Mono.just(dataBuffer));
//    }
//}
//

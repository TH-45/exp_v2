package jh.exp.common.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jh.exp.common.audit.AuditLog;
import jh.exp.common.audit.AuditLogOperation;
import jh.exp.common.audit.AuditLogRepository;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
public class AuditLogAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditLogAspect.class);

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;
    private final ExpressionParser spelParser = new SpelExpressionParser();

    public AuditLogAspect(AuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    @Around("@annotation(operation)")
    public Object around(ProceedingJoinPoint pjp, AuditLogOperation operation) throws Throwable {
        // 1. 拿到 HTTP 请求，如果没有（比如异步线程），直接执行方法，不记录审计
        HttpServletRequest request = currentRequest();
        if (request == null) {
            return pjp.proceed();
        }

        // 2. 基础信息
        String uri = request.getRequestURI();
        String method = request.getMethod();
        String clientIp = clientIp(request);
        String userId = request.getHeader("X-User-Id");
        String userName = request.getHeader("X-User-Name");

        // 3. 解析 targetId（SpEL，可选）
        String targetId = resolveTargetId(operation.targetId(), pjp);

        // 4. 序列化请求参数（简单版本：序列化方法参数）
        String requestParams = buildRequestParams(pjp);

        // 5. 默认认为执行成功，异常时再改
        boolean success = true;
        String errorCode = null;
        String errorMessage = null;

        Object result;
        try {
            result = pjp.proceed();
        } catch (Throwable ex) {
            success = false;
            // 这里你可以以后按异常类型映射业务错误码，目前先简单用类名
            errorCode = ex.getClass().getSimpleName();
            errorMessage = ex.getMessage();
            // 先记录，再抛出去给全局异常处理
            saveLog(operation, targetId, userId, userName, uri, method, clientIp,
                    requestParams, success, errorCode, errorMessage);
            throw ex;
        }

        // 正常结束也记一条
        saveLog(operation, targetId, userId, userName, uri, method, clientIp,
                requestParams, success, errorCode, errorMessage);

        return result;
    }

    private void saveLog(AuditLogOperation operation,
                         String targetId,
                         String userId,
                         String userName,
                         String uri,
                         String method,
                         String clientIp,
                         String requestParams,
                         boolean success,
                         String errorCode,
                         String errorMessage) {

        try {
            AuditLog logEntity = new AuditLog();
            logEntity.setModule(operation.module());
            logEntity.setAction(operation.action());
            logEntity.setTargetId(targetId);
            logEntity.setUserId(userId);
            logEntity.setUserName(userName);
            logEntity.setRequestUri(uri);
            logEntity.setHttpMethod(method);
            logEntity.setClientIp(clientIp);
            logEntity.setRequestParams(requestParams);
            logEntity.setSuccess(success);
            logEntity.setErrorCode(errorCode);
            logEntity.setErrorMessage(truncate(errorMessage, 255));
            logEntity.setCreatedAt(LocalDateTime.now());

            auditLogRepository.save(logEntity);
        } catch (Exception e) {
            // 审计日志本身失败不能影响主流程，只打个错误日志
            log.error("保存审计日志失败", e);
        }
    }

    private HttpServletRequest currentRequest() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes servletRequestAttributes) {
            return servletRequestAttributes.getRequest();
        }
        return null;
    }

    private String clientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) {
            int comma = ip.indexOf(',');
            return comma > 0 ? ip.substring(0, comma).trim() : ip.trim();
        }
        return request.getRemoteAddr();
    }

    private String resolveTargetId(String spel, ProceedingJoinPoint pjp) {
        if (spel == null || spel.isBlank()) {
            return null;
        }
        try {
            MethodSignature signature = (MethodSignature) pjp.getSignature();
            String[] paramNames = signature.getParameterNames();
            Object[] args = pjp.getArgs();

            StandardEvaluationContext context = new StandardEvaluationContext();
            if (paramNames != null) {
                for (int i = 0; i < paramNames.length; i++) {
                    context.setVariable(paramNames[i], args[i]);
                }
            }
            Expression expression = spelParser.parseExpression(spel);
            Object value = expression.getValue(context);
            return value != null ? String.valueOf(value) : null;
        } catch (Exception ex) {
            log.warn("解析 AuditLogOperation.targetId 表达式失败: {}", spel, ex);
            return null;
        }
    }

    private String buildRequestParams(ProceedingJoinPoint pjp) {
        try {
            MethodSignature signature = (MethodSignature) pjp.getSignature();
            String[] paramNames = signature.getParameterNames();
            Object[] args = pjp.getArgs();

            Map<String, Object> map = new HashMap<>();
            if (paramNames != null) {
                for (int i = 0; i < paramNames.length; i++) {
                    map.put(paramNames[i], args[i]);
                }
            }
            String json = objectMapper.writeValueAsString(map);
            return truncate(json, 2000); // 防止太长
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return null;
        return s.length() <= maxLen ? s : s.substring(0, maxLen);
    }
}
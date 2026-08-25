package com.health.platform.log;

import com.health.platform.security.context.CurrentUser;
import com.health.platform.security.context.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 操作日志切面：同步经 Feign 写入 system-service，失败仅告警不影响主流程。
 */
@Aspect
public class OperationLogAspect {

    private static final Logger log = LoggerFactory.getLogger(OperationLogAspect.class);

    private static final ExpressionParser SPEL_PARSER = new SpelExpressionParser();

    private final ObjectProvider<OperationLogClient> clientProvider;

    public OperationLogAspect(ObjectProvider<OperationLogClient> clientProvider) {
        this.clientProvider = clientProvider;
    }

    @AfterReturning(pointcut = "@annotation(operationLog)")
    public void afterReturning(JoinPoint joinPoint, OperationLog operationLog) {
        send(joinPoint, operationLog, "SUCCESS", null);
    }

    @AfterThrowing(pointcut = "@annotation(operationLog)", throwing = "ex")
    public void afterThrowing(JoinPoint joinPoint, OperationLog operationLog, Throwable ex) {
        send(joinPoint, operationLog, "FAIL", ex.getMessage());
    }

    private void send(JoinPoint joinPoint, OperationLog operationLog, String result, String error) {
        try {
            OperationLogClient client = clientProvider.getIfAvailable();
            if (client == null) {
                return;
            }
            OperationLogEvent event = new OperationLogEvent();
            CurrentUser user = UserContext.get();
            if (user != null) {
                event.setOperatorId(user.userId());
                event.setOperatorName(user.username());
            } else {
                // TODO: 登录等无用户上下文场景补充精确操作人（如登录用户名）
                event.setOperatorId(0L);
                event.setOperatorName("anonymous");
            }
            event.setOperation(operationLog.operation());
            event.setTargetType(operationLog.targetType());
            event.setTargetId(resolveTargetId(joinPoint, operationLog.targetId()));
            event.setRequestId(MDC.get("requestId"));
            event.setIp(currentIp());
            event.setResult(result);
            if (error != null) {
                event.setDetail(truncate(error, 900));
            }
            client.save(event);
        } catch (Exception e) {
            log.warn("操作日志记录失败 operation={}: {}", operationLog.operation(), e.getMessage());
        }
    }

    private String resolveTargetId(JoinPoint joinPoint, String expression) {
        if (!StringUtils.hasText(expression)) {
            return null;
        }
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        MethodBasedEvaluationContext context = new MethodBasedEvaluationContext(
                null, signature.getMethod(), joinPoint.getArgs(), new DefaultParameterNameDiscoverer());
        Object value = SPEL_PARSER.parseExpression(expression).getValue(context);
        return value == null ? null : String.valueOf(value);
    }

    private String currentIp() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        HttpServletRequest request = attributes.getRequest();
        String forwarded = request.getHeader("X-Forwarded-For");
        return StringUtils.hasText(forwarded) ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
    }

    private String truncate(String text, int maxLength) {
        return text.length() <= maxLength ? text : text.substring(0, maxLength);
    }
}

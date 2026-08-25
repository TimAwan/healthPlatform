package com.health.platform.web.handler;

import com.health.platform.core.exception.AuthenticationException;
import com.health.platform.core.exception.BizException;
import com.health.platform.core.exception.PermissionDeniedException;
import com.health.platform.core.result.CommonErrorCode;
import com.health.platform.core.result.Result;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpMethod;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlobalExceptionHandlerTest {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void bizExceptionShouldKeepCodeAndMessage() {
        Result<Void> result = handler.handleBizException(new BizException(400100, "医生不存在"));
        assertEquals(400100, result.getCode());
        assertEquals("医生不存在", result.getMessage());
    }

    @Test
    void authenticationExceptionShouldReturn401Code() {
        Result<Void> result = handler.handleAuthenticationException(
                new AuthenticationException(CommonErrorCode.TOKEN_EXPIRED));
        assertEquals(401002, result.getCode());
    }

    @Test
    void permissionDeniedShouldReturn403Code() {
        Result<Void> result = handler.handlePermissionDeniedException(new PermissionDeniedException());
        assertEquals(403001, result.getCode());
    }

    @Test
    void bindExceptionShouldJoinFieldErrors() {
        Bean target = new Bean();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "bean");
        bindingResult.rejectValue("name", "NotBlank", "不能为空");
        Result<Void> result = handler.handleValidationException(new BindException(bindingResult));
        assertEquals(CommonErrorCode.PARAM_ERROR.getCode(), result.getCode());
        assertTrue(result.getMessage().contains("name"));
        assertTrue(result.getMessage().contains("不能为空"));
    }

    @Test
    void constraintViolationShouldReturnParamError() {
        Query query = new Query();
        query.setPage(-1);
        Set<ConstraintViolation<Query>> violations = VALIDATOR.validate(query);
        Result<Void> result = handler.handleConstraintViolationException(
                new ConstraintViolationException(violations));
        assertEquals(CommonErrorCode.PARAM_ERROR.getCode(), result.getCode());
        assertTrue(result.getMessage().contains("page"));
    }

    @Test
    void messageNotReadableShouldReturnRequestBodyError() {
        Result<Void> result = handler.handleMessageNotReadable(
                new HttpMessageNotReadableException("bad json", (HttpInputMessage) null));
        assertEquals(CommonErrorCode.REQUEST_BODY_ERROR.getCode(), result.getCode());
    }

    @Test
    void noResourceFoundShouldReturn404Code() {
        Result<Void> result = handler.handleNoResourceFound(
                new NoResourceFoundException(HttpMethod.GET, "/api/v1/none"));
        assertEquals(CommonErrorCode.NOT_FOUND.getCode(), result.getCode());
    }

    @Test
    void methodNotSupportedShouldReturn405Code() {
        Result<Void> result = handler.handleMethodNotSupported(
                new HttpRequestMethodNotSupportedException("DELETE"));
        assertEquals(CommonErrorCode.METHOD_NOT_ALLOWED.getCode(), result.getCode());
    }

    @Test
    void unexpectedExceptionShouldMaskDetails() {
        Result<Void> result = handler.handleUnexpected(new RuntimeException("NPE at secret path"));
        assertEquals(CommonErrorCode.SYSTEM_ERROR.getCode(), result.getCode());
        assertEquals(CommonErrorCode.SYSTEM_ERROR.getMessage(), result.getMessage());
    }

    static class Bean {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    static class Query {
        @Positive(message = "必须大于 0")
        private int page;

        @NotBlank
        private String keyword;

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public String getKeyword() {
            return keyword;
        }

        public void setKeyword(String keyword) {
            this.keyword = keyword;
        }
    }
}

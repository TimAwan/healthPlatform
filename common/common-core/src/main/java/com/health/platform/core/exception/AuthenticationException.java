package com.health.platform.core.exception;

import com.health.platform.core.result.ErrorCode;

/**
 * 未认证 / 凭证无效。全局异常处理器将其映射为 HTTP 401。
 */
public class AuthenticationException extends BizException {

    private static final long serialVersionUID = 1L;

    public AuthenticationException(ErrorCode errorCode) {
        super(errorCode);
    }

    public AuthenticationException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}

package com.health.platform.core.exception;

import com.health.platform.core.result.CommonErrorCode;
import com.health.platform.core.result.ErrorCode;

/**
 * 已认证但无权限。全局异常处理器将其映射为 HTTP 403。
 */
public class PermissionDeniedException extends BizException {

    private static final long serialVersionUID = 1L;

    public PermissionDeniedException() {
        super(CommonErrorCode.FORBIDDEN);
    }

    public PermissionDeniedException(ErrorCode errorCode) {
        super(errorCode);
    }

    public PermissionDeniedException(String message) {
        super(CommonErrorCode.FORBIDDEN, message);
    }
}

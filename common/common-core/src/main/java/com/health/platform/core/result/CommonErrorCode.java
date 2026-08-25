package com.health.platform.core.result;

/**
 * 全局通用错误码，分段规则：
 * 400xxx 参数/业务，401xxx 认证，403xxx 权限，404xxx 资源，405xxx 方法，409xxx 冲突，500xxx 系统。
 * 各业务服务可自行扩展错误码枚举并实现 {@link ErrorCode}。
 */
public enum CommonErrorCode implements ErrorCode {

    SUCCESS(200, "success"),

    PARAM_ERROR(400001, "参数校验失败"),
    BUSINESS_ERROR(400002, "业务处理失败"),
    REQUEST_BODY_ERROR(400003, "请求体格式错误"),

    UNAUTHORIZED(401001, "未登录或凭证无效"),
    TOKEN_EXPIRED(401002, "登录已过期，请重新登录"),
    TOKEN_INVALID(401003, "凭证无效"),
    CAPTCHA_ERROR(401004, "验证码错误或已过期"),
    LOGIN_FAILED(401005, "用户名或密码错误"),
    LOGIN_LOCKED(401006, "登录失败次数过多，账号已临时锁定"),

    FORBIDDEN(403001, "无权限访问"),

    NOT_FOUND(404001, "资源不存在"),

    METHOD_NOT_ALLOWED(405001, "请求方法不支持"),

    CONFLICT(409001, "请求冲突，请勿重复提交"),

    SYSTEM_ERROR(500000, "系统繁忙，请稍后重试");

    private final int code;
    private final String message;

    CommonErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}

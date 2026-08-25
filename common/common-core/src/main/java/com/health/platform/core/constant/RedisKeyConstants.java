package com.health.platform.core.constant;

public final class RedisKeyConstants {

    private RedisKeyConstants() {
    }

    private static final String PREFIX = "health:";

    /** 图形验证码，value 为验证码文本 */
    public static final String CAPTCHA = PREFIX + "auth:captcha:";

    /** 登录失败计数，value 为失败次数 */
    public static final String LOGIN_FAIL_COUNT = PREFIX + "auth:fail:";

    /** refresh_token，value 为 token_id 关联信息 */
    public static final String REFRESH_TOKEN = PREFIX + "auth:rt:";

    /** token 黑名单（注销），存在即拒绝 */
    public static final String TOKEN_BLACKLIST = PREFIX + "auth:blacklist:";

    /** 用户权限码集合缓存 */
    public static final String USER_PERMISSIONS = PREFIX + "perm:";

    /** 防重复提交 */
    public static final String IDEMPOTENT = PREFIX + "idempotent:";
}

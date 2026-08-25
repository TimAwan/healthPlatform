package com.health.platform.security.context;

public final class UserContext {

    private static final ThreadLocal<CurrentUser> HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(CurrentUser user) {
        HOLDER.set(user);
    }

    public static CurrentUser get() {
        return HOLDER.get();
    }

    public static CurrentUser require() {
        CurrentUser user = HOLDER.get();
        if (user == null) {
            throw new com.health.platform.core.exception.AuthenticationException(
                    com.health.platform.core.result.CommonErrorCode.UNAUTHORIZED);
        }
        return user;
    }

    public static Long currentUserIdOrNull() {
        CurrentUser user = HOLDER.get();
        return user == null ? null : user.userId();
    }

    public static void clear() {
        HOLDER.remove();
    }
}

package com.health.platform.security.permission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明接口所需权限，权限码命名规范：资源_动作，如 DOCTOR_APPROVE（说明书第 23 章）。
 * 支持方法级与类级，方法级覆盖类级。
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {

    String[] value();

    Logical logical() default Logical.ANY;

    enum Logical {
        ANY, ALL
    }
}

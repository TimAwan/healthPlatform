package com.health.platform.log;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记需要记录操作日志的接口方法（说明书第 25 章：登录、医生增删改、审核、上下架、权限修改）。
 * targetId 支持 SpEL，可引用方法参数，如 "#id"、'#dto.id'。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationLog {

    /** 操作名称，如：新增医生 */
    String operation();

    /** 目标类型，如：DOCTOR */
    String targetType() default "";

    /** 目标 ID 的 SpEL 表达式 */
    String targetId() default "";
}

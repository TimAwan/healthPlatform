package com.health.platform.core.result;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResultTest {

    @Test
    void okShouldReturnSuccessCodeWithoutData() {
        Result<Void> result = Result.ok();
        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
        assertNull(result.getData());
        assertTrue(result.isSuccess());
    }

    @Test
    void okWithDataShouldCarryData() {
        Result<String> result = Result.ok("abc");
        assertEquals(200, result.getCode());
        assertEquals("abc", result.getData());
    }

    @Test
    void failShouldUseErrorCode() {
        Result<Void> result = Result.fail(CommonErrorCode.PARAM_ERROR);
        assertEquals(400001, result.getCode());
        assertEquals("参数校验失败", result.getMessage());
        assertFalse(result.isSuccess());
    }

    @Test
    void failWithCustomMessageShouldKeepCode() {
        Result<Void> result = Result.fail(400100, "自定义错误");
        assertEquals(400100, result.getCode());
        assertEquals("自定义错误", result.getMessage());
    }
}

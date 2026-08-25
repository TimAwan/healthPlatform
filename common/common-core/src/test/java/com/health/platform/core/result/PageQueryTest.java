package com.health.platform.core.result;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PageQueryTest {

    @Test
    void defaultsShouldBePageOneSizeTen() {
        PageQuery query = new PageQuery();
        assertEquals(1, query.getPage());
        assertEquals(10, query.getSize());
        assertEquals(0, query.offset());
    }

    @Test
    void offsetShouldSkipPreviousPages() {
        PageQuery query = new PageQuery();
        query.setPage(3);
        query.setSize(20);
        assertEquals(40, query.offset());
    }

    @Test
    void pageResultEmptyShouldHaveZeroTotalAndEmptyList() {
        PageResult<String> result = PageResult.empty(2, 10);
        assertEquals(0, result.getTotal());
        assertTrue(result.getList().isEmpty());
    }

    @Test
    void pageResultNullListShouldBecomeEmpty() {
        PageResult<String> result = new PageResult<>(5, 1, 10, null);
        assertTrue(result.getList().isEmpty());
        assertEquals(5, result.getTotal());
    }

    @Test
    void pageResultOfShouldKeepList() {
        PageResult<String> result = PageResult.of(2, 1, 10, List.of("a", "b"));
        assertEquals(2, result.getTotal());
        assertEquals(2, result.getList().size());
    }
}

package com.health.platform.core.result;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class PageQuery {

    private static final int MAX_SIZE = 100;

    @Min(value = 1, message = "页码必须大于等于 1")
    protected int page = 1;

    @Min(value = 1, message = "每页条数必须大于等于 1")
    @Max(value = MAX_SIZE, message = "每页条数不能超过 " + MAX_SIZE)
    protected int size = 10;

    public long offset() {
        return (long) (page - 1) * size;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}

package com.health.platform.core.result;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private long total;
    private int page;
    private int size;
    private List<T> list;

    public PageResult() {
        this.list = Collections.emptyList();
    }

    public PageResult(long total, int page, int size, List<T> list) {
        this.total = total;
        this.page = page;
        this.size = size;
        this.list = list == null ? Collections.emptyList() : list;
    }

    public static <T> PageResult<T> empty(int page, int size) {
        return new PageResult<>(0, page, size, Collections.emptyList());
    }

    public static <T> PageResult<T> of(long total, int page, int size, List<T> list) {
        return new PageResult<>(total, page, size, list);
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
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

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }
}

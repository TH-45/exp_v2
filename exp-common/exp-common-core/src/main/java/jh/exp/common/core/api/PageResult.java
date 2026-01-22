package jh.exp.common.core.api;

import java.util.List;

/**
 * 统一分页结果结构，对应 docs 中的 PageResult&lt;T&gt; 约定。
 */
public class PageResult<T> {

    /**
     * 当前页数据
     */
    private List<T> records;

    /**
     * 总条数
     */
    private long total;

    /**
     * 当前页，从 1 开始
     */
    private int page;

    /**
     * 每页数量
     */
    private int pageSize;

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
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

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}













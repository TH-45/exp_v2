package jh.exp.common.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.List;

/**
 * 通用分页查询请求类
 * <p>
 * 用于统一处理分页查询参数，包含页码、每页数量以及可选的排序信息。
 * </p>
 */
public class PageRequest {

    /**
     * 默认页码
     */
    private static final int DEFAULT_PAGE = 1;

    /**
     * 默认每页数量
     */
    private static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * 最大每页数量限制
     */
    private static final int MAX_PAGE_SIZE = 100;

    /**
     * 当前页，从 1 开始
     */
    @Min(value = 1, message = "页码不能小于1")
    private Integer page = DEFAULT_PAGE;

    /**
     * 每页数量
     */
    @Min(value = 1, message = "每页数量不能小于1")
    @Max(value = MAX_PAGE_SIZE, message = "每页数量不能超过" + MAX_PAGE_SIZE)
    private Integer pageSize = DEFAULT_PAGE_SIZE;

    /**
     * 排序字段（可选）
     */
    private String sortField;

    /**
     * 排序方向：ASC 或 DESC（可选）
     */
    private String sortOrder;

    /**
     * 查询的字段（可选）
     */
    private List<String> queryField;

    public List<String> getQueryField() {

        return queryField;
    }

    public void setQueryField(List<String> queryField) {
        this.queryField = queryField;
    }

    public PageRequest() {
    }

    public PageRequest(Integer page, Integer pageSize) {
        this.page = page != null && page > 0 ? page : DEFAULT_PAGE;
        this.pageSize = pageSize != null && pageSize > 0 ? pageSize : DEFAULT_PAGE_SIZE;
        if (this.pageSize > MAX_PAGE_SIZE) {
            this.pageSize = MAX_PAGE_SIZE;
        }
    }

    /**
     * 获取当前页码，如果未设置则返回默认值
     */
    public Integer getPage() {
        return page != null && page > 0 ? page : DEFAULT_PAGE;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    /**
     * 获取每页数量，如果未设置则返回默认值
     */
    public Integer getPageSize() {
        if (pageSize == null || pageSize <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return pageSize > MAX_PAGE_SIZE ? MAX_PAGE_SIZE : pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * 获取排序字段
     */
    public String getSortField() {
        return sortField;
    }

    public void setSortField(String sortField) {
        this.sortField = sortField;
    }

    /**
     * 获取排序方向
     */
    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    /**
     * 计算偏移量（用于数据库查询）
     * 
     * @return 偏移量
     */
    public int getOffset() {
        return (getPage() - 1) * getPageSize();
    }

    /**
     * 判断是否按升序排序
     * 
     * @return true 如果排序方向为 ASC 或未设置，false 如果为 DESC
     */
    public boolean isAsc() {
        if (sortOrder == null || sortOrder.trim().isEmpty()) {
            return true; // 默认升序
        }
        return "ASC".equalsIgnoreCase(sortOrder.trim());
    }

    /**
     * 判断是否按降序排序
     * 
     * @return true 如果排序方向为 DESC，false 否则
     */
    public boolean isDesc() {
        return "DESC".equalsIgnoreCase(sortOrder != null ? sortOrder.trim() : "");
    }
}


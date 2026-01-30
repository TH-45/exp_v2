package jh.exp.common.core.res;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jh.exp.common.core.req.SimplePageReq;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public  class SimplePageRes<T> {
    private Long total;
    private Long page;
    private Long size;
    private List<T> list;

    public SimplePageRes(List<T> list) {
        this.list = list;
    }

    public static <T> SimplePageRes<T> toPageRes(Page<T> result, SimplePageReq<?> pageReq) {
        SimplePageRes<T> pageResult = new SimplePageRes<>();
        pageResult.setList(result.getRecords());
        pageResult.setTotal(result.getTotal());
        pageResult.setPage((long) pageReq.getPageNum());
        pageResult.setSize((long) pageReq.getPageSize());
        return pageResult;
    }
}

package jh.exp.project.client.api;

import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.project.core.entity.ProjectStaffAssign;
import jh.exp.project.core.entity.req.BatchDeleteByIdsReq;
import jh.exp.project.core.entity.req.DeleteByIdReq;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("/internal/project/projectStaffAssign")
public interface ProjectStaffAssignClientService {
    @PostExchange("/list")
    ApiResponse<SimplePageRes<ProjectStaffAssign>> list(@RequestBody SimplePageReq<Object> req);

    @GetExchange("/detail")
    ApiResponse<ProjectStaffAssign> detail(@RequestParam("id") Long id);

    @PostExchange("/create")
    ApiResponse<ProjectStaffAssign> create(@RequestBody ProjectStaffAssign req);

    @PostExchange("/update")
    ApiResponse<ProjectStaffAssign> update(@RequestBody ProjectStaffAssign req);

    @PostExchange("/delete")
    ApiResponse<Void> delete(@RequestBody DeleteByIdReq req);

    @PostExchange("/batchDelete")
    ApiResponse<Void> batchDelete(@RequestBody BatchDeleteByIdsReq req);
}

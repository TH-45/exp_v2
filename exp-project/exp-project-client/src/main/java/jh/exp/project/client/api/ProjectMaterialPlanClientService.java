package jh.exp.project.client.api;

import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.project.core.entity.ProjectMaterialPlan;
import jh.exp.project.core.entity.req.BatchDeleteByIdsReq;
import jh.exp.project.core.entity.req.DeleteByIdReq;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("/projectMaterialPlan")
public interface ProjectMaterialPlanClientService {
    @PostExchange("/list")
    ApiResponse<SimplePageRes<ProjectMaterialPlan>> list(@RequestBody SimplePageReq<Object> req);

    @GetExchange("/detail")
    ApiResponse<ProjectMaterialPlan> detail(@RequestParam("planId") Long planId);

    @PostExchange("/create")
    ApiResponse<ProjectMaterialPlan> create(@RequestBody ProjectMaterialPlan req);

    @PostExchange("/update")
    ApiResponse<ProjectMaterialPlan> update(@RequestBody ProjectMaterialPlan req);

    @PostExchange("/delete")
    ApiResponse<Void> delete(@RequestBody DeleteByIdReq req);

    @PostExchange("/batchDelete")
    ApiResponse<Void> batchDelete(@RequestBody BatchDeleteByIdsReq req);
}

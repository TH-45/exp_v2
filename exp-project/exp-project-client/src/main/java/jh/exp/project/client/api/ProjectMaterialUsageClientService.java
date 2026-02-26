package jh.exp.project.client.api;

import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.project.core.entity.ProjectMaterialUsage;
import jh.exp.project.core.entity.req.BatchDeleteByIdsReq;
import jh.exp.project.core.entity.req.DeleteByIdReq;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("/internal/project/projectMaterialUsage")
public interface ProjectMaterialUsageClientService {
    @PostExchange("/list")
    ApiResponse<SimplePageRes<ProjectMaterialUsage>> list(@RequestBody SimplePageReq<Object> req);

    @GetExchange("/detail")
    ApiResponse<ProjectMaterialUsage> detail(@RequestParam("usageId") Long usageId);

    @PostExchange("/create")
    ApiResponse<ProjectMaterialUsage> create(@RequestBody ProjectMaterialUsage req);

    @PostExchange("/update")
    ApiResponse<ProjectMaterialUsage> update(@RequestBody ProjectMaterialUsage req);

    @PostExchange("/delete")
    ApiResponse<Void> delete(@RequestBody DeleteByIdReq req);

    @PostExchange("/batchDelete")
    ApiResponse<Void> batchDelete(@RequestBody BatchDeleteByIdsReq req);
}

package jh.exp.project.client.api;

import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.project.core.entity.Project;
import jh.exp.project.core.entity.req.BatchDeleteByIdsReq;
import jh.exp.project.core.entity.req.DeleteByIdReq;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;
import java.util.Map;

@HttpExchange("/internal/project/project")
public interface ProjectClientService {
    @PostExchange("/list")
    ApiResponse<SimplePageRes<Project>> list(@RequestBody SimplePageReq<Object> req);

    @GetExchange("/detail")
    ApiResponse<Project> detail(@RequestParam("projectId") Long projectId);

    @PostExchange("/create")
    ApiResponse<Project> create(@RequestBody Project req);

    @PostExchange("/update")
    ApiResponse<Project> update(@RequestBody Project req);

    @PostExchange("/delete")
    ApiResponse<Void> delete(@RequestBody DeleteByIdReq req);

    @PostExchange("/batchDelete")
    ApiResponse<Void> batchDelete(@RequestBody BatchDeleteByIdsReq req);

    /**
     * 批量获取项目信息
     * @param projectIds
     * @return
     */
    @PostExchange("/batchGetProjectByIds")
    ApiResponse<Map<Long,Project>> batchGetProjectByIds(List<Long> projectIds);
}

package jh.exp.project.client.api;

import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.project.core.entity.ProjectScheduleLog;
import jh.exp.project.core.entity.req.BatchDeleteByIdsReq;
import jh.exp.project.core.entity.req.DeleteByIdReq;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("/internal/project/projectScheduleLog")
public interface ProjectScheduleLogClientService {
    @PostExchange("/list")
    ApiResponse<SimplePageRes<ProjectScheduleLog>> list(@RequestBody SimplePageReq<Object> req);

    @GetExchange("/detail")
    ApiResponse<ProjectScheduleLog> detail(@RequestParam("logId") Long logId);

    @PostExchange("/create")
    ApiResponse<ProjectScheduleLog> create(@RequestBody ProjectScheduleLog req);

    @PostExchange("/update")
    ApiResponse<ProjectScheduleLog> update(@RequestBody ProjectScheduleLog req);

    @PostExchange("/delete")
    ApiResponse<Void> delete(@RequestBody DeleteByIdReq req);

    @PostExchange("/batchDelete")
    ApiResponse<Void> batchDelete(@RequestBody BatchDeleteByIdsReq req);
}

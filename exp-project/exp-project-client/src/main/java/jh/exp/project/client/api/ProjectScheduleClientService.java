package jh.exp.project.client.api;

import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.project.core.entity.ProjectSchedule;
import jh.exp.project.core.entity.req.BatchDeleteByIdsReq;
import jh.exp.project.core.entity.req.DeleteByIdReq;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("/projectSchedule")
public interface ProjectScheduleClientService {
    @PostExchange("/list")
    ApiResponse<SimplePageRes<ProjectSchedule>> list(@RequestBody SimplePageReq<Object> req);

    @GetExchange("/detail")
    ApiResponse<ProjectSchedule> detail(@RequestParam("scheduleId") Long scheduleId);

    @PostExchange("/create")
    ApiResponse<ProjectSchedule> create(@RequestBody ProjectSchedule req);

    @PostExchange("/update")
    ApiResponse<ProjectSchedule> update(@RequestBody ProjectSchedule req);

    @PostExchange("/delete")
    ApiResponse<Void> delete(@RequestBody DeleteByIdReq req);

    @PostExchange("/batchDelete")
    ApiResponse<Void> batchDelete(@RequestBody BatchDeleteByIdsReq req);
}

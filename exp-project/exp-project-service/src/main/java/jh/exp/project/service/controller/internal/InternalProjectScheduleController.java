package jh.exp.project.service.controller.internal;

import jakarta.validation.Valid;
import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.project.core.entity.ProjectSchedule;
import jh.exp.project.core.entity.req.BatchDeleteByIdsReq;
import jh.exp.project.core.entity.req.DeleteByIdReq;
import jh.exp.project.service.service.internal.ProjectScheduleInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/project/projectSchedule")
@RequiredArgsConstructor
public class InternalProjectScheduleController {
    private final ProjectScheduleInternalService service;

    @PostMapping("/list")
    public ApiResponse<SimplePageRes<ProjectSchedule>> list(@RequestBody SimplePageReq<Object> req) {
        req.pageDefault();
        return ApiResponse.success(service.list(req));
    }

    @GetMapping("/detail")
    public ApiResponse<ProjectSchedule> detail(@RequestParam Long scheduleId) {
        return ApiResponse.success(service.detail(scheduleId));
    }

    @PostMapping("/create")
    public ApiResponse<ProjectSchedule> create(@RequestBody @Valid ProjectSchedule req) {
        return ApiResponse.success(service.create(req));
    }

    @PostMapping("/update")
    public ApiResponse<ProjectSchedule> update(@RequestBody @Valid ProjectSchedule req) {
        return ApiResponse.success(service.update(req));
    }

    @PostMapping("/delete")
    public ApiResponse<Void> delete(@RequestBody @Valid DeleteByIdReq req) {
        service.delete(req);
        return ApiResponse.success(null);
    }

    @PostMapping("/batchDelete")
    public ApiResponse<Void> batchDelete(@RequestBody @Valid BatchDeleteByIdsReq req) {
        service.batchDelete(req);
        return ApiResponse.success(null);
    }
}

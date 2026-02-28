package jh.exp.project.service.controller.bus;

import jakarta.validation.Valid;
import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.project.core.entity.ProjectMaterialPlan;
import jh.exp.project.core.entity.req.BatchDeleteByIdsReq;
import jh.exp.project.core.entity.req.DeleteByIdReq;
import jh.exp.project.service.service.bus.ProjectMaterialPlanInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/projectMaterialPlan")
@RequiredArgsConstructor
public class ProjectMaterialPlanController {
    private final ProjectMaterialPlanInternalService service;

    @PostMapping("/list")
    public ApiResponse<SimplePageRes<ProjectMaterialPlan>> list(@RequestBody SimplePageReq<Object> req) {
        req.pageDefault();
        return ApiResponse.success(service.list(req));
    }

    @GetMapping("/detail")
    public ApiResponse<ProjectMaterialPlan> detail(@RequestParam Long planId) {
        return ApiResponse.success(service.detail(planId));
    }

    @PostMapping("/create")
    public ApiResponse<ProjectMaterialPlan> create(@RequestBody @Valid ProjectMaterialPlan req) {
        return ApiResponse.success(service.create(req));
    }

    @PostMapping("/update")
    public ApiResponse<ProjectMaterialPlan> update(@RequestBody @Valid ProjectMaterialPlan req) {
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

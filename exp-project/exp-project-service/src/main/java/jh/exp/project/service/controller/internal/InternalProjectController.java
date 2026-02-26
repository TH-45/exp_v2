package jh.exp.project.service.controller.internal;

import jakarta.validation.Valid;
import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.project.core.entity.Project;
import jh.exp.project.core.entity.req.BatchDeleteByIdsReq;
import jh.exp.project.core.entity.req.DeleteByIdReq;
import jh.exp.project.service.service.internal.ProjectInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/project/project")
@RequiredArgsConstructor
public class InternalProjectController {
    private final ProjectInternalService service;

    @PostMapping("/list")
    public ApiResponse<SimplePageRes<Project>> list(@RequestBody SimplePageReq<Object> req) {
        req.pageDefault();
        return ApiResponse.success(service.list(req));
    }

    @GetMapping("/detail")
    public ApiResponse<Project> detail(@RequestParam Long projectId) {
        return ApiResponse.success(service.detail(projectId));
    }

    @PostMapping("/create")
    public ApiResponse<Project> create(@RequestBody @Valid Project req) {
        return ApiResponse.success(service.create(req));
    }

    @PostMapping("/update")
    public ApiResponse<Project> update(@RequestBody @Valid Project req) {
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

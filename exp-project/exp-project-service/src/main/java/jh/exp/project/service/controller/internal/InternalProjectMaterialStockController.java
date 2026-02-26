package jh.exp.project.service.controller.internal;

import jakarta.validation.Valid;
import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.project.core.entity.ProjectMaterialStock;
import jh.exp.project.core.entity.req.BatchDeleteByIdsReq;
import jh.exp.project.core.entity.req.DeleteByIdReq;
import jh.exp.project.service.service.internal.ProjectMaterialStockInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/project/projectMaterialStock")
@RequiredArgsConstructor
public class InternalProjectMaterialStockController {
    private final ProjectMaterialStockInternalService service;

    @PostMapping("/list")
    public ApiResponse<SimplePageRes<ProjectMaterialStock>> list(@RequestBody SimplePageReq<Object> req) {
        req.pageDefault();
        return ApiResponse.success(service.list(req));
    }

    @GetMapping("/detail")
    public ApiResponse<ProjectMaterialStock> detail(@RequestParam Long stockId) {
        return ApiResponse.success(service.detail(stockId));
    }

    @PostMapping("/create")
    public ApiResponse<ProjectMaterialStock> create(@RequestBody @Valid ProjectMaterialStock req) {
        return ApiResponse.success(service.create(req));
    }

    @PostMapping("/update")
    public ApiResponse<ProjectMaterialStock> update(@RequestBody @Valid ProjectMaterialStock req) {
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

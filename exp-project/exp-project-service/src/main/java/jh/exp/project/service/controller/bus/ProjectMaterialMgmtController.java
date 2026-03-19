package jh.exp.project.service.controller.bus;

import jakarta.validation.Valid;
import jh.exp.common.core.annotation.RequiresMenuLevel;
import jh.exp.common.core.api.ApiResponse;
import jh.exp.project.core.entity.req.ProjectMaterialCreateReq;
import jh.exp.project.core.entity.req.ProjectMaterialDeleteReq;
import jh.exp.project.core.entity.req.ProjectMaterialInboundReq;
import jh.exp.project.core.entity.req.ProjectMaterialOutboundReq;
import jh.exp.project.core.entity.req.ProjectMaterialUpdateReq;
import jh.exp.project.core.entity.res.ProjectMaterialDetailRes;
import jh.exp.project.core.entity.res.ProjectMaterialRes;
import jh.exp.project.service.service.bus.ProjectMaterialMgmtInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/projectMgmt/material")
@RequiredArgsConstructor
@RequiresMenuLevel(code = "project:materials", level = 1)
public class ProjectMaterialMgmtController {
    private final ProjectMaterialMgmtInternalService service;

    @GetMapping("/detail")
    public ApiResponse<ProjectMaterialDetailRes> detail(@RequestParam Long projectId) {
        return ApiResponse.success(service.detail(projectId));
    }

    @PostMapping("/create")
    @RequiresMenuLevel(code = "project:materials", level = 2)
    public ApiResponse<ProjectMaterialRes> create(@RequestBody @Valid ProjectMaterialCreateReq req) {
        return ApiResponse.success(service.create(req));
    }

    @PostMapping("/update")
    @RequiresMenuLevel(code = "project:materials", level = 2)
    public ApiResponse<ProjectMaterialRes> update(@RequestBody @Valid ProjectMaterialUpdateReq req) {
        return ApiResponse.success(service.update(req));
    }

    @PostMapping("/delete")
    @RequiresMenuLevel(code = "project:materials", level = 3)
    public ApiResponse<Void> delete(@RequestBody @Valid ProjectMaterialDeleteReq req) {
        service.delete(req);
        return ApiResponse.success(null);
    }

    @PostMapping("/inbound")
    @RequiresMenuLevel(code = "project:materials", level = 2)
    public ApiResponse<ProjectMaterialRes> inbound(@RequestBody @Valid ProjectMaterialInboundReq req) {
        return ApiResponse.success(service.inbound(req));
    }

    @PostMapping("/outbound")
    @RequiresMenuLevel(code = "project:materials", level = 2)
    public ApiResponse<ProjectMaterialRes> outbound(@RequestBody @Valid ProjectMaterialOutboundReq req) {
        return ApiResponse.success(service.outbound(req));
    }
}

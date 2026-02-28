package jh.exp.project.service.controller.bus;

import jakarta.validation.Valid;
import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.project.core.entity.ProjectRoleCfg;
import jh.exp.project.core.entity.req.BatchDeleteByIdsReq;
import jh.exp.project.core.entity.req.DeleteByIdReq;
import jh.exp.project.service.service.bus.ProjectRoleCfgInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/projectRoleCfg")
@RequiredArgsConstructor
public class ProjectRoleCfgController {
    private final ProjectRoleCfgInternalService service;

    @PostMapping("/list")
    public ApiResponse<SimplePageRes<ProjectRoleCfg>> list(@RequestBody SimplePageReq<Object> req) {
        req.pageDefault();
        return ApiResponse.success(service.list(req));
    }

    @GetMapping("/detail")
    public ApiResponse<ProjectRoleCfg> detail(@RequestParam Long cfgId) {
        return ApiResponse.success(service.detail(cfgId));
    }

    @PostMapping("/create")
    public ApiResponse<ProjectRoleCfg> create(@RequestBody @Valid ProjectRoleCfg req) {
        return ApiResponse.success(service.create(req));
    }

    @PostMapping("/update")
    public ApiResponse<ProjectRoleCfg> update(@RequestBody @Valid ProjectRoleCfg req) {
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

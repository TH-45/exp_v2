package jh.exp.project.service.controller.bus;

import jakarta.validation.Valid;
import jh.exp.common.core.annotation.RequiresMenuLevel;
import jh.exp.common.core.api.ApiResponse;
import jh.exp.project.core.entity.req.ProjectMemberCreateReq;
import jh.exp.project.core.entity.req.ProjectMemberDeleteReq;
import jh.exp.project.core.entity.req.ProjectMemberUpdateReq;
import jh.exp.project.core.entity.res.ProjectMemberRes;
import jh.exp.project.service.service.bus.ProjectMemberMgmtInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/projectMgmt/member")
@RequiredArgsConstructor
@RequiresMenuLevel(code = "project:members", level = 1)
public class ProjectMemberMgmtController {
    private final ProjectMemberMgmtInternalService service;

    @GetMapping("/list")
    public ApiResponse<List<ProjectMemberRes>> list(@RequestParam Long projectId) {
        return ApiResponse.success(service.listByProjectId(projectId));
    }

    @PostMapping("/create")
    @RequiresMenuLevel(code = "project:members", level = 2)
    public ApiResponse<ProjectMemberRes> create(@RequestBody @Valid ProjectMemberCreateReq req) {
        return ApiResponse.success(service.create(req));
    }

    @PostMapping("/update")
    @RequiresMenuLevel(code = "project:members", level = 2)
    public ApiResponse<ProjectMemberRes> update(@RequestBody @Valid ProjectMemberUpdateReq req) {
        return ApiResponse.success(service.update(req));
    }

    @PostMapping("/delete")
    @RequiresMenuLevel(code = "project:members", level = 3)
    public ApiResponse<Void> delete(@RequestBody @Valid ProjectMemberDeleteReq req) {
        service.delete(req);
        return ApiResponse.success(null);
    }
}

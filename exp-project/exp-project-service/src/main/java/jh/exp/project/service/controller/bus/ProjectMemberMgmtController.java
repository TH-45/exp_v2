package jh.exp.project.service.controller.bus;

import jakarta.validation.Valid;
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
public class ProjectMemberMgmtController {
    private final ProjectMemberMgmtInternalService service;

    @GetMapping("/list")
    public ApiResponse<List<ProjectMemberRes>> list(@RequestParam Long projectId) {
        return ApiResponse.success(service.listByProjectId(projectId));
    }

    @PostMapping("/create")
    public ApiResponse<ProjectMemberRes> create(@RequestBody @Valid ProjectMemberCreateReq req) {
        return ApiResponse.success(service.create(req));
    }

    @PostMapping("/update")
    public ApiResponse<ProjectMemberRes> update(@RequestBody @Valid ProjectMemberUpdateReq req) {
        return ApiResponse.success(service.update(req));
    }

    @PostMapping("/delete")
    public ApiResponse<Void> delete(@RequestBody @Valid ProjectMemberDeleteReq req) {
        service.delete(req);
        return ApiResponse.success(null);
    }
}

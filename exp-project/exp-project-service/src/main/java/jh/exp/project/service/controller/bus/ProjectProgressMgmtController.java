package jh.exp.project.service.controller.bus;

import jakarta.validation.Valid;
import jh.exp.common.core.annotation.RequiresMenuLevel;
import jh.exp.common.core.api.ApiResponse;
import jh.exp.project.core.entity.req.ProjectMilestoneCreateReq;
import jh.exp.project.core.entity.req.ProjectMilestoneDeleteReq;
import jh.exp.project.core.entity.req.ProjectMilestoneProgressUpdateReq;
import jh.exp.project.core.entity.req.ProjectMilestoneUpdateReq;
import jh.exp.project.core.entity.res.ProjectMilestoneRes;
import jh.exp.project.core.entity.res.ProjectProgressRes;
import jh.exp.project.service.service.bus.ProjectProgressMgmtInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/projectMgmt/progress")
@RequiredArgsConstructor
@RequiresMenuLevel(code = "project:progress", level = 1)
public class ProjectProgressMgmtController {
    private final ProjectProgressMgmtInternalService service;

    @GetMapping("/detail")
    public ApiResponse<ProjectProgressRes> detail(@RequestParam Long projectId) {
        return ApiResponse.success(service.detail(projectId));
    }

    @PostMapping("/milestone/create")
    @RequiresMenuLevel(code = "project:progress", level = 2)
    public ApiResponse<ProjectMilestoneRes> createMilestone(@RequestBody @Valid ProjectMilestoneCreateReq req) {
        return ApiResponse.success(service.createMilestone(req));
    }

    @PostMapping("/milestone/update")
    @RequiresMenuLevel(code = "project:progress", level = 2)
    public ApiResponse<ProjectMilestoneRes> updateMilestone(@RequestBody @Valid ProjectMilestoneUpdateReq req) {
        return ApiResponse.success(service.updateMilestone(req));
    }

    @PostMapping("/milestone/delete")
    @RequiresMenuLevel(code = "project:progress", level = 3)
    public ApiResponse<Void> deleteMilestone(@RequestBody @Valid ProjectMilestoneDeleteReq req) {
        service.deleteMilestone(req);
        return ApiResponse.success(null);
    }

    @PostMapping("/milestone/updateProgress")
    @RequiresMenuLevel(code = "project:progress", level = 2)
    public ApiResponse<ProjectMilestoneRes> updateProgress(@RequestBody @Valid ProjectMilestoneProgressUpdateReq req) {
        return ApiResponse.success(service.updateProgress(req));
    }
}

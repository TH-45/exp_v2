package jh.exp.project.service.controller.bus;

import jakarta.validation.Valid;
import jh.exp.common.core.annotation.RequiresMenuLevel;
import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.project.core.entity.Project;
import jh.exp.project.core.entity.req.BatchDeleteByIdsReq;
import jh.exp.project.core.entity.req.DeleteByIdReq;
import jh.exp.project.core.entity.res.ProjectStatsRes;
import jh.exp.project.service.service.bus.ProjectInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/project")
@RequiredArgsConstructor
@RequiresMenuLevel(code = "project:management", level = 1)
public class ProjectController {
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

    /**
     * 批量获取项目信息
     * @param projectIds
     * @return
     */
    @PostMapping("/batchGetProjectByIds")
    public ApiResponse<Map<Long,Project>> batchGetProjectByIds(@RequestBody List<Long> projectIds){
        return service.batchGetProjectByIds(projectIds);
    }

    @PostMapping("/create")
    @RequiresMenuLevel(code = "project:management", level = 2)
    public ApiResponse<Project> create(@RequestBody @Valid Project req) {
        return ApiResponse.success(service.create(req));
    }

    @PostMapping("/update")
    @RequiresMenuLevel(code = "project:management", level = 2)
    public ApiResponse<Project> update(@RequestBody @Valid Project req) {
        return ApiResponse.success(service.update(req));
    }

    @PostMapping("/delete")
    @RequiresMenuLevel(code = "project:management", level = 3)
    public ApiResponse<Void> delete(@RequestBody @Valid DeleteByIdReq req) {
        service.delete(req);
        return ApiResponse.success(null);
    }

    @PostMapping("/batchDelete")
    @RequiresMenuLevel(code = "project:management", level = 3)
    public ApiResponse<Void> batchDelete(@RequestBody @Valid BatchDeleteByIdsReq req) {
        service.batchDelete(req);
        return ApiResponse.success(null);
    }

    @GetMapping("/stats")
    public ApiResponse<ProjectStatsRes> stats() {
        return ApiResponse.success(service.stats());
    }
}

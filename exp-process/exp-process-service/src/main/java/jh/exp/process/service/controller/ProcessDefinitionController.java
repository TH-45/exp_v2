package jh.exp.process.service.controller;

import jakarta.validation.Valid;
import jh.exp.common.core.annotation.RequiresMenuLevel;
import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.process.core.entity.req.NodeSaveReq;
import jh.exp.process.core.entity.req.NodeSortReq;
import jh.exp.process.core.entity.req.ProcessDefinitionCopyReq;
import jh.exp.process.core.entity.req.ProcessDefinitionQueryReq;
import jh.exp.process.core.entity.req.ProcessDefinitionSaveReq;
import jh.exp.process.core.entity.res.NodeRes;
import jh.exp.process.core.entity.res.ProcessDefinitionDetailRes;
import jh.exp.process.core.entity.res.ProcessDefinitionListRes;
import jh.exp.process.service.service.ProcessDefinitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/definition")
@RequiredArgsConstructor
@RequiresMenuLevel(code = "process:definition", level = 1)
public class ProcessDefinitionController {

    private final ProcessDefinitionService processDefinitionService;

    @PostMapping("/save")
    @RequiresMenuLevel(code = "process:definition", level = 2)
    public ApiResponse<ProcessDefinitionDetailRes> save(@RequestBody @Valid ProcessDefinitionSaveReq req) {
        return ApiResponse.success(processDefinitionService.saveDefinition(req));
    }

    @PostMapping("/list")
    public ApiResponse<SimplePageRes<ProcessDefinitionListRes>> list(@RequestBody SimplePageReq<ProcessDefinitionQueryReq> req) {
        req.pageDefault();
        return ApiResponse.success(processDefinitionService.listDefinitions(req));
    }

    @GetMapping("/detail")
    public ApiResponse<ProcessDefinitionDetailRes> detail(@RequestParam Long procDefId) {
        return ApiResponse.success(processDefinitionService.detail(procDefId));
    }
    //根据流程编号获取流程定义详情
    @GetMapping("/get")
    public ApiResponse<ProcessDefinitionDetailRes> get(@RequestParam String procDefCode) {
        return ApiResponse.success(processDefinitionService.detailByCode(procDefCode));
    }

    @PostMapping("/activate")
    @RequiresMenuLevel(code = "process:definition", level = 2)
    public ApiResponse<Void> activate(@RequestParam Long procDefId, @RequestParam Integer isActive) {
        processDefinitionService.setActive(procDefId, isActive);
        return ApiResponse.success(null);
    }

    /** 删除流程定义（无实例时方可删除） */
    @PostMapping("/delete")
    @RequiresMenuLevel(code = "process:definition", level = 3)
    public ApiResponse<Void> delete(@RequestParam Long procDefId) {
        processDefinitionService.deleteDefinition(procDefId);
        return ApiResponse.success(null);
    }

    @PostMapping("/copy")
    @RequiresMenuLevel(code = "process:definition", level = 2)
    public ApiResponse<ProcessDefinitionDetailRes> copy(@RequestBody @Valid ProcessDefinitionCopyReq req) {
        return ApiResponse.success(processDefinitionService.copy(req));
    }

    @PostMapping("/node/save")
    @RequiresMenuLevel(code = "process:definition", level = 2)
    public ApiResponse<NodeRes> saveNode(@RequestBody @Valid NodeSaveReq req) {
        return ApiResponse.success(processDefinitionService.saveNode(req));
    }

    @PostMapping("/node/delete")
    @RequiresMenuLevel(code = "process:definition", level = 2)
    public ApiResponse<Void> deleteNode(@RequestParam Long nodeId) {
        processDefinitionService.deleteNode(nodeId);
        return ApiResponse.success(null);
    }

    @PostMapping("/node/sort")
    @RequiresMenuLevel(code = "process:definition", level = 2)
    public ApiResponse<List<NodeRes>> sortNode(@RequestBody @Valid NodeSortReq req) {
        return ApiResponse.success(processDefinitionService.sortNode(req));
    }
}

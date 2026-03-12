package jh.exp.bid.contract.service.controller.bus;

import jh.exp.bid.contract.core.entity.req.*;
import jh.exp.bid.contract.core.entity.res.ContractDetailRes;
import jh.exp.bid.contract.core.entity.res.ContractListRes;
import jh.exp.bid.contract.service.service.bus.ContractService;
import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 合同管理控制器
 */
@RestController
@RequestMapping("/contract")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    /**
     * 分页查询合同列表
     */
    @PostMapping("/list")
    public ApiResponse<SimplePageRes<ContractListRes>> list(@RequestBody SimplePageReq<QueryContractReq> req) {
        req.pageDefault();
        SimplePageRes<ContractListRes> result = contractService.queryContractList(req);
        return ApiResponse.success(result);
    }

    /**
     * 根据ID查询合同详情
     */
    @GetMapping("/detail")
    public ApiResponse<ContractDetailRes> detail(@RequestParam Long contractId) {
        return ApiResponse.success(contractService.getContractById(contractId));
    }

    /**
     * 创建合同
     */
    @PostMapping("/create")
    public ApiResponse<ContractDetailRes> create(@RequestBody @Valid CreateContractReq req) {
        return ApiResponse.success(contractService.createContract(req));
    }

    /**
     * 更新合同
     */
    @PostMapping("/update")
    public ApiResponse<ContractDetailRes> update(@RequestBody @Valid UpdateContractReq req) {
        return ApiResponse.success(contractService.updateContract(req));
    }

    /**
     * 删除合同
     */
    @PostMapping("/delete")
    public ApiResponse<Void> delete(@RequestParam Long contractId) {
        contractService.deleteContract(contractId);
        return ApiResponse.success(null);
    }

    /**
     * 提交审批（内部调用流程创建，保留兼容）
     */
    @PostMapping("/submitForApproval")
    public ApiResponse<Long> submitForApproval(@RequestBody @Valid SubmitContractApprovalReq req) {
        Long instanceId = contractService.submitForApproval(req);
        return ApiResponse.success(instanceId);
    }

    /**
     * 流程创建成功后，将合同状态更新为审核中（供前端在调用统一流程创建接口成功后调用）
     */
    @PostMapping("/updateStatusAfterProcessStart")
    public ApiResponse<Void> updateStatusAfterProcessStart(@RequestParam Long contractId) {
        contractService.updateStatusAfterProcessStart(contractId);
        return ApiResponse.success(null);
    }

    /**
     * 供流程引擎回调：按流程结果更新合同状态
     * 仅限内部服务调用，生产环境建议增加鉴权
     */
    @PostMapping("/updateStatusByProcessResult")
    public ApiResponse<Void> updateStatusByProcessResult(@RequestParam Long contractId,
                                                        @RequestParam String instanceStatus) {
        contractService.updateStatusByProcessResult(contractId, instanceStatus);
        return ApiResponse.success(null);
    }

    /**
     * 合同签订/不签订（统一接口）
     * 拟签阶段：签订→正常归档；不签订+变更→返回起草；不签订+不变更→异常归档
     */
    @PostMapping("/sign")
    public ApiResponse<Void> sign(@RequestBody @Valid SignContractReq req) {
        contractService.signContract(req);
        return ApiResponse.success(null);
    }
}

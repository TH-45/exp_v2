package jh.exp.bid.contract.client.api;

import feign.Body;
import jh.exp.bid.contract.core.entity.req.*;
import jh.exp.bid.contract.core.entity.res.ContractDetailRes;
import jh.exp.bid.contract.core.entity.res.ContractListRes;
import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

/**
 * 合同服务客户端接口，供其他服务（如流程服务）调用合同相关 API
 */
@HttpExchange("/contract")
public interface ContractClient {

    /**
     * 分页查询合同列表
     *
     * @param req 分页查询请求
     * @return 合同列表分页结果
     */
    @PostExchange("/list")
    ApiResponse<SimplePageRes<ContractListRes>> list(@RequestBody SimplePageReq<QueryContractReq> req);

    /**
     * 根据ID查询合同详情
     *
     * @param contractId 合同ID
     * @return 合同详情
     */
    @GetExchange("/detail")
    ApiResponse<ContractDetailRes> detail(@RequestParam("contractId") Long contractId);

    /**
     * 更新合同
     *
     * @param req 更新合同请求
     * @return 更新后的合同详情
     */
    @PostExchange("/update")
    ApiResponse<ContractDetailRes> update(@RequestBody UpdateContractReq req);

    /**
     * 更新合同状态
     * 供流程引擎或内部服务调用，按流程结果更新合同状态（如驳回→起草，通过→拟签）
     *
     * @param req 更新合同状态请求（contractId、status）
     * @return 操作结果
     */
    @PostExchange("/updateStatus")
    ApiResponse<Void> updateStatus(UpdateContractStatusReq req);

    /**
     * 删除合同
     *
     * @param contractId 合同ID
     * @return 删除结果
     */
    @PostExchange("/delete")
    ApiResponse<Void> delete(@RequestParam("contractId") Long contractId);

    /**
     * 创建合同业务（创建合同并返回流程实例ID，供流程服务使用）
     *
     * @param req 创建合同请求
     * @return 流程实例ID
     */
    @PostExchange("/createContractBusiness")
    ApiResponse<Long> createContractBusiness(@RequestBody CreateContractReq req);

    /**
     * 流程创建成功后，将合同状态更新为审核中
     * 供前端在调用统一流程创建接口成功后调用
     *
     * @param contractId 合同ID
     * @return 操作结果
     */
    @PostExchange("/updateStatusAfterProcessStart")
    ApiResponse<Void> updateStatusAfterProcessStart(@RequestParam("contractId") Long contractId);

    /**
     * 供流程引擎回调：按流程结果更新合同状态
     * 仅限内部服务调用
     *
     * @param contractId     合同ID
     * @param instanceStatus 流程实例状态
     * @return 操作结果
     */
    @PostExchange("/updateStatusByProcessResult")
    ApiResponse<Void> updateStatusByProcessResult(
            @RequestParam("contractId") Long contractId,
            @RequestParam("instanceStatus") String instanceStatus);

    /**
     * 合同签订/不签订（统一接口）
     * 拟签阶段：签订→正常归档；不签订+变更→返回起草；不签订+不变更→异常归档
     *
     * @param req 签订请求
     * @return 操作结果
     */
    @PostExchange("/sign")
    ApiResponse<Void> sign(@RequestBody SignContractReq req);
}

package jh.exp.bid.contract.service.service.bus;

import jh.exp.bid.contract.core.entity.req.*;
import jh.exp.bid.contract.core.entity.res.ContractDetailRes;
import jh.exp.bid.contract.core.entity.res.ContractListRes;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;

/**
 * 合同服务接口
 */
public interface ContractService {

    SimplePageRes<ContractListRes> queryContractList(SimplePageReq<QueryContractReq> req);

    ContractDetailRes getContractById(Long contractId);

    ContractDetailRes createContract(CreateContractReq req);

    ContractDetailRes updateContract(UpdateContractReq req);

    void deleteContract(Long contractId);

    /**
     * 提交审批：将起草中的合同提交至流程引擎，合同状态变更为审核中
     * @return 流程实例ID
     */
    Long createContractBusiness(CreateContractReq req);

    /**
     * 流程创建成功后，将合同状态更新为审核中（供前端调用统一流程创建接口成功后调用）
     */
    void updateStatusAfterProcessStart(Long contractId);

    /**
     * 供流程引擎回调：更新合同状态（驳回→起草，通过→拟签）
     */
    void updateStatusByProcess(Long contractId, String status);

    /**
     * 供流程引擎回调：按流程结果更新（COMPLETED→拟签，REJECTED→起草）
     */
    void updateStatusByProcessResult(Long contractId, String instanceStatus);

    /**
     * 合同签订/不签订（统一接口）
     * 签订→正常归档；不签订+变更→返回起草；不签订+不变更→异常归档
     */
    void signContract(SignContractReq req);
}

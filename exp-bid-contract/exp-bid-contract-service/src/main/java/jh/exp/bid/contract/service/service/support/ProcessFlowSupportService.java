//package jh.exp.bid.contract.service.service.support;
//
//import jh.exp.common.core.req.SimplePageReq;
//import jh.exp.process.client.api.ProcessApprovalClient;
//import jh.exp.process.client.api.ProcessDefinitionClient;
//import jh.exp.process.core.entity.req.ProcessDefinitionQueryReq;
//import jh.exp.process.core.entity.req.StartProcessReq;
//import jh.exp.process.core.entity.res.ProcessDefinitionListRes;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.util.CollectionUtils;
//
///**
// * 流程服务调用封装，通过 exp-process-client 调用流程服务
// */
//@Service
//@Slf4j
//@RequiredArgsConstructor
//public class ProcessFlowSupportService {
//
//    private final ProcessApprovalClient processApprovalClient;
//    private final ProcessDefinitionClient processDefinitionClient;
//
//    /**
//     * 发起流程
//     *
//     * @param busType   业务类型，如 contract（当 procDefId 和 procCode 都为空时，按 busType 取第一个启用流程）
//     * @param busId    业务主键
//
//     * @return 流程实例ID
//     */
//    public Long startProcess(String busType, String busId) {
//
//        if (useProcDefId == null && (useProcCode == null || useProcCode.isEmpty())) {
//            var def = resolveDefaultProcess(busType);
//            if (def != null) {
//                useProcDefId = def.procDefId;
//                useProcCode = def.procCode;
//            }
//        }
//        if (useProcDefId == null && (useProcCode == null || useProcCode.isEmpty())) {
//            throw new RuntimeException("未找到合同审批流程，请在流程管理中配置 busType=contract 的流程");
//        }
//        return doStartProcess(busId, useProcDefId, useProcCode);
//    }
//
//    private ProcDef resolveDefaultProcess(String busType) {
//        ProcessDefinitionQueryReq queryParam = new ProcessDefinitionQueryReq();
//        queryParam.setBusType(busType);
//        queryParam.setIsActive(1);
//        SimplePageReq<ProcessDefinitionQueryReq> req = new SimplePageReq<>(1, 10, null, queryParam);
//        try {
//            var resp = processDefinitionClient.list(req);
//            if (resp != null && resp.isSuccess() && resp.getData() != null
//                    && !CollectionUtils.isEmpty(resp.getData().getList())) {
//                ProcessDefinitionListRes first = resp.getData().getList().get(0);
//                return new ProcDef(first.getProcDefId(), first.getProcCode());
//            }
//        } catch (Exception e) {
//            log.warn("查询流程定义失败", e);
//        }
//        return null;
//    }
//
//    private Long doStartProcess(String busId, Long procDefId, String procCode) {
//        StartProcessReq req = new StartProcessReq();
//        req.setBusId(busId);
//        req.setProcDefId(procDefId);
//        req.setProcCode(procCode);
//        var resp = processApprovalClient.start(req);
//        if (resp == null || !resp.isSuccess()) {
//            throw new RuntimeException("发起审批失败：" + (resp != null ? resp.getMessage() : "未返回结果"));
//        }
//        Long instanceId = resp.getData();
//        if (instanceId == null) {
//            throw new RuntimeException("发起审批失败：未返回流程实例ID");
//        }
//        return instanceId;
//    }
//
//    private record ProcDef(Long procDefId, String procCode) {}
//}

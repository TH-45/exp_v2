package jh.exp.process.service.service;

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

import java.util.List;

public interface ProcessDefinitionService {
    ProcessDefinitionDetailRes saveDefinition(ProcessDefinitionSaveReq req);

    SimplePageRes<ProcessDefinitionListRes> listDefinitions(SimplePageReq<ProcessDefinitionQueryReq> req);

    ProcessDefinitionDetailRes detail(Long procDefId);

    void setActive(Long procDefId, Integer isActive);

    /** 删除流程定义（无实例时方可删除，级联删除节点） */
    void deleteDefinition(Long procDefId);

    ProcessDefinitionDetailRes copy(ProcessDefinitionCopyReq req);

    NodeRes saveNode(NodeSaveReq req);

    void deleteNode(Long nodeId);

    List<NodeRes> sortNode(NodeSortReq req);

    ProcessDefinitionDetailRes detailByCode(String procDefCode);
}

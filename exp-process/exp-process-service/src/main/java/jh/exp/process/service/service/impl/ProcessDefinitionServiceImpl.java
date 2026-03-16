package jh.exp.process.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jh.exp.common.core.auth.CurrentUserHolder;
import jh.exp.common.core.auth.dto.CurrentUser;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.process.core.entity.WfNodeDefinition;
import jh.exp.process.core.entity.WfProcessDefinition;
import jh.exp.process.core.entity.req.NodeSaveReq;
import jh.exp.process.core.entity.req.NodeSortReq;
import jh.exp.process.core.entity.req.ProcessDefinitionCopyReq;
import jh.exp.process.core.entity.req.ProcessDefinitionQueryReq;
import jh.exp.process.core.entity.req.ProcessDefinitionSaveReq;
import jh.exp.process.core.entity.res.NodeRes;
import jh.exp.process.core.entity.res.ProcessDefinitionDetailRes;
import jh.exp.process.core.entity.res.ProcessDefinitionListRes;
import jh.exp.process.core.mapper.WfNodeDefinitionMapper;
import jh.exp.process.core.mapper.WfProcessDefinitionMapper;
import jh.exp.process.service.service.ProcessDefinitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProcessDefinitionServiceImpl implements ProcessDefinitionService {

    private final WfProcessDefinitionMapper processDefinitionMapper;
    private final WfNodeDefinitionMapper nodeDefinitionMapper;

    @Override
    @Transactional
    public ProcessDefinitionDetailRes saveDefinition(ProcessDefinitionSaveReq req) {
        LocalDateTime now = LocalDateTime.now();
        CurrentUser currentUser = CurrentUserHolder.get();
        Long userId = currentUser == null ? 0L : currentUser.getUserId();

        WfProcessDefinition entity;
        if (req.getProcDefId() == null) {
            entity = new WfProcessDefinition();
            BeanUtils.copyProperties(req, entity);
            entity.setIsActive(req.getIsActive() == null ? 1 : req.getIsActive());
            entity.setVersion(req.getVersion() == null ? 1 : req.getVersion());
            entity.setCreatedBy(userId);
            entity.setCreatedTime(now);
            entity.setUpdatedTime(now);
            processDefinitionMapper.insert(entity);
        } else {
            entity = processDefinitionMapper.selectById(req.getProcDefId());
            if (entity == null) {
                throw new RuntimeException("流程定义不存在");
            }
            entity.setProcCode(req.getProcCode());
            entity.setProcName(req.getProcName());
            entity.setBusType(req.getBusType());
            entity.setIsActive(req.getIsActive() == null ? entity.getIsActive() : req.getIsActive());
            entity.setVersion(req.getVersion() == null ? entity.getVersion() : req.getVersion());
            entity.setRemark(req.getRemark());
            entity.setUpdatedTime(now);
            processDefinitionMapper.updateById(entity);
        }
        return detail(entity.getProcDefId());
    }

    @Override
    public SimplePageRes<ProcessDefinitionListRes> listDefinitions(SimplePageReq<ProcessDefinitionQueryReq> req) {
        req.pageDefault();
        ProcessDefinitionQueryReq query = req.getQueryParam() == null ? new ProcessDefinitionQueryReq() : req.getQueryParam();
        Page<WfProcessDefinition> page = new Page<>(req.getPageNum(), req.getPageSize());

        LambdaQueryWrapper<WfProcessDefinition> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(query.getProcCode() != null && !query.getProcCode().isBlank(), WfProcessDefinition::getProcCode, query.getProcCode())
                .like(query.getProcName() != null && !query.getProcName().isBlank(), WfProcessDefinition::getProcName, query.getProcName())
                .eq(query.getBusType() != null && !query.getBusType().isBlank(), WfProcessDefinition::getBusType, query.getBusType())
                .eq(query.getIsActive() != null, WfProcessDefinition::getIsActive, query.getIsActive())
                .orderByDesc(WfProcessDefinition::getUpdatedTime);

        Page<WfProcessDefinition> result = processDefinitionMapper.selectPage(page, wrapper);
        List<ProcessDefinitionListRes> list = result.getRecords().stream().map(item -> {
            ProcessDefinitionListRes res = new ProcessDefinitionListRes();
            BeanUtils.copyProperties(item, res);
            return res;
        }).toList();

        return new SimplePageRes<>(result.getTotal(), result.getCurrent(), result.getSize(), list);
    }

    @Override
    public ProcessDefinitionDetailRes detail(Long procDefId) {
        WfProcessDefinition definition = processDefinitionMapper.selectById(procDefId);
        if (definition == null) {
            throw new RuntimeException("流程定义不存在");
        }
        ProcessDefinitionDetailRes res = new ProcessDefinitionDetailRes();
        BeanUtils.copyProperties(definition, res);
        List<NodeRes> nodes = listNodes(procDefId);
        res.setNodes(nodes);
        return res;
    }

    @Override
    public void setActive(Long procDefId, Integer isActive) {
        WfProcessDefinition entity = processDefinitionMapper.selectById(procDefId);
        if (entity == null) {
            throw new RuntimeException("流程定义不存在");
        }
        entity.setIsActive(isActive == null ? 0 : isActive);
        entity.setUpdatedTime(LocalDateTime.now());
        processDefinitionMapper.updateById(entity);
    }

    @Override
    @Transactional
    public ProcessDefinitionDetailRes copy(ProcessDefinitionCopyReq req) {
        WfProcessDefinition source = processDefinitionMapper.selectById(req.getSourceProcDefId());
        if (source == null) {
            throw new RuntimeException("源流程不存在");
        }

        LocalDateTime now = LocalDateTime.now();
        CurrentUser currentUser = CurrentUserHolder.get();
        Long userId = currentUser == null ? 0L : currentUser.getUserId();

        WfProcessDefinition target = new WfProcessDefinition();
        target.setProcCode(req.getNewProcCode());
        target.setProcName(req.getNewProcName());
        target.setBusType(source.getBusType());
        target.setIsActive(0);
        target.setVersion((source.getVersion() == null ? 1 : source.getVersion()) + 1);
        target.setRemark("复制自流程ID:" + source.getProcDefId());
        target.setCreatedBy(userId);
        target.setCreatedTime(now);
        target.setUpdatedTime(now);
        processDefinitionMapper.insert(target);

        List<WfNodeDefinition> sourceNodes = nodeDefinitionMapper.selectList(
                new LambdaQueryWrapper<WfNodeDefinition>()
                        .eq(WfNodeDefinition::getProcDefId, source.getProcDefId())
                        .orderByAsc(WfNodeDefinition::getSortNo)
        );
        for (WfNodeDefinition sourceNode : sourceNodes) {
            WfNodeDefinition copyNode = new WfNodeDefinition();
            copyNode.setProcDefId(target.getProcDefId());
            copyNode.setNodeName(sourceNode.getNodeName());
            copyNode.setSortNo(sourceNode.getSortNo());
            copyNode.setApproveType(sourceNode.getApproveType());
            copyNode.setAssigneeType(sourceNode.getAssigneeType());
            copyNode.setAssigneeId(sourceNode.getAssigneeId());
            copyNode.setCreatedTime(now);
            copyNode.setUpdatedTime(now);
            nodeDefinitionMapper.insert(copyNode);
        }
        return detail(target.getProcDefId());
    }

    @Override
    @Transactional
    public NodeRes saveNode(NodeSaveReq req) {
        LocalDateTime now = LocalDateTime.now();
        WfNodeDefinition entity;
        if (req.getNodeId() == null) {
            entity = new WfNodeDefinition();
            BeanUtils.copyProperties(req, entity);
            entity.setCreatedTime(now);
            entity.setUpdatedTime(now);
            nodeDefinitionMapper.insert(entity);
        } else {
            entity = nodeDefinitionMapper.selectById(req.getNodeId());
            if (entity == null) {
                throw new RuntimeException("节点不存在");
            }
            entity.setNodeName(req.getNodeName());
            entity.setSortNo(req.getSortNo());
            entity.setApproveType(req.getApproveType());
            entity.setAssigneeType(req.getAssigneeType());
            entity.setAssigneeId(req.getAssigneeId());
            entity.setUpdatedTime(now);
            nodeDefinitionMapper.updateById(entity);
        }
        NodeRes res = new NodeRes();
        BeanUtils.copyProperties(entity, res);
        return res;
    }

    @Override
    public void deleteNode(Long nodeId) {
        nodeDefinitionMapper.deleteById(nodeId);
    }

    @Override
    @Transactional
    public List<NodeRes> sortNode(NodeSortReq req) {
        WfNodeDefinition current = nodeDefinitionMapper.selectById(req.getNodeId());
        if (current == null) {
            throw new RuntimeException("节点不存在");
        }
        WfNodeDefinition target = nodeDefinitionMapper.selectOne(
                new LambdaQueryWrapper<WfNodeDefinition>()
                        .eq(WfNodeDefinition::getProcDefId, current.getProcDefId())
                        .eq(WfNodeDefinition::getSortNo, req.getTargetSortNo())
        );
        if (target != null) {
            Integer tempSort = current.getSortNo();
            current.setSortNo(target.getSortNo());
            target.setSortNo(tempSort);
            current.setUpdatedTime(LocalDateTime.now());
            target.setUpdatedTime(LocalDateTime.now());
            nodeDefinitionMapper.updateById(current);
            nodeDefinitionMapper.updateById(target);
        } else {
            nodeDefinitionMapper.update(
                    null,
                    new LambdaUpdateWrapper<WfNodeDefinition>()
                            .eq(WfNodeDefinition::getNodeId, current.getNodeId())
                            .set(WfNodeDefinition::getSortNo, req.getTargetSortNo())
                            .set(WfNodeDefinition::getUpdatedTime, LocalDateTime.now())
            );
        }
        return listNodes(current.getProcDefId());
    }

    @Override
    public ProcessDefinitionDetailRes detailByCode(String procDefCode) {
        if (procDefCode == null || procDefCode.isBlank()) {
            throw new RuntimeException("流程编号不能为空");
        }
        WfProcessDefinition entity = processDefinitionMapper.selectOne(
                new LambdaQueryWrapper<WfProcessDefinition>()
                        .eq(WfProcessDefinition::getProcCode, procDefCode)
        );
        return detail(entity.getProcDefId());
    }

    private List<NodeRes> listNodes(Long procDefId) {
        return nodeDefinitionMapper.selectList(
                new LambdaQueryWrapper<WfNodeDefinition>()
                        .eq(WfNodeDefinition::getProcDefId, procDefId)
                        .orderByAsc(WfNodeDefinition::getSortNo)
        ).stream().map(item -> {
            NodeRes res = new NodeRes();
            BeanUtils.copyProperties(item, res);
            return res;
        }).toList();
    }
}

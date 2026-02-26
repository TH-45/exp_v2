package jh.exp.project.service.service.internal.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.project.core.entity.ProjectRoleCfg;
import jh.exp.project.core.entity.req.BatchDeleteByIdsReq;
import jh.exp.project.core.entity.req.DeleteByIdReq;
import jh.exp.project.core.mapper.ProjectRoleCfgMapper;
import jh.exp.project.service.service.internal.ProjectRoleCfgInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProjectRoleCfgInternalServiceImpl implements ProjectRoleCfgInternalService {
    private final ProjectRoleCfgMapper mapper;

    @Override
    public SimplePageRes<ProjectRoleCfg> list(SimplePageReq<Object> req) {
        Page<ProjectRoleCfg> page = new Page<>(req.getPageNum(), req.getPageSize());
        LambdaQueryWrapper<ProjectRoleCfg> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ProjectRoleCfg::getCfgId);
        Page<ProjectRoleCfg> result = mapper.selectPage(page, wrapper);
        return SimplePageRes.toPageRes(result, req);
    }

    @Override
    public ProjectRoleCfg detail(Long cfgId) {
        ProjectRoleCfg entity = mapper.selectById(cfgId);
        if (entity == null) {
            throw new RuntimeException("项目岗位配置不存在");
        }
        return entity;
    }

    @Override
    @Transactional
    public ProjectRoleCfg create(ProjectRoleCfg req) {
        req.setCreatedTime(LocalDateTime.now());
        req.setUpdatedTime(LocalDateTime.now());
        mapper.insert(req);
        return detail(req.getCfgId());
    }

    @Override
    @Transactional
    public ProjectRoleCfg update(ProjectRoleCfg req) {
        if (req.getCfgId() == null) {
            throw new RuntimeException("cfgId不能为空");
        }
        ProjectRoleCfg old = detail(req.getCfgId());
        BeanUtils.copyProperties(req, old, "cfgId", "createdTime");
        old.setUpdatedTime(LocalDateTime.now());
        mapper.updateById(old);
        return detail(old.getCfgId());
    }

    @Override
    @Transactional
    public void delete(DeleteByIdReq req) {
        detail(req.getId());
        mapper.deleteById(req.getId());
    }

    @Override
    @Transactional
    public void batchDelete(BatchDeleteByIdsReq req) {
        if (req.getIds() == null || req.getIds().isEmpty()) {
            return;
        }
        mapper.deleteBatchIds(req.getIds());
    }
}

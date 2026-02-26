package jh.exp.project.service.service.internal.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.project.core.entity.ProjectStaffAssign;
import jh.exp.project.core.entity.req.BatchDeleteByIdsReq;
import jh.exp.project.core.entity.req.DeleteByIdReq;
import jh.exp.project.core.mapper.ProjectStaffAssignMapper;
import jh.exp.project.service.service.internal.ProjectStaffAssignInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProjectStaffAssignInternalServiceImpl implements ProjectStaffAssignInternalService {
    private final ProjectStaffAssignMapper mapper;

    @Override
    public SimplePageRes<ProjectStaffAssign> list(SimplePageReq<Object> req) {
        Page<ProjectStaffAssign> page = new Page<>(req.getPageNum(), req.getPageSize());
        LambdaQueryWrapper<ProjectStaffAssign> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ProjectStaffAssign::getId);
        Page<ProjectStaffAssign> result = mapper.selectPage(page, wrapper);
        return SimplePageRes.toPageRes(result, req);
    }

    @Override
    public ProjectStaffAssign detail(Long id) {
        ProjectStaffAssign entity = mapper.selectById(id);
        if (entity == null) {
            throw new RuntimeException("项目人员配置不存在");
        }
        return entity;
    }

    @Override
    @Transactional
    public ProjectStaffAssign create(ProjectStaffAssign req) {
        mapper.insert(req);
        return detail(req.getId());
    }

    @Override
    @Transactional
    public ProjectStaffAssign update(ProjectStaffAssign req) {
        if (req.getId() == null) {
            throw new RuntimeException("id不能为空");
        }
        ProjectStaffAssign old = detail(req.getId());
        BeanUtils.copyProperties(req, old, "id");
        mapper.updateById(old);
        return detail(old.getId());
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

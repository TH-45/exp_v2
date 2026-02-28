package jh.exp.project.service.service.bus.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.project.core.entity.PersonProjectRel;
import jh.exp.project.core.entity.req.BatchDeleteByIdsReq;
import jh.exp.project.core.entity.req.DeleteByIdReq;
import jh.exp.project.core.mapper.PersonProjectRelMapper;
import jh.exp.project.service.service.bus.PersonProjectRelInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PersonProjectRelInternalServiceImpl implements PersonProjectRelInternalService {
    private final PersonProjectRelMapper mapper;

    @Override
    public SimplePageRes<PersonProjectRel> list(SimplePageReq<Object> req) {
        Page<PersonProjectRel> page = new Page<>(req.getPageNum(), req.getPageSize());
        LambdaQueryWrapper<PersonProjectRel> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(PersonProjectRel::getId);
        Page<PersonProjectRel> result = mapper.selectPage(page, wrapper);
        return SimplePageRes.toPageRes(result, req);
    }

    @Override
    public PersonProjectRel detail(Long id) {
        PersonProjectRel entity = mapper.selectById(id);
        if (entity == null) {
            throw new RuntimeException("人员项目关联不存在");
        }
        return entity;
    }

    @Override
    @Transactional
    public PersonProjectRel create(PersonProjectRel req) {
        mapper.insert(req);
        return detail(req.getId());
    }

    @Override
    @Transactional
    public PersonProjectRel update(PersonProjectRel req) {
        if (req.getId() == null) {
            throw new RuntimeException("id不能为空");
        }
        PersonProjectRel old = detail(req.getId());
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

package jh.exp.corp.service.service.bus.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.corp.core.entity.Qualification;
import jh.exp.corp.core.entity.req.*;
import jh.exp.corp.core.entity.res.QualificationDetailRes;
import jh.exp.corp.core.entity.res.QualificationListRes;
import jh.exp.corp.core.mapper.QualificationMapper;
import jh.exp.corp.service.service.bus.QualificationInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QualificationInternalServiceImpl implements QualificationInternalService {

    private final QualificationMapper qualificationMapper;

    @Override
    public SimplePageRes<QualificationListRes> list(SimplePageReq<QueryQualificationReq> req) {
        QueryQualificationReq query = req.getQueryParam() == null ? new QueryQualificationReq() : req.getQueryParam();
        Page<Qualification> page = new Page<>(req.getPageNum(), req.getPageSize());
        LambdaQueryWrapper<Qualification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(query.getCompanyId() != null, Qualification::getCompanyId, query.getCompanyId())
                .like(StringUtils.hasText(query.getQualificationCode()), Qualification::getQualificationCode, query.getQualificationCode())
                .like(StringUtils.hasText(query.getQualificationName()), Qualification::getQualificationName, query.getQualificationName())
                .eq(StringUtils.hasText(query.getQualificationType()), Qualification::getQualificationType, query.getQualificationType())
                .eq(StringUtils.hasText(query.getStatus()), Qualification::getStatus, query.getStatus())
                .orderByDesc(Qualification::getQualificationId);
        IPage<Qualification> result = qualificationMapper.selectPage(page, wrapper);
        List<QualificationListRes> list = result.getRecords().stream().map(this::toListRes).collect(Collectors.toList());
        SimplePageRes<QualificationListRes> pageRes = new SimplePageRes<>();
        pageRes.setList(list);
        pageRes.setPage(result.getCurrent());
        pageRes.setSize(result.getSize());
        pageRes.setTotal(result.getTotal());
        return pageRes;
    }

    @Override
    public QualificationDetailRes detail(Long qualificationId) {
        Qualification entity = qualificationMapper.selectById(qualificationId);
        if (entity == null) {
            throw new RuntimeException("资质不存在");
        }
        QualificationDetailRes res = new QualificationDetailRes();
        BeanUtils.copyProperties(entity, res);
        return res;
    }

    @Override
    @Transactional
    public QualificationDetailRes create(CreateQualificationReq req) {
        Qualification entity = new Qualification();
        BeanUtils.copyProperties(req, entity);
        entity.setCreatedTime(LocalDateTime.now());
        entity.setUpdatedTime(LocalDateTime.now());
        qualificationMapper.insert(entity);
        return detail(entity.getQualificationId());
    }

    @Override
    @Transactional
    public QualificationDetailRes update(UpdateQualificationReq req) {
        Qualification old = qualificationMapper.selectById(req.getQualificationId());
        if (old == null) {
            throw new RuntimeException("资质不存在");
        }
        BeanUtils.copyProperties(req, old);
        old.setUpdatedTime(LocalDateTime.now());
        qualificationMapper.updateById(old);
        return detail(old.getQualificationId());
    }

    @Override
    @Transactional
    public void delete(DeleteQualificationReq req) {
        if (qualificationMapper.selectById(req.getQualificationId()) == null) {
            throw new RuntimeException("资质不存在");
        }
        qualificationMapper.deleteById(req.getQualificationId());
    }

    @Override
    @Transactional
    public void batchDelete(BatchDeleteQualificationReq req) {
        if (req.getQualificationIds() == null || req.getQualificationIds().isEmpty()) {
            return;
        }
        qualificationMapper.deleteBatchIds(req.getQualificationIds());
    }

    private QualificationListRes toListRes(Qualification entity) {
        QualificationListRes res = new QualificationListRes();
        BeanUtils.copyProperties(entity, res);
        return res;
    }
}

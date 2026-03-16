package jh.exp.corp.service.service.bus.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jh.exp.common.core.exception.GatewayBizException;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.corp.core.constant.CorpErrorCode;
import jh.exp.corp.core.entity.Qualification;
import jh.exp.corp.core.entity.req.*;
import jh.exp.corp.core.entity.res.QualificationDetailRes;
import jh.exp.corp.core.entity.res.QualificationListRes;
import jh.exp.corp.core.entity.res.QualificationStatsRes;
import jh.exp.corp.core.mapper.QualificationMapper;
import jh.exp.corp.service.service.bus.QualificationInternalService;
import jh.exp.corp.service.service.support.CurrentCompanyResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QualificationInternalServiceImpl implements QualificationInternalService {

    private final QualificationMapper qualificationMapper;
    private final CurrentCompanyResolver currentCompanyResolver;

    @Override
    public SimplePageRes<QualificationListRes> list(SimplePageReq<QueryQualificationReq> req) {
        Long currentCompanyId = currentCompanyResolver.resolveCurrentCompanyId();
        QueryQualificationReq query = req.getQueryParam() == null ? new QueryQualificationReq() : req.getQueryParam();
        Page<Qualification> page = new Page<>(req.getPageNum(), req.getPageSize());
        LambdaQueryWrapper<Qualification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Qualification::getCompanyId, currentCompanyId)
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
        Long currentCompanyId = currentCompanyResolver.resolveCurrentCompanyId();
        if (entity == null || !currentCompanyId.equals(entity.getCompanyId())) {
            throw new GatewayBizException(CorpErrorCode.QUALIFICATION_NOT_FOUND, "资质不存在");
        }
        QualificationDetailRes res = new QualificationDetailRes();
        BeanUtils.copyProperties(entity, res);
        return res;
    }

    @Override
    @Transactional
    public QualificationDetailRes create(CreateQualificationReq req) {
        Long currentCompanyId = currentCompanyResolver.resolveCurrentCompanyId();
        Qualification entity = new Qualification();
        BeanUtils.copyProperties(req, entity);
        entity.setCompanyId(currentCompanyId);
        entity.setStatus(normalizeStatus(entity.getStatus(), entity.getValidTo()));
        entity.setCreatedTime(LocalDateTime.now());
        entity.setUpdatedTime(LocalDateTime.now());
        qualificationMapper.insert(entity);
        return detail(entity.getQualificationId());
    }

    @Override
    @Transactional
    public QualificationDetailRes update(UpdateQualificationReq req) {
        Qualification old = qualificationMapper.selectById(req.getQualificationId());
        Long currentCompanyId = currentCompanyResolver.resolveCurrentCompanyId();
        if (old == null || !currentCompanyId.equals(old.getCompanyId())) {
            throw new GatewayBizException(CorpErrorCode.QUALIFICATION_NOT_FOUND, "资质不存在");
        }
        Long fixedCompanyId = old.getCompanyId();
        BeanUtils.copyProperties(req, old);
        old.setCompanyId(fixedCompanyId);
        old.setStatus(normalizeStatus(old.getStatus(), old.getValidTo()));
        old.setUpdatedTime(LocalDateTime.now());
        qualificationMapper.updateById(old);
        return detail(old.getQualificationId());
    }

    @Override
    @Transactional
    public void delete(DeleteQualificationReq req) {
        Qualification entity = qualificationMapper.selectById(req.getQualificationId());
        Long currentCompanyId = currentCompanyResolver.resolveCurrentCompanyId();
        if (entity == null || !currentCompanyId.equals(entity.getCompanyId())) {
            throw new GatewayBizException(CorpErrorCode.QUALIFICATION_NOT_FOUND, "资质不存在");
        }
        qualificationMapper.deleteById(req.getQualificationId());
    }

    @Override
    @Transactional
    public void batchDelete(BatchDeleteQualificationReq req) {
        if (req.getQualificationIds() == null || req.getQualificationIds().isEmpty()) {
            return;
        }
        Long currentCompanyId = currentCompanyResolver.resolveCurrentCompanyId();
        LambdaQueryWrapper<Qualification> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Qualification::getQualificationId, req.getQualificationIds())
                .eq(Qualification::getCompanyId, currentCompanyId);
        qualificationMapper.delete(wrapper);
    }

    @Override
    public QualificationStatsRes stats() {
        Long currentCompanyId = currentCompanyResolver.resolveCurrentCompanyId();
        LambdaQueryWrapper<Qualification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Qualification::getCompanyId, currentCompanyId);
        List<Qualification> qualifications = qualificationMapper.selectList(wrapper);
        long valid = qualifications.stream().filter(item -> "VALID".equals(item.getStatus())).count();
        long expiring = qualifications.stream().filter(item -> "WILL_EXPIRE".equals(item.getStatus())).count();
        long expired = qualifications.stream().filter(item -> "EXPIRED".equals(item.getStatus())).count();
        QualificationStatsRes res = new QualificationStatsRes();
        res.setValid(valid);
        res.setExpiring(expiring);
        res.setExpired(expired);
        return res;
    }

    private QualificationListRes toListRes(Qualification entity) {
        QualificationListRes res = new QualificationListRes();
        BeanUtils.copyProperties(entity, res);
        return res;
    }

    private String normalizeStatus(String status, LocalDate validTo) {
        if (StringUtils.hasText(status)) {
            return status;
        }
        if (validTo == null) {
            return "VALID";
        }
        LocalDate today = LocalDate.now();
        if (validTo.isBefore(today)) {
            return "EXPIRED";
        }
        if (!validTo.isAfter(today.plusDays(30))) {
            return "WILL_EXPIRE";
        }
        return "VALID";
    }
}

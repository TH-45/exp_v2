package jh.exp.corp.service.service.bus.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jh.exp.common.core.exception.GatewayBizException;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.corp.core.constant.CorpErrorCode;
import jh.exp.corp.core.entity.CompanyContact;
import jh.exp.corp.core.entity.req.*;
import jh.exp.corp.core.entity.res.CompanyContactDetailRes;
import jh.exp.corp.core.entity.res.CompanyContactListRes;
import jh.exp.corp.core.mapper.CompanyContactMapper;
import jh.exp.corp.service.service.bus.CompanyContactInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompanyContactInternalServiceImpl implements CompanyContactInternalService {

    private final CompanyContactMapper companyContactMapper;

    @Override
    public SimplePageRes<CompanyContactListRes> list(SimplePageReq<QueryCompanyContactReq> req) {
        QueryCompanyContactReq query = req.getQueryParam() == null ? new QueryCompanyContactReq() : req.getQueryParam();
        Page<CompanyContact> page = new Page<>(req.getPageNum(), req.getPageSize());
        LambdaQueryWrapper<CompanyContact> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(query.getCompanyId() != null, CompanyContact::getCompanyId, query.getCompanyId())
                .like(StringUtils.hasText(query.getContactName()), CompanyContact::getContactName, query.getContactName())
                .like(StringUtils.hasText(query.getMobile()), CompanyContact::getMobile, query.getMobile())
                .eq(query.getIsPrimary() != null, CompanyContact::getIsPrimary, query.getIsPrimary())
                .eq(StringUtils.hasText(query.getStatus()), CompanyContact::getStatus, query.getStatus())
                .orderByDesc(CompanyContact::getContactId);
        IPage<CompanyContact> result = companyContactMapper.selectPage(page, wrapper);
        List<CompanyContactListRes> list = result.getRecords().stream().map(this::toListRes).collect(Collectors.toList());
        SimplePageRes<CompanyContactListRes> pageRes = new SimplePageRes<>();
        pageRes.setList(list);
        pageRes.setPage(result.getCurrent());
        pageRes.setSize(result.getSize());
        pageRes.setTotal(result.getTotal());
        return pageRes;
    }

    @Override
    public CompanyContactDetailRes detail(Long contactId) {
        CompanyContact entity = companyContactMapper.selectById(contactId);
        if (entity == null) {
            throw new GatewayBizException(CorpErrorCode.CONTACT_NOT_FOUND, "企业联系人不存在");
        }
        CompanyContactDetailRes res = new CompanyContactDetailRes();
        BeanUtils.copyProperties(entity, res);
        return res;
    }

    @Override
    @Transactional
    public CompanyContactDetailRes create(CreateCompanyContactReq req) {
        CompanyContact entity = new CompanyContact();
        BeanUtils.copyProperties(req, entity);
        companyContactMapper.insert(entity);
        return detail(entity.getContactId());
    }

    @Override
    @Transactional
    public CompanyContactDetailRes update(UpdateCompanyContactReq req) {
        CompanyContact old = companyContactMapper.selectById(req.getContactId());
        if (old == null) {
            throw new GatewayBizException(CorpErrorCode.CONTACT_NOT_FOUND, "企业联系人不存在");
        }
        BeanUtils.copyProperties(req, old);
        companyContactMapper.updateById(old);
        return detail(old.getContactId());
    }

    @Override
    @Transactional
    public void delete(DeleteCompanyContactReq req) {
        if (companyContactMapper.selectById(req.getContactId()) == null) {
            throw new GatewayBizException(CorpErrorCode.CONTACT_NOT_FOUND, "企业联系人不存在");
        }
        companyContactMapper.deleteById(req.getContactId());
    }

    @Override
    @Transactional
    public void batchDelete(BatchDeleteCompanyContactReq req) {
        if (req.getContactIds() == null || req.getContactIds().isEmpty()) {
            return;
        }
        companyContactMapper.deleteBatchIds(req.getContactIds());
    }

    private CompanyContactListRes toListRes(CompanyContact entity) {
        CompanyContactListRes res = new CompanyContactListRes();
        BeanUtils.copyProperties(entity, res);
        return res;
    }
}

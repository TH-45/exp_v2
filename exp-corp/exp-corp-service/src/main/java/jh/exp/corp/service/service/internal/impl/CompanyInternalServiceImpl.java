package jh.exp.corp.service.service.internal.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.corp.core.entity.Company;
import jh.exp.corp.core.entity.req.*;
import jh.exp.corp.core.entity.res.CompanyDetailRes;
import jh.exp.corp.core.entity.res.CompanyListRes;
import jh.exp.corp.core.mapper.CompanyMapper;
import jh.exp.corp.service.service.internal.CompanyInternalService;
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
public class CompanyInternalServiceImpl implements CompanyInternalService {

    private final CompanyMapper companyMapper;

    @Override
    public SimplePageRes<CompanyListRes> list(SimplePageReq<QueryCompanyReq> req) {
        QueryCompanyReq query = req.getQueryParam() == null ? new QueryCompanyReq() : req.getQueryParam();
        Page<Company> page = new Page<>(req.getPageNum(), req.getPageSize());
        LambdaQueryWrapper<Company> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getCompanyCode()), Company::getCompanyCode, query.getCompanyCode())
                .like(StringUtils.hasText(query.getCompanyName()), Company::getCompanyName, query.getCompanyName())
                .eq(StringUtils.hasText(query.getCompanyType()), Company::getCompanyType, query.getCompanyType())
                .eq(StringUtils.hasText(query.getStatus()), Company::getStatus, query.getStatus())
                .orderByDesc(Company::getCompanyId);
        IPage<Company> result = companyMapper.selectPage(page, wrapper);
        List<CompanyListRes> list = result.getRecords().stream().map(this::toListRes).collect(Collectors.toList());
        SimplePageRes<CompanyListRes> pageRes = new SimplePageRes<>();
        pageRes.setList(list);
        pageRes.setPage(result.getCurrent());
        pageRes.setSize(result.getSize());
        pageRes.setTotal(result.getTotal());
        return pageRes;
    }

    @Override
    public CompanyDetailRes detail(Long companyId) {
        Company company = companyMapper.selectById(companyId);
        if (company == null) {
            throw new RuntimeException("企业不存在");
        }
        CompanyDetailRes res = new CompanyDetailRes();
        BeanUtils.copyProperties(company, res);
        return res;
    }

    @Override
    @Transactional
    public CompanyDetailRes create(CreateCompanyReq req) {
        Company entity = new Company();
        BeanUtils.copyProperties(req, entity);
        entity.setCreatedTime(LocalDateTime.now());
        entity.setUpdatedTime(LocalDateTime.now());
        companyMapper.insert(entity);
        return detail(entity.getCompanyId());
    }

    @Override
    @Transactional
    public CompanyDetailRes update(UpdateCompanyReq req) {
        Company old = companyMapper.selectById(req.getCompanyId());
        if (old == null) {
            throw new RuntimeException("企业不存在");
        }
        BeanUtils.copyProperties(req, old);
        old.setUpdatedTime(LocalDateTime.now());
        companyMapper.updateById(old);
        return detail(old.getCompanyId());
    }

    @Override
    @Transactional
    public void delete(DeleteCompanyReq req) {
        if (companyMapper.selectById(req.getCompanyId()) == null) {
            throw new RuntimeException("企业不存在");
        }
        companyMapper.deleteById(req.getCompanyId());
    }

    @Override
    @Transactional
    public void batchDelete(BatchDeleteCompanyReq req) {
        if (req.getCompanyIds() == null || req.getCompanyIds().isEmpty()) {
            return;
        }
        companyMapper.deleteBatchIds(req.getCompanyIds());
    }

    private CompanyListRes toListRes(Company entity) {
        CompanyListRes res = new CompanyListRes();
        BeanUtils.copyProperties(entity, res);
        return res;
    }
}

package jh.exp.corp.service.service.bus.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.constant.CommonConstant;
import jh.exp.common.core.exception.GatewayBizException;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.corp.core.constant.CorpErrorCode;
import jh.exp.corp.core.entity.Company;
import jh.exp.corp.core.entity.req.*;
import jh.exp.corp.core.entity.res.CompanyDetailRes;
import jh.exp.corp.core.entity.res.CompanyListRes;
import jh.exp.corp.core.mapper.CompanyMapper;
import jh.exp.corp.service.service.bus.CompanyInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompanyInternalServiceImpl implements CompanyInternalService {

    private final CompanyMapper companyMapper;
    private static final Pattern COMPANY_CODE_TOKEN_PATTERN = Pattern.compile("^(\\d{3})([A-Z]*)$");
    private static final DateTimeFormatter COMPANY_CODE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyMMdd");

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
            throw new GatewayBizException(CorpErrorCode.COMPANY_NOT_FOUND, "企业不存在");
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
        // 企业编码统一由后端生成，避免前端并发下出现重复。
        entity.setCompanyCode(generateCompanyCode());
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
            throw new GatewayBizException(CorpErrorCode.COMPANY_NOT_FOUND, "企业不存在");
        }
        String oldCompanyCode = old.getCompanyCode();
        BeanUtils.copyProperties(req, old);
        // 编辑场景不允许修改企业编码，后端做强制保护。
        old.setCompanyCode(oldCompanyCode);
        old.setUpdatedTime(LocalDateTime.now());
        companyMapper.updateById(old);
        return detail(old.getCompanyId());
    }

    @Override
    @Transactional
    public void delete(DeleteCompanyReq req) {
        if (companyMapper.selectById(req.getCompanyId()) == null) {
            throw new GatewayBizException(CorpErrorCode.COMPANY_NOT_FOUND, "企业不存在");
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

    @Override
    public ApiResponse<Map<Long, CompanyDetailRes>> batchDetail(List<Long> companyIds) {
        if (CollectionUtils.isEmpty(companyIds)) {
            return ApiResponse.success(Collections.emptyMap());
        }

        List<Company> companies = companyMapper.selectList(new LambdaQueryWrapper<Company>()
                .eq(Company::getStatus, CommonConstant.ENABLED_STATUS_STR)
                .in(Company::getCompanyId, companyIds));

        if (CollectionUtils.isEmpty(companies)) {
            return ApiResponse.success(Collections.emptyMap());
        }

        Map<Long, CompanyDetailRes> resultData = companies.stream()
                .collect(Collectors.toMap(
                        Company::getCompanyId,
                        company -> {
                            CompanyDetailRes res = new CompanyDetailRes();
                            BeanUtils.copyProperties(company, res);
                            return res;
                        },
                        (v1, v2) -> v1
                ));

        return ApiResponse.success(resultData);
    }

    private CompanyListRes toListRes(Company entity) {
        CompanyListRes res = new CompanyListRes();
        BeanUtils.copyProperties(entity, res);
        return res;
    }

    private String generateCompanyCode() {
        String datePart = LocalDate.now().format(COMPANY_CODE_DATE_FORMATTER);
        String prefix = "C" + datePart;
        List<Company> companies = companyMapper.selectList(new LambdaQueryWrapper<Company>()
                .select(Company::getCompanyCode)
                .likeRight(Company::getCompanyCode, prefix));
        int maxOrder = 0;
        for (Company company : companies) {
            String companyCode = company.getCompanyCode();
            if (!StringUtils.hasText(companyCode) || !companyCode.startsWith(prefix)) {
                continue;
            }
            String token = companyCode.substring(prefix.length()).toUpperCase();
            int order = parseSequenceOrder(token);
            if (order > maxOrder) {
                maxOrder = order;
            }
        }
        return prefix + buildSequenceToken(maxOrder + 1);
    }

    private int parseSequenceOrder(String token) {
        Matcher matcher = COMPANY_CODE_TOKEN_PATTERN.matcher(token);
        if (!matcher.matches()) {
            return -1;
        }
        int sequence = Integer.parseInt(matcher.group(1));
        if (sequence < 1 || sequence > 999) {
            return -1;
        }
        int suffixIndex = lettersToIndex(matcher.group(2));
        if (suffixIndex < 0) {
            return -1;
        }
        return suffixIndex * 999 + sequence;
    }

    private String buildSequenceToken(int order) {
        int normalizedOrder = Math.max(order, 1);
        int suffixIndex = (normalizedOrder - 1) / 999;
        int sequence = ((normalizedOrder - 1) % 999) + 1;
        String suffix = indexToLetters(suffixIndex);
        return String.format("%03d%s", sequence, suffix);
    }

    private int lettersToIndex(String letters) {
        if (!StringUtils.hasText(letters)) {
            return 0;
        }
        int value = 0;
        for (int i = 0; i < letters.length(); i++) {
            char c = Character.toUpperCase(letters.charAt(i));
            if (c < 'A' || c > 'Z') {
                return -1;
            }
            value = value * 26 + (c - 'A' + 1);
        }
        return value;
    }

    private String indexToLetters(int index) {
        if (index <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int value = index;
        while (value > 0) {
            int remainder = (value - 1) % 26;
            sb.append((char) ('A' + remainder));
            value = (value - 1) / 26;
        }
        return sb.reverse().toString();
    }
}

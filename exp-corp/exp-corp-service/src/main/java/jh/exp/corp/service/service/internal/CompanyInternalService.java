package jh.exp.corp.service.service.internal;

import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.corp.core.entity.req.*;
import jh.exp.corp.core.entity.res.CompanyDetailRes;
import jh.exp.corp.core.entity.res.CompanyListRes;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;
import java.util.Map;

public interface CompanyInternalService {
    SimplePageRes<CompanyListRes> list(SimplePageReq<QueryCompanyReq> req);

    CompanyDetailRes detail(Long companyId);

    CompanyDetailRes create(CreateCompanyReq req);

    CompanyDetailRes update(UpdateCompanyReq req);

    void delete(DeleteCompanyReq req);

    void batchDelete(BatchDeleteCompanyReq req);
    /**
     * 批量或许公司详细信息，最多获取一次性获取50个公司详细信息 companyId主键
     */
    @PostExchange("/batchDetail")
    ApiResponse<Map<String,CompanyDetailRes>> batchDetail(@RequestBody List<Long> companyIds);
}

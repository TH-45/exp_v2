package jh.exp.corp.client.api;

import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.corp.core.entity.req.*;
import jh.exp.corp.core.entity.res.CompanyDetailRes;
import jh.exp.corp.core.entity.res.CompanyListRes;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("/internal/corp/company")
public interface CompanyClientService {

    @PostExchange("/list")
    ApiResponse<SimplePageRes<CompanyListRes>> list(@RequestBody SimplePageReq<QueryCompanyReq> req);

    @GetExchange("/detail")
    ApiResponse<CompanyDetailRes> detail(@RequestParam("companyId") Long companyId);

    @PostExchange("/create")
    ApiResponse<CompanyDetailRes> create(@RequestBody CreateCompanyReq req);

    @PostExchange("/update")
    ApiResponse<CompanyDetailRes> update(@RequestBody UpdateCompanyReq req);

    @PostExchange("/delete")
    ApiResponse<Void> delete(@RequestBody DeleteCompanyReq req);

    @PostExchange("/batchDelete")
    ApiResponse<Void> batchDelete(@RequestBody BatchDeleteCompanyReq req);
}

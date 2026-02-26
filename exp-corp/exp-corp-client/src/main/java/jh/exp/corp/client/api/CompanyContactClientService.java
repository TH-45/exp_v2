package jh.exp.corp.client.api;

import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.corp.core.entity.req.*;
import jh.exp.corp.core.entity.res.CompanyContactDetailRes;
import jh.exp.corp.core.entity.res.CompanyContactListRes;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("/internal/corp/company-contact")
public interface CompanyContactClientService {

    @PostExchange("/list")
    ApiResponse<SimplePageRes<CompanyContactListRes>> list(@RequestBody SimplePageReq<QueryCompanyContactReq> req);

    @GetExchange("/detail")
    ApiResponse<CompanyContactDetailRes> detail(@RequestParam("contactId") Long contactId);

    @PostExchange("/create")
    ApiResponse<CompanyContactDetailRes> create(@RequestBody CreateCompanyContactReq req);

    @PostExchange("/update")
    ApiResponse<CompanyContactDetailRes> update(@RequestBody UpdateCompanyContactReq req);

    @PostExchange("/delete")
    ApiResponse<Void> delete(@RequestBody DeleteCompanyContactReq req);

    @PostExchange("/batchDelete")
    ApiResponse<Void> batchDelete(@RequestBody BatchDeleteCompanyContactReq req);
}

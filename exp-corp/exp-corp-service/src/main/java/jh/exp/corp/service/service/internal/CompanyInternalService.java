package jh.exp.corp.service.service.internal;

import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.corp.core.entity.req.*;
import jh.exp.corp.core.entity.res.CompanyDetailRes;
import jh.exp.corp.core.entity.res.CompanyListRes;

public interface CompanyInternalService {
    SimplePageRes<CompanyListRes> list(SimplePageReq<QueryCompanyReq> req);

    CompanyDetailRes detail(Long companyId);

    CompanyDetailRes create(CreateCompanyReq req);

    CompanyDetailRes update(UpdateCompanyReq req);

    void delete(DeleteCompanyReq req);

    void batchDelete(BatchDeleteCompanyReq req);
}

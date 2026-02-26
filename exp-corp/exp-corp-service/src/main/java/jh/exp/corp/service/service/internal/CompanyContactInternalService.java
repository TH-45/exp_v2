package jh.exp.corp.service.service.internal;

import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.corp.core.entity.req.*;
import jh.exp.corp.core.entity.res.CompanyContactDetailRes;
import jh.exp.corp.core.entity.res.CompanyContactListRes;

public interface CompanyContactInternalService {
    SimplePageRes<CompanyContactListRes> list(SimplePageReq<QueryCompanyContactReq> req);

    CompanyContactDetailRes detail(Long contactId);

    CompanyContactDetailRes create(CreateCompanyContactReq req);

    CompanyContactDetailRes update(UpdateCompanyContactReq req);

    void delete(DeleteCompanyContactReq req);

    void batchDelete(BatchDeleteCompanyContactReq req);
}

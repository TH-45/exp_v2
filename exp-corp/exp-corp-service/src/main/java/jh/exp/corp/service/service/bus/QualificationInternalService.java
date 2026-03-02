package jh.exp.corp.service.service.bus;

import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.corp.core.entity.req.*;
import jh.exp.corp.core.entity.res.QualificationDetailRes;
import jh.exp.corp.core.entity.res.QualificationListRes;

public interface QualificationInternalService {
    SimplePageRes<QualificationListRes> list(SimplePageReq<QueryQualificationReq> req);

    QualificationDetailRes detail(Long qualificationId);

    QualificationDetailRes create(CreateQualificationReq req);

    QualificationDetailRes update(UpdateQualificationReq req);

    void delete(DeleteQualificationReq req);

    void batchDelete(BatchDeleteQualificationReq req);
}

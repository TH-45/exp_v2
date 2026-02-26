package jh.exp.corp.service.service.internal;

import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.corp.core.entity.req.*;
import jh.exp.corp.core.entity.res.QualificationAttachmentDetailRes;
import jh.exp.corp.core.entity.res.QualificationAttachmentListRes;

public interface QualificationAttachmentInternalService {
    SimplePageRes<QualificationAttachmentListRes> list(SimplePageReq<QueryQualificationAttachmentReq> req);

    QualificationAttachmentDetailRes detail(Long attachmentId);

    QualificationAttachmentDetailRes create(CreateQualificationAttachmentReq req);

    QualificationAttachmentDetailRes update(UpdateQualificationAttachmentReq req);

    void delete(DeleteQualificationAttachmentReq req);

    void batchDelete(BatchDeleteQualificationAttachmentReq req);
}

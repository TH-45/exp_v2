package jh.exp.corp.service.service.bus;

import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.corp.core.entity.req.*;
import jh.exp.corp.core.entity.res.QualificationAttachmentDetailRes;
import jh.exp.corp.core.entity.res.QualificationAttachmentListRes;
import org.springframework.web.multipart.MultipartFile;

public interface QualificationAttachmentInternalService {
    SimplePageRes<QualificationAttachmentListRes> list(SimplePageReq<QueryQualificationAttachmentReq> req);

    QualificationAttachmentDetailRes detail(Long attachmentId);

    QualificationAttachmentDetailRes create(CreateQualificationAttachmentReq req);

    QualificationAttachmentDetailRes update(UpdateQualificationAttachmentReq req);

    void delete(DeleteQualificationAttachmentReq req);

    void batchDelete(BatchDeleteQualificationAttachmentReq req);

    QualificationAttachmentDetailRes upload(Long qualificationId, MultipartFile file);

    byte[] download(Long attachmentId);
}

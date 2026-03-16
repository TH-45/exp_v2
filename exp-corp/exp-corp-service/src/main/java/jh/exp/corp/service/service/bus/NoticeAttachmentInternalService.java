package jh.exp.corp.service.service.bus;

import jh.exp.corp.core.entity.req.DeleteNoticeAttachmentReq;
import jh.exp.corp.core.entity.req.QueryNoticeAttachmentReq;
import jh.exp.corp.core.entity.res.NoticeAttachmentRes;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface NoticeAttachmentInternalService {
    List<NoticeAttachmentRes> list(QueryNoticeAttachmentReq req);

    NoticeAttachmentRes upload(Long noticeId, MultipartFile file);

    void delete(DeleteNoticeAttachmentReq req);

    byte[] download(Long attachmentId);
}

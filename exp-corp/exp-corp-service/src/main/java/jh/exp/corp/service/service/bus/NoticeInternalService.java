package jh.exp.corp.service.service.bus;

import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.corp.core.entity.req.CreateNoticeReq;
import jh.exp.corp.core.entity.req.DeleteNoticeReq;
import jh.exp.corp.core.entity.req.NoticeActionReq;
import jh.exp.corp.core.entity.req.UpdateNoticeReq;
import jh.exp.corp.core.entity.res.NoticeDetailRes;
import jh.exp.corp.core.entity.res.NoticeListRes;

public interface NoticeInternalService {
    SimplePageRes<NoticeListRes> list(SimplePageReq<jh.exp.corp.core.entity.req.QueryNoticeReq> req);

    NoticeDetailRes detail(Long noticeId);

    NoticeDetailRes create(CreateNoticeReq req);

    NoticeDetailRes update(UpdateNoticeReq req);

    void delete(DeleteNoticeReq req);

    NoticeDetailRes publish(NoticeActionReq req);

    NoticeDetailRes withdraw(NoticeActionReq req);
}

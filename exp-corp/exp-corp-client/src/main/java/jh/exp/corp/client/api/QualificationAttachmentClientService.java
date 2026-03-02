package jh.exp.corp.client.api;

import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.corp.core.entity.req.*;
import jh.exp.corp.core.entity.res.QualificationAttachmentDetailRes;
import jh.exp.corp.core.entity.res.QualificationAttachmentListRes;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("/qualification-attachment")
public interface QualificationAttachmentClientService {

    @PostExchange("/list")
    ApiResponse<SimplePageRes<QualificationAttachmentListRes>> list(
            @RequestBody SimplePageReq<QueryQualificationAttachmentReq> req);

    @GetExchange("/detail")
    ApiResponse<QualificationAttachmentDetailRes> detail(@RequestParam("attachmentId") Long attachmentId);

    @PostExchange("/create")
    ApiResponse<QualificationAttachmentDetailRes> create(@RequestBody CreateQualificationAttachmentReq req);

    @PostExchange("/update")
    ApiResponse<QualificationAttachmentDetailRes> update(@RequestBody UpdateQualificationAttachmentReq req);

    @PostExchange("/delete")
    ApiResponse<Void> delete(@RequestBody DeleteQualificationAttachmentReq req);

    @PostExchange("/batchDelete")
    ApiResponse<Void> batchDelete(@RequestBody BatchDeleteQualificationAttachmentReq req);
}

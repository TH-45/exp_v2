package jh.exp.corp.client.api;

import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.corp.core.entity.req.*;
import jh.exp.corp.core.entity.res.QualificationDetailRes;
import jh.exp.corp.core.entity.res.QualificationListRes;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("/internal/corp/qualification")
public interface QualificationClientService {

    @PostExchange("/list")
    ApiResponse<SimplePageRes<QualificationListRes>> list(@RequestBody SimplePageReq<QueryQualificationReq> req);

    @GetExchange("/detail")
    ApiResponse<QualificationDetailRes> detail(@RequestParam("qualificationId") Long qualificationId);

    @PostExchange("/create")
    ApiResponse<QualificationDetailRes> create(@RequestBody CreateQualificationReq req);

    @PostExchange("/update")
    ApiResponse<QualificationDetailRes> update(@RequestBody UpdateQualificationReq req);

    @PostExchange("/delete")
    ApiResponse<Void> delete(@RequestBody DeleteQualificationReq req);

    @PostExchange("/batchDelete")
    ApiResponse<Void> batchDelete(@RequestBody BatchDeleteQualificationReq req);
}

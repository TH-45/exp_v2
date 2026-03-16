package jh.exp.corp.service.controller.bus;

import jakarta.validation.Valid;
import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.corp.core.entity.req.CreateNoticeReq;
import jh.exp.corp.core.entity.req.DeleteNoticeReq;
import jh.exp.corp.core.entity.req.NoticeActionReq;
import jh.exp.corp.core.entity.req.QueryNoticeReq;
import jh.exp.corp.core.entity.req.UpdateNoticeReq;
import jh.exp.corp.core.entity.res.NoticeDetailRes;
import jh.exp.corp.core.entity.res.NoticeListRes;
import jh.exp.corp.service.service.bus.NoticeInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeInternalService noticeInternalService;

    @PostMapping("/list")
    public ApiResponse<SimplePageRes<NoticeListRes>> list(@RequestBody SimplePageReq<QueryNoticeReq> req) {
        req.pageDefault();
        return ApiResponse.success(noticeInternalService.list(req));
    }

    @GetMapping("/detail")
    public ApiResponse<NoticeDetailRes> detail(@RequestParam Long noticeId) {
        return ApiResponse.success(noticeInternalService.detail(noticeId));
    }

    @PostMapping("/create")
    public ApiResponse<NoticeDetailRes> create(@RequestBody @Valid CreateNoticeReq req) {
        return ApiResponse.success(noticeInternalService.create(req));
    }

    @PostMapping("/update")
    public ApiResponse<NoticeDetailRes> update(@RequestBody @Valid UpdateNoticeReq req) {
        return ApiResponse.success(noticeInternalService.update(req));
    }

    @PostMapping("/delete")
    public ApiResponse<Void> delete(@RequestBody @Valid DeleteNoticeReq req) {
        noticeInternalService.delete(req);
        return ApiResponse.success(null);
    }

    @PostMapping("/publish")
    public ApiResponse<NoticeDetailRes> publish(@RequestBody @Valid NoticeActionReq req) {
        return ApiResponse.success(noticeInternalService.publish(req));
    }

    @PostMapping("/withdraw")
    public ApiResponse<NoticeDetailRes> withdraw(@RequestBody @Valid NoticeActionReq req) {
        return ApiResponse.success(noticeInternalService.withdraw(req));
    }
}

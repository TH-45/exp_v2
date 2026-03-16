package jh.exp.corp.service.controller.bus;

import jakarta.validation.Valid;
import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.corp.core.entity.req.*;
import jh.exp.corp.core.entity.res.QualificationDetailRes;
import jh.exp.corp.core.entity.res.QualificationListRes;
import jh.exp.corp.core.entity.res.QualificationStatsRes;
import jh.exp.corp.service.service.bus.QualificationInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/qualification")
@RequiredArgsConstructor
public class QualificationController {

    private final QualificationInternalService qualificationInternalService;

    @PostMapping("/list")
    public ApiResponse<SimplePageRes<QualificationListRes>> list(@RequestBody SimplePageReq<QueryQualificationReq> req) {
        req.pageDefault();
        return ApiResponse.success(qualificationInternalService.list(req));
    }

    @GetMapping("/detail")
    public ApiResponse<QualificationDetailRes> detail(@RequestParam Long qualificationId) {
        return ApiResponse.success(qualificationInternalService.detail(qualificationId));
    }

    @PostMapping("/create")
    public ApiResponse<QualificationDetailRes> create(@RequestBody @Valid CreateQualificationReq req) {
        return ApiResponse.success(qualificationInternalService.create(req));
    }

    @PostMapping("/update")
    public ApiResponse<QualificationDetailRes> update(@RequestBody @Valid UpdateQualificationReq req) {
        return ApiResponse.success(qualificationInternalService.update(req));
    }

    @PostMapping("/delete")
    public ApiResponse<Void> delete(@RequestBody @Valid DeleteQualificationReq req) {
        qualificationInternalService.delete(req);
        return ApiResponse.success(null);
    }

    @PostMapping("/batchDelete")
    public ApiResponse<Void> batchDelete(@RequestBody @Valid BatchDeleteQualificationReq req) {
        qualificationInternalService.batchDelete(req);
        return ApiResponse.success(null);
    }

    @GetMapping("/stats")
    public ApiResponse<QualificationStatsRes> stats() {
        return ApiResponse.success(qualificationInternalService.stats());
    }
}

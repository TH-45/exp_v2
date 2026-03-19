package jh.exp.corp.service.controller.bus;

import jakarta.validation.Valid;
import jh.exp.common.core.annotation.RequiresMenuLevel;
import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.corp.core.entity.req.*;
import jh.exp.corp.core.entity.res.CompanyDetailRes;
import jh.exp.corp.core.entity.res.CompanyListRes;
import jh.exp.corp.service.service.bus.CompanyInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/company")
@RequiredArgsConstructor
@RequiresMenuLevel(code = "enterprise:basic", level = 1)
public class CompanyController {

    private final CompanyInternalService companyInternalService;

    @PostMapping("/list")
    public ApiResponse<SimplePageRes<CompanyListRes>> list(@RequestBody SimplePageReq<QueryCompanyReq> req) {
        req.pageDefault();
        return ApiResponse.success(companyInternalService.list(req));
    }

    @GetMapping("/detail")
    public ApiResponse<CompanyDetailRes> detail(@RequestParam Long companyId) {
        return ApiResponse.success(companyInternalService.detail(companyId));
    }

    @PostMapping("/batchDetail")
    public ApiResponse<Map<Long, CompanyDetailRes>> batchDetail(@RequestBody List<Long> companyIds) {
        return companyInternalService.batchDetail(companyIds);
    }

    @PostMapping("/create")
    @RequiresMenuLevel(code = "enterprise:basic", level = 2)
    public ApiResponse<CompanyDetailRes> create(@RequestBody @Valid CreateCompanyReq req) {
        return ApiResponse.success(companyInternalService.create(req));
    }

    @PostMapping("/update")
    @RequiresMenuLevel(code = "enterprise:basic", level = 2)
    public ApiResponse<CompanyDetailRes> update(@RequestBody @Valid UpdateCompanyReq req) {
        return ApiResponse.success(companyInternalService.update(req));
    }

    @PostMapping("/delete")
    @RequiresMenuLevel(code = "enterprise:basic", level = 3)
    public ApiResponse<Void> delete(@RequestBody @Valid DeleteCompanyReq req) {
        companyInternalService.delete(req);
        return ApiResponse.success(null);
    }

    @PostMapping("/batchDelete")
    @RequiresMenuLevel(code = "enterprise:basic", level = 3)
    public ApiResponse<Void> batchDelete(@RequestBody @Valid BatchDeleteCompanyReq req) {
        companyInternalService.batchDelete(req);
        return ApiResponse.success(null);
    }
}

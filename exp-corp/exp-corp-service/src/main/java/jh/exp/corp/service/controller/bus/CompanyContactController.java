package jh.exp.corp.service.controller.bus;

import jakarta.validation.Valid;
import jh.exp.common.core.annotation.RequiresMenuLevel;
import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.corp.core.entity.req.*;
import jh.exp.corp.core.entity.res.CompanyContactDetailRes;
import jh.exp.corp.core.entity.res.CompanyContactListRes;
import jh.exp.corp.service.service.bus.CompanyContactInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/company-contact")
@RequiredArgsConstructor
@RequiresMenuLevel(code = "enterprise:basic", level = 1)
public class CompanyContactController {

    private final CompanyContactInternalService companyContactInternalService;

    @PostMapping("/list")
    public ApiResponse<SimplePageRes<CompanyContactListRes>> list(@RequestBody SimplePageReq<QueryCompanyContactReq> req) {
        req.pageDefault();
        return ApiResponse.success(companyContactInternalService.list(req));
    }

    @GetMapping("/detail")
    public ApiResponse<CompanyContactDetailRes> detail(@RequestParam Long contactId) {
        return ApiResponse.success(companyContactInternalService.detail(contactId));
    }

    @PostMapping("/create")
    @RequiresMenuLevel(code = "enterprise:basic", level = 2)
    public ApiResponse<CompanyContactDetailRes> create(@RequestBody @Valid CreateCompanyContactReq req) {
        return ApiResponse.success(companyContactInternalService.create(req));
    }

    @PostMapping("/update")
    @RequiresMenuLevel(code = "enterprise:basic", level = 2)
    public ApiResponse<CompanyContactDetailRes> update(@RequestBody @Valid UpdateCompanyContactReq req) {
        return ApiResponse.success(companyContactInternalService.update(req));
    }

    @PostMapping("/delete")
    @RequiresMenuLevel(code = "enterprise:basic", level = 3)
    public ApiResponse<Void> delete(@RequestBody @Valid DeleteCompanyContactReq req) {
        companyContactInternalService.delete(req);
        return ApiResponse.success(null);
    }

    @PostMapping("/batchDelete")
    @RequiresMenuLevel(code = "enterprise:basic", level = 3)
    public ApiResponse<Void> batchDelete(@RequestBody @Valid BatchDeleteCompanyContactReq req) {
        companyContactInternalService.batchDelete(req);
        return ApiResponse.success(null);
    }
}

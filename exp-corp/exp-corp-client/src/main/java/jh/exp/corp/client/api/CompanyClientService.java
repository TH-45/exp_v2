package jh.exp.corp.client.api;

import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.corp.core.entity.req.*;
import jh.exp.corp.core.entity.res.CompanyDetailRes;
import jh.exp.corp.core.entity.res.CompanyListRes;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;
import java.util.Map;

/**
 * 公司服务客户端接口
 * 提供公司相关的API调用方法
 */
@HttpExchange("/internal/corp/company")
public interface CompanyClientService {

    /**
     * 获取公司列表
     * @param req 分页查询请求参数
     * @return 公司列表分页结果
     */
    @PostExchange("/list")
    ApiResponse<SimplePageRes<CompanyListRes>> list(@RequestBody SimplePageReq<QueryCompanyReq> req);

    /**
     * 获取公司详情
     * @param companyId 公司ID
     * @return 公司详细信息
     */
    @GetExchange("/detail")
    ApiResponse<CompanyDetailRes> detail(@RequestParam("companyId") Long companyId);

    /**
     * 批量或许公司详细信息，最多获取一次性获取50个公司详细信息 companyId主键
     */
    @PostExchange("/batchDetail")
    ApiResponse<Map<Long,CompanyDetailRes>> batchDetail(@RequestBody List<Long> companyIds);


    /**
     * 创建公司
     * @param req 创建公司请求参数
     * @return 创建的公司详细信息
     */
    @PostExchange("/create")
    ApiResponse<CompanyDetailRes> create(@RequestBody CreateCompanyReq req);

    /**
     * 更新公司信息
     * @param req 更新公司请求参数
     * @return 更新后的公司详细信息
     */
    @PostExchange("/update")
    ApiResponse<CompanyDetailRes> update(@RequestBody UpdateCompanyReq req);

    /**
     * 删除公司
     * @param req 删除公司请求参数
     * @return 删除结果
     */
    @PostExchange("/delete")
    ApiResponse<Void> delete(@RequestBody DeleteCompanyReq req);

    /**
     * 批量删除公司
     * @param req 批量删除公司请求参数
     * @return 批量删除结果
     */
    @PostExchange("/batchDelete")
    ApiResponse<Void> batchDelete(@RequestBody BatchDeleteCompanyReq req);


}

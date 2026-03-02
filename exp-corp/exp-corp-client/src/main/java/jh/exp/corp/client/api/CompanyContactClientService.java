package jh.exp.corp.client.api;

import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.corp.core.entity.req.*;
import jh.exp.corp.core.entity.res.CompanyContactDetailRes;
import jh.exp.corp.core.entity.res.CompanyContactListRes;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

/**
 * 公司联系人客户端服务接口
 * 提供公司联系人的增删改查及相关批量操作功能
 */
@HttpExchange("/company-contact")
public interface CompanyContactClientService {

    /**
     * 分页查询公司联系人列表
     * @param req 分页查询请求参数
     * @return 公司联系人分页结果
     */
    @PostExchange("/list")
    ApiResponse<SimplePageRes<CompanyContactListRes>> list(@RequestBody SimplePageReq<QueryCompanyContactReq> req);

    /**
     * 根据联系人ID获取详细信息
     * @param contactId 联系人ID
     * @return 公司联系人详细信息
     */
    @GetExchange("/detail")
    ApiResponse<CompanyContactDetailRes> detail(@RequestParam("contactId") Long contactId);

    /**
     * 创建新的公司联系人
     * @param req 创建联系人请求参数
     * @return 创建后的联系人详细信息
     */
    @PostExchange("/create")
    ApiResponse<CompanyContactDetailRes> create(@RequestBody CreateCompanyContactReq req);

    /**
     * 更新公司联系人信息
     * @param req 更新联系人请求参数
     * @return 更新后的联系人详细信息
     */
    @PostExchange("/update")
    ApiResponse<CompanyContactDetailRes> update(@RequestBody UpdateCompanyContactReq req);

    /**
     * 删除指定的公司联系人
     * @param req 删除联系人请求参数
     * @return 操作结果
     */
    @PostExchange("/delete")
    ApiResponse<Void> delete(@RequestBody DeleteCompanyContactReq req);

    /**
     * 批量删除公司联系人
     * @param req 批量删除联系人请求参数
     * @return 操作结果
     */
    @PostExchange("/batchDelete")
    ApiResponse<Void> batchDelete(@RequestBody BatchDeleteCompanyContactReq req);
}

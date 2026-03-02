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

/**
 * 资质管理客户端服务接口
 * 提供资质相关的远程调用方法
 */
@HttpExchange("/qualification")
public interface QualificationClientService {

    /**
     * 分页查询资质列表
     * @param req 分页查询请求参数
     * @return 资质列表分页结果
     */
    @PostExchange("/list")
    ApiResponse<SimplePageRes<QualificationListRes>> list(@RequestBody SimplePageReq<QueryQualificationReq> req);

    /**
     * 根据ID获取资质详情
     * @param qualificationId 资质ID
     * @return 资质详情信息
     */
    @GetExchange("/detail")
    ApiResponse<QualificationDetailRes> detail(@RequestParam("qualificationId") Long qualificationId);

    /**
     * 创建新的资质
     * @param req 创建资质请求参数
     * @return 创建后的资质详情
     */
    @PostExchange("/create")
    ApiResponse<QualificationDetailRes> create(@RequestBody CreateQualificationReq req);

    /**
     * 更新资质信息
     * @param req 更新资质请求参数
     * @return 更新后的资质详情
     */
    @PostExchange("/update")
    ApiResponse<QualificationDetailRes> update(@RequestBody UpdateQualificationReq req);

    /**
     * 删除指定资质
     * @param req 删除资质请求参数
     * @return 操作结果
     */
    @PostExchange("/delete")
    ApiResponse<Void> delete(@RequestBody DeleteQualificationReq req);

    /**
     * 批量删除资质
     * @param req 批量删除资质请求参数
     * @return 操作结果
     */
    @PostExchange("/batchDelete")
    ApiResponse<Void> batchDelete(@RequestBody BatchDeleteQualificationReq req);
}

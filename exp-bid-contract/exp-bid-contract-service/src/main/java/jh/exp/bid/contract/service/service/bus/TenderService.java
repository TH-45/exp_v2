package jh.exp.bid.contract.service.service.bus;

import jh.exp.bid.contract.core.entity.req.*;
import jh.exp.bid.contract.core.entity.res.TenderDetailRes;
import jh.exp.bid.contract.core.entity.res.TenderListRes;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;

/**
 * 招标服务接口
 */
public interface TenderService {

    /**
     * 分页查询招标列表
     */
    SimplePageRes<TenderListRes> queryTenderList(SimplePageReq<QueryTenderReq> req);

    /**
     * 分页查询可进入评标/定标流程的招标列表
     */
    SimplePageRes<TenderListRes> queryEvaluationEligibleTenderList(SimplePageReq<QueryTenderReq> req);

    /**
     * 校验招标项目是否可进入评标/定标流程
     */
    boolean checkEvaluationFlowEligible(Long tenderId);

    /**
     * 根据ID查询招标详情
     */
    TenderDetailRes getTenderById(Long tenderId);

    /**
     * 创建招标
     */
    TenderDetailRes createTender(CreateTenderReq req);

    /**
     * 更新招标
     */
    TenderDetailRes updateTender(UpdateTenderReq req);

    /**
     * 删除招标
     */
    void deleteTender(Long tenderId);

    /**
     * 批量删除招标
     */
    void batchDeleteTenders(BatchDeleteTenderReq req);

    /**
     * 更改招标状态
     */
    TenderDetailRes updateTenderStatus(TenderStatusReq req);

    /**
     * 批量更改招标状态
     */
    void batchUpdateTenderStatus(BatchTenderStatusReq req);

    /**
     * 检查招标编号是否存在
     */
    boolean checkTenderCodeExists(String tenderCode, Long excludeTenderId);

    /**
     * 根据项目ID获取项目负责人信息
     */
    TenderDetailRes getProjectManagerByProjectId(Long projectId);

    /**
     * 检查用户是否有删除招标的权限
     */
    boolean checkDeletePermission(Long tenderId, Long userId);

    /**
     * 绑定招标业务员
     */
    void bindSalesman(BindTenderSalesmanReq req);
}
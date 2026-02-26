package jh.exp.bid.contract.service.service.bus.Impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jh.exp.auth.clinet.api.AccountService;
import jh.exp.auth.clinet.api.PersonService;


import jh.exp.auth.core.entity.res.PersonDetailRes;
import jh.exp.bid.contract.core.entity.Tender;
import jh.exp.bid.contract.core.entity.req.*;
import jh.exp.bid.contract.core.entity.res.TenderDetailRes;
import jh.exp.bid.contract.core.entity.res.TenderListRes;
import jh.exp.bid.contract.core.mapper.TenderMapper;
import jh.exp.bid.contract.service.service.bus.TenderService;
import jh.exp.common.core.auth.CurrentUserHolder;
import jh.exp.common.core.auth.dto.CurrentUser;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;

/**
 * 招标服务实现类
 */
@Service
@RequiredArgsConstructor
public class TenderServiceImpl implements TenderService {

    private final TenderMapper tenderMapper;
    private final PersonService personService;
    private final AccountService accountService;

    @Override
    public SimplePageRes<TenderListRes> queryTenderList(SimplePageReq<QueryTenderReq> req) {
        // 创建分页对象
        Page<TenderListRes> page = new Page<>(req.getPageNum(), req.getPageSize());

        QueryTenderReq queryParam = req.getQueryParam();
        // 如果前端没有传递查询参数，创建一个默认的空对象
        if (queryParam == null) {
            queryParam = new QueryTenderReq();
        }

        // 使用MyBatis-Plus自动分页查询
        IPage<TenderListRes> result = tenderMapper.selectTenderList(page, queryParam);

        // 转换为统一的响应格式
        SimplePageRes<TenderListRes> pageRes = new SimplePageRes<>();
        pageRes.setList(result.getRecords());
        pageRes.setTotal(result.getTotal());
        pageRes.setPage(result.getCurrent());
        pageRes.setSize(result.getSize());
        return pageRes;
    }

    @Override
    public TenderDetailRes getTenderById(Long tenderId) {
        TenderDetailRes tenderDetail = tenderMapper.selectTenderDetailById(tenderId);
        if (tenderDetail == null) {
            throw new RuntimeException("招标信息不存在");
        }
        return tenderDetail;
    }

    @Override
    @Transactional
    public TenderDetailRes createTender(CreateTenderReq req) {
        // 检查招标编号是否已存在
        if (checkTenderCodeExists(req.getTenderCode(), null)) {
            throw new RuntimeException("招标编号已存在");
        }

        // 根据项目ID获取项目负责人信息
        TenderDetailRes projectInfo = getProjectManagerByProjectId(req.getProjectId());
        if (projectInfo == null || projectInfo.getProjectId() == null) {
            throw new RuntimeException("项目信息不存在或项目负责人未设置");
        }

        // 验证项目组织负责人是否存在
        if (projectInfo.getOrgManagerId() == null) {
            throw new RuntimeException("项目归属组织的负责人未设置，无法创建招标");
        }

        // 调用auth服务获取当前用户信息
        CurrentUser currentUser = CurrentUserHolder.get();
        Long personId = Long.valueOf(currentUser.getUserId());

        // 通过认证服务查询人员详细信息，获取部门和岗位信息

        PersonDetailRes personDetail = personService.getPersonById(personId);

        if (personDetail == null) {
            throw new RuntimeException("无法获取当前用户信息");
        }

        Tender tender = new Tender();
        tender.setTenderCode(req.getTenderCode());
        tender.setTenderName(req.getTenderName());
        tender.setTenderType(req.getTenderType());
        tender.setTenderMode(req.getTenderMode());
        tender.setCompanyId(req.getCompanyId());
        tender.setBudgetAmount(req.getBudgetAmount());
        tender.setCurrency(req.getCurrency());
        tender.setTenderBrief(req.getTenderBrief());
        tender.setPublishTime(req.getPublishTime());
        tender.setBidStartTime(req.getBidStartTime());
        tender.setBidEndTime(req.getBidEndTime());
        tender.setOpenTime(req.getOpenTime());
        tender.setOpenAddress(req.getOpenAddress());
        tender.setProjectId(req.getProjectId());
        tender.setStatus("准备"); // 新建招标默认为准备状态
        tender.setRemark(req.getRemark());
        tender.setCreatedTime(LocalDateTime.now());
        tender.setUpdatedTime(LocalDateTime.now());

        // 设置创建人相关信息
        tender.setCreatedBy(personId);
        tender.setCreatedDeptId(personDetail.getOrgId());
        tender.setCreatedPostId(personDetail.getPostId());

        tenderMapper.insert(tender);

        // 返回创建后的招标详情信息
        return getTenderById(tender.getTenderId());
    }

    @Override
    @Transactional
    public TenderDetailRes updateTender(UpdateTenderReq req) {
        // 检查招标是否存在
        Tender existingTender = tenderMapper.selectById(req.getTenderId());
        if (existingTender == null) {
            throw new RuntimeException("招标信息不存在");
        }

        // 检查招标编号是否已存在（排除当前招标）
        if (checkTenderCodeExists(req.getTenderCode(), req.getTenderId())) {
            throw new RuntimeException("招标编号已存在");
        }

        // 注意：projectId在更新时不允许修改，这里不设置projectId字段
        Tender tender = new Tender();
        tender.setTenderId(req.getTenderId());
        tender.setTenderCode(req.getTenderCode());
        tender.setTenderName(req.getTenderName());
        tender.setTenderType(req.getTenderType());
        tender.setTenderMode(req.getTenderMode());
        tender.setCompanyId(req.getCompanyId());
        tender.setBudgetAmount(req.getBudgetAmount());
        tender.setCurrency(req.getCurrency());
        tender.setTenderBrief(req.getTenderBrief());
        tender.setPublishTime(req.getPublishTime());
        tender.setBidStartTime(req.getBidStartTime());
        tender.setBidEndTime(req.getBidEndTime());
        tender.setOpenTime(req.getOpenTime());
        tender.setOpenAddress(req.getOpenAddress());
        // projectId字段不更新，保持原有值
        tender.setRemark(req.getRemark());
        tender.setUpdatedTime(LocalDateTime.now());

        tenderMapper.updateById(tender);

        // 返回更新后的招标信息
        return getTenderById(req.getTenderId());
    }

    @Override
    @Transactional
    public void deleteTender(Long tenderId) {
        // 检查招标是否存在
        Tender tender = tenderMapper.selectById(tenderId);
        if (tender == null) {
            throw new RuntimeException("招标信息不存在");
        }

        // 检查当前用户是否有删除权限
        CurrentUser currentUser = CurrentUserHolder.get();
        if (!checkDeletePermission(tenderId, Long.valueOf(currentUser.getUserId()))) {
            throw new RuntimeException("无权限删除该招标信息");
        }

        // TODO: 检查招标是否有相关联的业务数据，如果有则不允许删除

        tenderMapper.deleteById(tenderId);
    }

    @Override
    @Transactional
    public void batchDeleteTenders(BatchDeleteTenderReq req) {
        if (CollectionUtils.isEmpty(req.getTenderIds())) {
            return;
        }

        CurrentUser currentUser = CurrentUserHolder.get();
        Long userId = Long.valueOf(currentUser.getUserId());

        // 检查每个招标的删除权限
        for (Long tenderId : req.getTenderIds()) {
            if (!checkDeletePermission(tenderId, userId)) {
                throw new RuntimeException("无权限删除招标ID: " + tenderId);
            }
        }

        // TODO: 检查招标是否有相关联的业务数据，如果有则不允许删除

        UpdateWrapper<Tender> updateWrapper = new UpdateWrapper<>();
        updateWrapper.in("tender_id", req.getTenderIds());
        tenderMapper.delete(updateWrapper);
    }

    @Override
    @Transactional
    public TenderDetailRes updateTenderStatus(TenderStatusReq req) {
        // 检查招标是否存在
        Tender existingTender = tenderMapper.selectById(req.getTenderId());
        if (existingTender == null) {
            throw new RuntimeException("招标信息不存在");
        }

        Tender tender = new Tender();
        tender.setTenderId(req.getTenderId());
        tender.setStatus(req.getStatus());
        tender.setUpdatedTime(LocalDateTime.now());

        tenderMapper.updateById(tender);

        // 返回更新后的招标信息
        return getTenderById(req.getTenderId());
    }

    @Override
    @Transactional
    public void batchUpdateTenderStatus(BatchTenderStatusReq req) {
        if (CollectionUtils.isEmpty(req.getTenderIds())) {
            return;
        }

        tenderMapper.batchUpdateStatus(req.getTenderIds(), req.getStatus());
    }

    @Override
    public boolean checkTenderCodeExists(String tenderCode, Long excludeTenderId) {
        return tenderMapper.countByTenderCode(tenderCode, excludeTenderId) > 0;
    }

    @Override
    public TenderDetailRes getProjectManagerByProjectId(Long projectId) {
        return tenderMapper.selectProjectManagerByProjectId(projectId);
    }

    @Override
    public boolean checkDeletePermission(Long tenderId, Long userId) {
        // 检查招标是否存在
        Tender tender = tenderMapper.selectById(tenderId);
        if (tender == null) {
            return false;
        }

        // 检查是否为招标创建者
        if (tender.getCreatedBy().equals(userId)) {
            return true;
        }

        // 示例：通过认证服务获取账号详细信息进行权限检查
        try {
            Object accountDetail = accountService.getAccountById(userId);
            if (accountDetail != null) {
                // 可以根据账号信息进行更复杂的权限检查
                // 例如：检查账号状态、角色、部门等
                // 这里暂时简化处理，实际使用时需要根据返回的具体类型进行处理
                return true; // 账号存在且正常
            }
        } catch (Exception e) {
            // 服务调用失败时的降级处理
            // 可以记录日志，但不影响原有逻辑
        }

        return false;
    }
}
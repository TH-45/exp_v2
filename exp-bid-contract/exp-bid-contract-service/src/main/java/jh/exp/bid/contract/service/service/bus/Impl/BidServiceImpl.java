package jh.exp.bid.contract.service.service.bus.Impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jh.exp.auth.clinet.api.bus.AccountService;
import jh.exp.auth.clinet.api.bus.PersonService;
import jh.exp.auth.core.entity.res.PersonDetailRes;
import jh.exp.bid.contract.core.entity.Bid;
import jh.exp.bid.contract.core.entity.req.*;
import jh.exp.bid.contract.core.entity.res.BidDetailRes;
import jh.exp.bid.contract.core.entity.res.BidListRes;
import jh.exp.bid.contract.core.mapper.BidMapper;
import jh.exp.bid.contract.service.service.bus.BidService;
import jh.exp.common.core.auth.CurrentUserHolder;
import jh.exp.common.core.auth.dto.CurrentUser;
import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.corp.client.api.CompanyClientService;
import jh.exp.corp.core.entity.res.CompanyDetailRes;
import jh.exp.project.client.api.ProjectClientService;
import jh.exp.project.core.entity.Project;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 投标服务实现类
 * 本模块表（exp_bid、exp_tender）仅通过 Mapper 增删改查；其他模块表（供应商/项目/人员等）通过调用对应模块接口查询，不直接查表。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BidServiceImpl implements BidService {

    private final BidMapper bidMapper;
    private final PersonService personService;
    private final AccountService accountService;
    private final CompanyClientService companyClientService;
    private final ProjectClientService projectClientService;

    @Override
    public SimplePageRes<BidListRes> queryBidList(SimplePageReq<QueryBidReq> req) {
        req.pageDefault();
        Page<BidListRes> page = new Page<>(req.getPageNum(), req.getPageSize());
        QueryBidReq queryParam = req.getQueryParam() != null ? req.getQueryParam() : new QueryBidReq();

        // 仅查本模块表（exp_bid、exp_tender）
        IPage<BidListRes> result = bidMapper.selectBidList(page, queryParam);
        List<BidListRes> records = result.getRecords();

        // 通过各模块接口补全供应商名称、项目名称、创建人姓名（不直接查其他模块表）
        if (!CollectionUtils.isEmpty(records)) {
            fillBidListResNames(records);
        }

        SimplePageRes<BidListRes> pageRes = new SimplePageRes<>();
        pageRes.setList(records);
        pageRes.setTotal(result.getTotal());
        pageRes.setPage(result.getCurrent());
        pageRes.setSize(result.getSize());
        return pageRes;
    }

    @Override
    public BidDetailRes getBidById(Long bidId) {
        // 仅查本模块表
        BidDetailRes bidDetail = bidMapper.selectBidDetailById(bidId);
        if (bidDetail == null) {
            throw new RuntimeException("投标信息不存在");
        }
        // 通过各模块接口补全供应商/项目/创建人及部门岗位名称
        fillBidDetailResNames(bidDetail);
        return bidDetail;
    }

    @Override
    @Transactional
    public BidDetailRes createBid(CreateBidReq req) {
        // 检查投标编号是否已存在
        if (checkBidCodeExists(req.getBidCode(), null)) {
            throw new RuntimeException("投标编号已存在");
        }

        // 检查投标单位是否已对该招标项目投标
        if (checkSupplierBidExists(req.getTenderId(), req.getSupplierId(), null)) {
            throw new RuntimeException("该投标单位已对该招标项目投标，不能重复投标");
        }

        // 调用auth服务获取当前用户信息
        CurrentUser currentUser = CurrentUserHolder.get();
        Long personId = Long.valueOf(currentUser.getUserId());

        // 通过认证服务查询人员详细信息，获取部门和岗位信息
        PersonDetailRes personDetail = personService.getPersonById(personId);

        if (personDetail == null) {
            throw new RuntimeException("无法获取当前用户信息");
        }

        Bid bid = new Bid();
        bid.setTenderId(req.getTenderId());
        bid.setSupplierId(req.getSupplierId());
        bid.setBidCode(req.getBidCode());
        bid.setBidName(req.getBidName());
        bid.setBidTotalAmount(req.getBidTotalAmount());
        bid.setCurrency(req.getCurrency());
        bid.setBidSubmitTime(req.getBidSubmitTime());
        bid.setProjectId(req.getProjectId());
        bid.setBidStatus("准备"); // 新建投标默认为准备状态
        bid.setWinFlag(0); // 默认未中标
        bid.setRemark(req.getRemark());
        bid.setCreatedTime(LocalDateTime.now());
        bid.setUpdatedTime(LocalDateTime.now());

        // 设置创建人相关信息
        bid.setCreatedBy(personId);
        bid.setCreatedDeptId(personDetail.getOrgId());
        bid.setCreatedPostId(personDetail.getPostId());

        bidMapper.insert(bid);

        // 返回创建后的投标详情信息
        return getBidById(bid.getBidId());
    }

    @Override
    @Transactional
    public BidDetailRes updateBid(UpdateBidReq req) {
        // 检查投标是否存在
        Bid existingBid = bidMapper.selectById(req.getBidId());
        if (existingBid == null) {
            throw new RuntimeException("投标信息不存在");
        }

        // 检查投标编号是否已存在（排除当前投标）
        if (checkBidCodeExists(req.getBidCode(), req.getBidId())) {
            throw new RuntimeException("投标编号已存在");
        }

        Bid bid = new Bid();
        bid.setBidId(req.getBidId());
        bid.setBidCode(req.getBidCode());
        bid.setBidName(req.getBidName());
        bid.setBidTotalAmount(req.getBidTotalAmount());
        bid.setCurrency(req.getCurrency());
        bid.setBidSubmitTime(req.getBidSubmitTime());
        bid.setRemark(req.getRemark());
        bid.setUpdatedTime(LocalDateTime.now());

        bidMapper.updateById(bid);

        // 返回更新后的投标信息
        return getBidById(req.getBidId());
    }

    @Override
    @Transactional
    public void deleteBid(Long bidId) {
        // 检查投标是否存在
        Bid bid = bidMapper.selectById(bidId);
        if (bid == null) {
            throw new RuntimeException("投标信息不存在");
        }

        // 检查当前用户是否有删除权限
        CurrentUser currentUser = CurrentUserHolder.get();
        if (!checkDeletePermission(bidId, Long.valueOf(currentUser.getUserId()))) {
            throw new RuntimeException("无权限删除该投标信息");
        }

        // TODO: 检查投标是否有相关联的业务数据，如果有则不允许删除

        bidMapper.deleteById(bidId);
    }

    @Override
    @Transactional
    public void batchDeleteBids(BatchDeleteBidReq req) {
        if (CollectionUtils.isEmpty(req.getBidIds())) {
            return;
        }

        CurrentUser currentUser = CurrentUserHolder.get();
        Long userId = Long.valueOf(currentUser.getUserId());

        // 检查每个投标的删除权限
        for (Long bidId : req.getBidIds()) {
            if (!checkDeletePermission(bidId, userId)) {
                throw new RuntimeException("无权限删除投标ID: " + bidId);
            }
        }

        // TODO: 检查投标是否有相关联的业务数据，如果有则不允许删除

        UpdateWrapper<Bid> updateWrapper = new UpdateWrapper<>();
        updateWrapper.in("bid_id", req.getBidIds());
        bidMapper.delete(updateWrapper);
    }

    @Override
    @Transactional
    public BidDetailRes updateBidStatus(BidStatusReq req) {
        // 检查投标是否存在
        Bid existingBid = bidMapper.selectById(req.getBidId());
        if (existingBid == null) {
            throw new RuntimeException("投标信息不存在");
        }

        Bid bid = new Bid();
        bid.setBidId(req.getBidId());
        bid.setBidStatus(req.getBidStatus());
        bid.setUpdatedTime(LocalDateTime.now());

        bidMapper.updateById(bid);

        // 返回更新后的投标信息
        return getBidById(req.getBidId());
    }

    @Override
    @Transactional
    public void batchUpdateBidStatus(BatchBidStatusReq req) {
        if (CollectionUtils.isEmpty(req.getBidIds())) {
            return;
        }

        bidMapper.batchUpdateStatus(req.getBidIds(), req.getBidStatus());
    }

    @Override
    public boolean checkBidCodeExists(String bidCode, Long excludeBidId) {
        return bidMapper.countByBidCode(bidCode, excludeBidId) > 0;
    }

    @Override
    public List<BidListRes> getBidsByTenderId(Long tenderId) {
        // 仅查本模块表
        List<BidListRes> list = bidMapper.selectBidsByTenderId(tenderId);
        if (!CollectionUtils.isEmpty(list)) {
            fillBidListResNames(list);
        }
        return list;
    }

    @Override
    public boolean checkSupplierBidExists(Long tenderId, Long supplierId, Long excludeBidId) {
        return bidMapper.countByTenderAndSupplier(tenderId, supplierId, excludeBidId) > 0;
    }

    /**
     * 通过 corp/project/auth 模块接口补全列表中的供应商名称、项目名称、创建人姓名（不直接查其他模块表）
     */
    private void fillBidListResNames(List<BidListRes> records) {
        List<Long> supplierIds = records.stream().map(BidListRes::getSupplierId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        List<Long> projectIds = records.stream().map(BidListRes::getProjectId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        List<Long> createdByIds = records.stream().map(BidListRes::getCreatedBy).filter(Objects::nonNull).distinct().collect(Collectors.toList());

        Map<Long, CompanyDetailRes> companyMap = Collections.emptyMap();
        if (!supplierIds.isEmpty()) {
            ApiResponse<Map<Long, CompanyDetailRes>> companyResp = companyClientService.batchDetail(supplierIds);
            if (companyResp != null && companyResp.isSuccess() && companyResp.getData() != null) {
                companyMap = companyResp.getData();
            }
        }
        Map<Long, Project> projectMap = Collections.emptyMap();
        if (!projectIds.isEmpty()) {
            ApiResponse<Map<Long, Project>> projectResp = projectClientService.batchGetProjectByIds(projectIds);
            if (projectResp != null && projectResp.isSuccess() && projectResp.getData() != null) {
                projectMap = projectResp.getData();
            }
        }
        Map<Long, PersonDetailRes> personMap = Collections.emptyMap();
        if (!createdByIds.isEmpty()) {
            try {
                Map<Long, PersonDetailRes> map = personService.batchGetPersonByIds(createdByIds);
                if (map != null) {
                    personMap = map;
                }
            } catch (Exception e) {
                log.warn("批量查询创建人信息失败, createdByIds={}", createdByIds, e);
            }
        }

        final Map<Long, CompanyDetailRes> companyMapFinal = companyMap;
        final Map<Long, Project> projectMapFinal = projectMap;
        final Map<Long, PersonDetailRes> personMapFinal = personMap;
        records.forEach(item -> {
            if (item.getSupplierId() != null) {
                CompanyDetailRes company = companyMapFinal.get(item.getSupplierId());
                item.setSupplierName(company != null ? company.getCompanyName() : null);
            }
            if (item.getProjectId() != null) {
                Project project = projectMapFinal.get(item.getProjectId());
                item.setProjectName(project != null ? project.getProjectName() : null);
            }
            if (item.getCreatedBy() != null) {
                PersonDetailRes person = personMapFinal.get(item.getCreatedBy());
                item.setCreatedByName(person != null ? person.getPersonName() : null);
            }
        });
    }

    /**
     * 通过 corp/project/auth 模块接口补全详情中的供应商名称、项目名称、创建人及部门岗位名称（不直接查其他模块表）
     */
    private void fillBidDetailResNames(BidDetailRes detail) {
        if (detail.getSupplierId() != null) {
            try {
                ApiResponse<CompanyDetailRes> resp = companyClientService.detail(detail.getSupplierId());
                if (resp != null && resp.isSuccess() && resp.getData() != null) {
                    detail.setSupplierName(resp.getData().getCompanyName());
                }
            } catch (Exception e) {
                log.warn("查询供应商(公司)详情失败, supplierId={}", detail.getSupplierId(), e);
            }
        }
        if (detail.getProjectId() != null) {
            try {
                ApiResponse<Project> resp = projectClientService.detail(detail.getProjectId());
                if (resp != null && resp.isSuccess() && resp.getData() != null) {
                    detail.setProjectName(resp.getData().getProjectName());
                }
            } catch (Exception e) {
                log.warn("查询项目详情失败, projectId={}", detail.getProjectId(), e);
            }
        }
        if (detail.getCreatedBy() != null) {
            try {
                PersonDetailRes person = personService.getPersonById(detail.getCreatedBy());
                if (person != null) {
                    detail.setCreatedByName(person.getPersonName());
                    detail.setCreatedDeptName(person.getOrgName());
                    detail.setCreatedPostName(person.getPostName());
                }
            } catch (Exception e) {
                log.warn("查询创建人详情失败, createdBy={}", detail.getCreatedBy(), e);
            }
        }
    }

    @Override
    public boolean checkDeletePermission(Long bidId, Long userId) {
        // 检查投标是否存在
        Bid bid = bidMapper.selectById(bidId);
        if (bid == null) {
            return false;
        }

        // 检查是否为投标创建者
        if (Objects.equals(bid.getCreatedBy(), userId)) {
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
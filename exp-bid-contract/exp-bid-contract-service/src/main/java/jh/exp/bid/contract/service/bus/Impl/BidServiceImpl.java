package jh.exp.bid.contract.service.bus.Impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jh.exp.auth.api.AccountService;
import jh.exp.auth.api.PersonService;
import jh.exp.auth.entity.res.PersonDetailRes;

import jh.exp.bid.contract.entity.ExpBid;
import jh.exp.bid.contract.entity.req.*;
import jh.exp.bid.contract.entity.res.BidDetailRes;
import jh.exp.bid.contract.entity.res.BidListRes;
import jh.exp.bid.contract.mapper.BidMapper;
import jh.exp.bid.contract.service.bus.BidService;
import jh.exp.common.auth.CurrentUserHolder;
import jh.exp.common.auth.dto.CurrentUser;
import jh.exp.common.req.SimplePageReq;
import jh.exp.common.res.SimplePageRes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 投标服务实现类
 */
@Service
@RequiredArgsConstructor
public class BidServiceImpl implements BidService {

    private final BidMapper bidMapper;
    private final PersonService personService;
    private final AccountService accountService;

    @Override
    public SimplePageRes<BidListRes> queryBidList(SimplePageReq<QueryBidReq> req) {
        // 创建分页对象
        Page<BidListRes> page = new Page<>(req.getPageNum(), req.getPageSize());

        QueryBidReq queryParam = req.getQueryParam();
        // 如果前端没有传递查询参数，创建一个默认的空对象
        if (queryParam == null) {
            queryParam = new QueryBidReq();
        }

        // 使用MyBatis-Plus自动分页查询
        IPage<BidListRes> result = bidMapper.selectBidList(page, queryParam);

        // 转换为统一的响应格式
        SimplePageRes<BidListRes> pageRes = new SimplePageRes<>();
        pageRes.setList(result.getRecords());
        pageRes.setTotal(result.getTotal());
        pageRes.setPage(result.getCurrent());
        pageRes.setSize(result.getSize());
        return pageRes;
    }

    @Override
    public BidDetailRes getBidById(Long bidId) {
        BidDetailRes bidDetail = bidMapper.selectBidDetailById(bidId);
        if (bidDetail == null) {
            throw new RuntimeException("投标信息不存在");
        }
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

        ExpBid bid = new ExpBid();
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
        ExpBid existingBid = bidMapper.selectById(req.getBidId());
        if (existingBid == null) {
            throw new RuntimeException("投标信息不存在");
        }

        // 检查投标编号是否已存在（排除当前投标）
        if (checkBidCodeExists(req.getBidCode(), req.getBidId())) {
            throw new RuntimeException("投标编号已存在");
        }

        ExpBid bid = new ExpBid();
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
        ExpBid bid = bidMapper.selectById(bidId);
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

        UpdateWrapper<ExpBid> updateWrapper = new UpdateWrapper<>();
        updateWrapper.in("bid_id", req.getBidIds());
        bidMapper.delete(updateWrapper);
    }

    @Override
    @Transactional
    public BidDetailRes updateBidStatus(BidStatusReq req) {
        // 检查投标是否存在
        ExpBid existingBid = bidMapper.selectById(req.getBidId());
        if (existingBid == null) {
            throw new RuntimeException("投标信息不存在");
        }

        ExpBid bid = new ExpBid();
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
        return bidMapper.selectBidsByTenderId(tenderId);
    }

    @Override
    public boolean checkSupplierBidExists(Long tenderId, Long supplierId, Long excludeBidId) {
        return bidMapper.countByTenderAndSupplier(tenderId, supplierId, excludeBidId) > 0;
    }

    @Override
    public boolean checkDeletePermission(Long bidId, Long userId) {
        // 检查投标是否存在
        ExpBid bid = bidMapper.selectById(bidId);
        if (bid == null) {
            return false;
        }

        // 检查是否为投标创建者
        if (bid.getCreatedBy().equals(userId)) {
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
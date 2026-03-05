package jh.exp.bid.contract.service.service.bus.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jh.exp.auth.clinet.api.bus.AccountService;
import jh.exp.auth.clinet.api.bus.PersonService;
import jh.exp.auth.core.entity.dto.OrgIdAndPersonIdDTO;
import jh.exp.auth.core.entity.req.PersonFlagReq;
import jh.exp.auth.core.entity.res.PersonDetailRes;
import jh.exp.bid.contract.core.constant.BidContractConstant;
import jh.exp.bid.contract.core.entity.Bid;
import jh.exp.bid.contract.core.entity.BidMember;
import jh.exp.bid.contract.core.entity.dto.ManagerAndSalespersonDTO;
import jh.exp.bid.contract.core.entity.req.*;
import jh.exp.bid.contract.core.entity.res.BidDetailRes;
import jh.exp.bid.contract.core.entity.res.BidListRes;
import jh.exp.bid.contract.core.mapper.BidMapper;
import jh.exp.bid.contract.core.mapper.middle.BidMemberMapper;
import jh.exp.bid.contract.core.mapper.middle.TenderMemberMapper;
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
import java.util.*;
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
    private final BidMemberMapper bidMemberMapper;

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
        List<BidMember> bidMembers = bidMemberMapper.selectList(new LambdaQueryWrapper<BidMember>().eq(BidMember::getBidId, bidId));
        bidMembers.forEach(r ->{
            if(r.getRoleInBid().equals(BidContractConstant.BID_CONTRACT_PRINCIPAL)){
                bidDetail.setManagerPersonId(r.getPersonId());
            }
            if(r.getRoleInBid().equals(BidContractConstant.BID_CONTRACT_SALESMAN)){
                bidDetail.setSalesmanId(r.getPersonId());
            }
        });
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
        Long personId = currentUser.getUserId();

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
        bid.setBidStatus(BidContractConstant.BID_CONTRACT_PROJECT_PREPARE); // 新建投标默认为准备状态
        bid.setWinFlag(0); // 默认未中标
        bid.setRemark(req.getRemark());
        bid.setCreatedTime(LocalDateTime.now());
        bid.setUpdatedTime(LocalDateTime.now());

        // 设置创建人相关信息
        bid.setCreatedBy(personId);
        bid.setCreatedDeptId(personDetail.getOrgId());
        bid.setCreatedPostId(personDetail.getPostId());

        bidMapper.insert(bid);
        Long principalId=(req.getPrincipalId()==null||req.getPrincipalId()==0)? null: req.getPrincipalId();
        Map<Long, PersonDetailRes> pmMap = personService.queryProjectManager(List.of(new OrgIdAndPersonIdDTO(req.getOrgId(), principalId)));
        if (pmMap == null || pmMap.isEmpty()) {
            throw new RuntimeException("无法获取负责人信息");
        }
        // 获取 Map 中的第一个值（因为只传入了一个 ID，预期只有一个结果）
        PersonDetailRes personDetailRes = pmMap.get(req.getOrgId());

        BidMember bidMember = new BidMember();
        bidMember.setBidId(bid.getBidId());
        bidMember.setPersonId(personDetailRes.getPersonId());
        bidMember.setOrgId(req.getOrgId());
        bidMember.setPostId(personDetailRes.getPostId());
        bidMember.setRoleInBid(BidContractConstant.BID_CONTRACT_PRINCIPAL);
        bidMember.setIsLeader(1);
        bidMember.setJoinTime(LocalDateTime.now());
        bidMember.setResponsibilityDesc("负责人");
        bidMember.setJoinTime(LocalDateTime.now());
        bidMemberMapper.insert(bidMember);


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
        bid.setRemark(req.getRemark());
        bid.setUpdatedTime(LocalDateTime.now());
        bidMapper.updateById(bid);


        Long principalId=(req.getPrincipalId()==null||req.getPrincipalId()==0)? null: req.getPrincipalId();
        Map<Long, PersonDetailRes> pmMap = personService.queryProjectManager(List.of(new OrgIdAndPersonIdDTO(req.getOrgId(), principalId)));
        if (pmMap == null || pmMap.isEmpty()) {
            throw new RuntimeException("无法获取负责人信息");
        }
        // 获取 Map 中的第一个值（因为只传入了一个 ID，预期只有一个结果）
        PersonDetailRes personDetailRes = pmMap.get(req.getOrgId());

        BidMember bidMember = new BidMember();
        bidMember.setBidId(req.getBidId());
        bidMember.setPersonId(personDetailRes.getPersonId());
        bidMember.setOrgId(req.getOrgId());
        bidMember.setPostId(personDetailRes.getPostId());
        bidMember.setRoleInBid(BidContractConstant.BID_CONTRACT_PRINCIPAL);
        bidMember.setIsLeader(1);
        bidMember.setJoinTime(LocalDateTime.now());
        bidMember.setResponsibilityDesc("负责人");
        bidMemberMapper.updateById(bidMember);

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
        //获取负责人和业务员
        List<Long> bidIdList = records.stream().map(BidListRes::getBidId).filter(Objects::nonNull).distinct().toList();
        List<BidMember> bidMembers = bidMemberMapper.selectList(new LambdaQueryWrapper<BidMember>().in(BidMember::getBidId, bidIdList));

        List<OrgIdAndPersonIdDTO> opIdList = bidMembers.stream()
                .filter(r -> r.getPersonId() != null&& r.getRoleInBid().equals(BidContractConstant.BID_CONTRACT_PRINCIPAL))
                .map(r -> new OrgIdAndPersonIdDTO(r.getOrgId(), r.getPersonId())).toList();

        List<PersonFlagReq> spIdList =bidMembers.stream()
                .filter(r -> r.getPersonId() != null&& r.getRoleInBid().equals(BidContractConstant.BID_CONTRACT_SALESMAN))
                .map(r -> new PersonFlagReq(String.valueOf(r.getOrgId()), String.valueOf(r.getOrgId()))).toList();


        Map<Long, PersonDetailRes> managerPersonResMap = personService.queryProjectManager(opIdList);
        Map<Long, PersonDetailRes> salesPersonResMap = personService.batchFlagPersonByIds(spIdList);


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
        final Map<Long, PersonDetailRes> managerPersonResMapFinal = managerPersonResMap;
        final Map<Long, PersonDetailRes> salesPersonResMapFinal = salesPersonResMap;
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

            if(item.getOrgId()!=null){
                PersonDetailRes managerPerson = managerPersonResMapFinal.get(item.getOrgId());
                item.setOrgIdName(managerPerson != null ? managerPerson.getOrgName() : null);
                item.setManagerPersonName(managerPerson != null ? managerPerson.getPersonName() : null);

                PersonDetailRes salesPerson = salesPersonResMapFinal.get(item.getOrgId());
                item.setSalesmanName(salesPerson != null ? salesPerson.getPersonName() : null);
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

        ArrayList<Long> ids = new ArrayList<>();
        if(detail.getCreatedBy() != null){
            ids.add(detail.getCreatedBy());
        }
        if (detail.getManagerPersonId() != null){
            ids.add(detail.getManagerPersonId());
        }
        if (detail.getSalesmanId() != null){
            ids.add(detail.getSalesmanId());
        }
        Map<Long, PersonDetailRes> personMap=null;
        try{
            personMap= personService.batchGetPersonByIds(ids);
        }catch (Exception e){
             log.warn("批量查询创建人信息失败, ids={}", ids, e);
        }
        if (personMap != null) {
            PersonDetailRes person = personMap.get(detail.getCreatedBy());
            detail.setCreatedByName(person != null ? person.getPersonName() : null);
            detail.setCreatedDeptName(person != null ? person.getOrgName() : null);
            detail.setCreatedPostName(person != null ? person.getPostName() : null);

            person = personMap.get(detail.getManagerPersonId());
            detail.setManagerPersonName(person != null ? person.getPersonName() : null);
            person = personMap.get(detail.getSalesmanId());
            detail.setSalesmanName(person != null ? person.getPersonName() : null);
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

    @Override
    @Transactional
    public void bindSalesman(BindBidSalesmanReq req) {
        Bid bid = bidMapper.selectById(req.getBidId());
        if (bid == null) {
            throw new RuntimeException("投标信息不存在");
        }

        PersonDetailRes salesman = personService.getPersonById(req.getSalesmanId());
        if (salesman == null) {
            throw new RuntimeException("业务员信息不存在");
        }

        BidMember existMember = bidMemberMapper.selectOne(new LambdaQueryWrapper<BidMember>()
                .eq(BidMember::getBidId, req.getBidId())
                .eq(BidMember::getRoleInBid, BidContractConstant.BID_CONTRACT_SALESMAN)
                .last("LIMIT 1"));

        LocalDateTime now = LocalDateTime.now();
        if (existMember == null) {
            BidMember bidMember = new BidMember();
            bidMember.setBidId(req.getBidId());
            bidMember.setPersonId(req.getSalesmanId());
            bidMember.setOrgId(salesman.getOrgId());
            bidMember.setPostId(salesman.getPostId());
            bidMember.setRoleInBid(BidContractConstant.BID_CONTRACT_SALESMAN);
            bidMember.setIsLeader(0);
            bidMember.setJoinTime(now);
            bidMember.setResponsibilityDesc("业务员");
            bidMemberMapper.insert(bidMember);
            return;
        }

        existMember.setPersonId(req.getSalesmanId());
        existMember.setOrgId(salesman.getOrgId());
        existMember.setPostId(salesman.getPostId());
        existMember.setIsLeader(0);
        existMember.setJoinTime(now);
        existMember.setResponsibilityDesc("业务员");
        bidMemberMapper.updateById(existMember);
    }
}
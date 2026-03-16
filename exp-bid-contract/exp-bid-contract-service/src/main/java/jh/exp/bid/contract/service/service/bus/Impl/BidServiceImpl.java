package jh.exp.bid.contract.service.service.bus.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jh.exp.auth.clinet.api.bus.PersonService;
import jh.exp.auth.core.entity.dto.OrgIdAndPersonIdDTO;
import jh.exp.auth.core.entity.req.PersonFlagReq;
import jh.exp.auth.core.entity.res.PersonDetailRes;
import jh.exp.bid.contract.core.constant.BidContractConstant;
import jh.exp.bid.contract.core.entity.Bid;
import jh.exp.bid.contract.core.entity.BidMember;
import jh.exp.bid.contract.core.entity.req.*;
import jh.exp.bid.contract.core.entity.res.BidDetailRes;
import jh.exp.bid.contract.core.entity.res.BidListRes;
import jh.exp.bid.contract.core.mapper.BidMapper;
import jh.exp.bid.contract.core.mapper.middle.BidMemberMapper;
import jh.exp.bid.contract.service.service.bus.BidService;
import jh.exp.common.core.auth.CurrentUserHolder;
import jh.exp.common.core.auth.dto.CurrentUser;
import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.corp.client.api.CompanyClientService;
import jh.exp.corp.core.entity.req.QueryCompanyReq;
import jh.exp.corp.core.entity.res.CompanyDetailRes;
import jh.exp.corp.core.entity.res.CompanyListRes;
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
    private static final int NAME_QUERY_PAGE_SIZE = 200;
    private static final int NAME_QUERY_MAX_PAGE = 20;

    private static final Set<String> BID_STATUS_SET = Set.of(
            BidContractConstant.BID_CONTRACT_PROJECT_PREPARE,
            BidContractConstant.BID_CONTRACT_PROJECT_SUBMITTED,
            BidContractConstant.BID_CONTRACT_PROJECT_EVALUATING,
            BidContractConstant.BID_CONTRACT_PROJECT_WON,
            BidContractConstant.BID_CONTRACT_PROJECT_LOST,
            BidContractConstant.BID_CONTRACT_PROJECT_ABANDONED
    );
    private static final Map<String, Set<String>> BID_STATUS_TRANSITIONS = Map.of(
            BidContractConstant.BID_CONTRACT_PROJECT_PREPARE, Set.of(
                    BidContractConstant.BID_CONTRACT_PROJECT_SUBMITTED,
                    BidContractConstant.BID_CONTRACT_PROJECT_ABANDONED
            ),
            BidContractConstant.BID_CONTRACT_PROJECT_SUBMITTED, Set.of(
                    BidContractConstant.BID_CONTRACT_PROJECT_EVALUATING,
                    BidContractConstant.BID_CONTRACT_PROJECT_ABANDONED
            ),
            BidContractConstant.BID_CONTRACT_PROJECT_EVALUATING, Set.of(
                    BidContractConstant.BID_CONTRACT_PROJECT_WON,
                    BidContractConstant.BID_CONTRACT_PROJECT_LOST,
                    BidContractConstant.BID_CONTRACT_PROJECT_ABANDONED
            ),
            BidContractConstant.BID_CONTRACT_PROJECT_WON, Set.of(),
            BidContractConstant.BID_CONTRACT_PROJECT_LOST, Set.of(),
            BidContractConstant.BID_CONTRACT_PROJECT_ABANDONED, Set.of()
    );

    private final BidMapper bidMapper;
    private final PersonService personService;
    private final CompanyClientService companyClientService;
    private final ProjectClientService projectClientService;
    private final BidMemberMapper bidMemberMapper;

    @Override
    public SimplePageRes<BidListRes> queryBidList(SimplePageReq<QueryBidReq> req) {
        req.pageDefault();
        Page<BidListRes> page = new Page<>(req.getPageNum(), req.getPageSize());
        QueryBidReq queryParam = req.getQueryParam() != null ? req.getQueryParam() : new QueryBidReq();
        queryParam.setPurchaserName(trimToNull(queryParam.getPurchaserName()));
        queryParam.setTenderName(trimToNull(queryParam.getTenderName()));
        queryParam.setProjectName(trimToNull(queryParam.getProjectName()));

        List<Long> purchaserIds = resolvePurchaserIdsByName(queryParam.getPurchaserName());
        if (queryParam.getPurchaserName() != null && CollectionUtils.isEmpty(purchaserIds)) {
            return emptyPage(req);
        }
        List<Long> projectIds = resolveProjectIdsByName(queryParam.getProjectName());
        if (queryParam.getProjectName() != null && CollectionUtils.isEmpty(projectIds)) {
            return emptyPage(req);
        }

        // 仅查本模块表（exp_bid、exp_tender）
        IPage<BidListRes> result = bidMapper.selectBidList(page, queryParam, purchaserIds, projectIds);
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

    /**
     * 查询名称类筛选无命中时，直接返回空分页，避免无效数据库扫描。
     */
    private SimplePageRes<BidListRes> emptyPage(SimplePageReq<?> req) {
        SimplePageRes<BidListRes> pageRes = new SimplePageRes<>();
        pageRes.setList(Collections.emptyList());
        pageRes.setTotal(0L);
        pageRes.setPage((long) req.getPageNum());
        pageRes.setSize((long) req.getPageSize());
        return pageRes;
    }

    /**
     * 将招标单位名称转换为 company_id 集合，用于后续 SQL IN 过滤。
     */
    private List<Long> resolvePurchaserIdsByName(String purchaserName) {
        if (purchaserName == null) {
            return null;
        }
        QueryCompanyReq companyQuery = new QueryCompanyReq();
        companyQuery.setCompanyName(purchaserName);
        Set<Long> companyIds = new LinkedHashSet<>();
        for (int pageNum = 1; pageNum <= NAME_QUERY_MAX_PAGE; pageNum++) {
            SimplePageReq<QueryCompanyReq> pageReq = new SimplePageReq<>();
            pageReq.setPageNum(pageNum);
            pageReq.setPageSize(NAME_QUERY_PAGE_SIZE);
            pageReq.setQueryParam(companyQuery);
            ApiResponse<SimplePageRes<CompanyListRes>> companyResp = companyClientService.list(pageReq);
            if (companyResp == null || !companyResp.isSuccess() || companyResp.getData() == null
                    || CollectionUtils.isEmpty(companyResp.getData().getList())) {
                break;
            }
            companyResp.getData().getList().stream()
                    .map(CompanyListRes::getCompanyId)
                    .filter(Objects::nonNull)
                    .forEach(companyIds::add);

            if (companyResp.getData().getList().size() < NAME_QUERY_PAGE_SIZE) {
                break;
            }
        }
        return new ArrayList<>(companyIds);
    }

    /**
     * 将项目名称转换为 project_id 集合，用于后续 SQL IN 过滤。
     */
    private List<Long> resolveProjectIdsByName(String projectName) {
        if (projectName == null) {
            return null;
        }
        Set<Long> projectIds = new LinkedHashSet<>();
        String keyword = projectName.toLowerCase(Locale.ROOT);
        for (int pageNum = 1; pageNum <= NAME_QUERY_MAX_PAGE; pageNum++) {
            SimplePageReq<Object> pageReq = new SimplePageReq<>();
            pageReq.setPageNum(pageNum);
            pageReq.setPageSize(NAME_QUERY_PAGE_SIZE);
            pageReq.setQueryParam(null);
            ApiResponse<SimplePageRes<Project>> projectResp = projectClientService.list(pageReq);
            if (projectResp == null || !projectResp.isSuccess() || projectResp.getData() == null
                    || CollectionUtils.isEmpty(projectResp.getData().getList())) {
                break;
            }
            projectResp.getData().getList().stream()
                    .filter(Objects::nonNull)
                    .filter(project -> trimToNull(project.getProjectName()) != null)
                    .filter(project -> project.getProjectName().toLowerCase(Locale.ROOT).contains(keyword))
                    .map(Project::getProjectId)
                    .filter(Objects::nonNull)
                    .forEach(projectIds::add);

            if (projectResp.getData().getList().size() < NAME_QUERY_PAGE_SIZE) {
                break;
            }
        }
        return new ArrayList<>(projectIds);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    @Override
    public BidDetailRes getBidById(Long bidId) {
        // 仅查本模块表
        BidDetailRes bidDetail = bidMapper.selectBidDetailById(bidId);
        if (bidDetail == null) {
            throw new RuntimeException("投标信息不存在");
        }
        List<BidMember> bidMembers = bidMemberMapper.selectList(new LambdaQueryWrapper<BidMember>().eq(BidMember::getBidId, bidId));
        bidMembers.forEach(r -> {
            if (BidContractConstant.BID_CONTRACT_PRINCIPAL.equals(r.getRoleInBid())) {
                bidDetail.setManagerPersonId(r.getPersonId());
            }
            if (BidContractConstant.BID_CONTRACT_SALESMAN.equals(r.getRoleInBid())) {
                bidDetail.setSalesmanId(r.getPersonId());
            }
        });
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
        ApiResponse<PersonDetailRes> personResp = personService.getPersonById(personId);
        PersonDetailRes personDetail = (personResp != null && personResp.isSuccess()) ? personResp.getData() : null;

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
        bid.setOrgId(req.getOrgId());
        bid.setRemark(req.getRemark());
        bid.setCreatedTime(LocalDateTime.now());
        bid.setUpdatedTime(LocalDateTime.now());

        // 设置创建人相关信息
        bid.setCreatedBy(personId);
        bid.setCreatedDeptId(personDetail.getOrgId());
        bid.setCreatedPostId(personDetail.getPostId());

        bidMapper.insert(bid);
        PersonDetailRes principal = queryPrincipal(req.getOrgId(), req.getPrincipalId());
        upsertRoleMember(bid.getBidId(), req.getOrgId(), principal, BidContractConstant.BID_CONTRACT_PRINCIPAL, 1, "负责人");
        bid.setBidId(bid.getBidId());
        bid.setPrincipalPersonId(principal.getPersonId());
        if (req.getSalesmanId() != null && req.getSalesmanId() > 0) {
            PersonDetailRes salesman = queryPersonById(req.getSalesmanId(), "业务员信息不存在");
            upsertRoleMember(bid.getBidId(), salesman.getOrgId(), salesman, BidContractConstant.BID_CONTRACT_SALESMAN, 0, "业务员");
            bid.setSalesmanPersonId(salesman.getPersonId());
        } else {
            bid.setSalesmanPersonId(null);
        }
        bid.setUpdatedTime(LocalDateTime.now());
        bidMapper.updateById(bid);


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
        bid.setOrgId(req.getOrgId());
        bid.setRemark(req.getRemark());
        bid.setUpdatedTime(LocalDateTime.now());
        bidMapper.updateById(bid);


        PersonDetailRes principal = queryPrincipal(req.getOrgId(), req.getPrincipalId());
        upsertRoleMember(req.getBidId(), req.getOrgId(), principal, BidContractConstant.BID_CONTRACT_PRINCIPAL, 1, "负责人");

        Bid updateFields = new Bid();
        updateFields.setBidId(req.getBidId());
        updateFields.setPrincipalPersonId(principal.getPersonId());
        if (req.getSalesmanId() != null && req.getSalesmanId() > 0) {
            PersonDetailRes salesman = queryPersonById(req.getSalesmanId(), "业务员信息不存在");
            upsertRoleMember(req.getBidId(), salesman.getOrgId(), salesman, BidContractConstant.BID_CONTRACT_SALESMAN, 0, "业务员");
            updateFields.setSalesmanPersonId(salesman.getPersonId());
        }
        updateFields.setUpdatedTime(LocalDateTime.now());
        bidMapper.updateById(updateFields);

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
        if (!checkDeletePermission(bidId, currentUser.getUserId())) {
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
        Long userId = currentUser.getUserId();

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
        fillStatusTransitionFields(existingBid, req.getBidStatus(), bid);

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
        for (Long bidId : req.getBidIds()) {
            Bid existingBid = bidMapper.selectById(bidId);
            if (existingBid == null) {
                throw new RuntimeException("投标信息不存在, bidId=" + bidId);
            }
            Bid bid = new Bid();
            bid.setBidId(bidId);
            fillStatusTransitionFields(existingBid, req.getBidStatus(), bid);
            bidMapper.updateById(bid);
        }
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
        List<BidMember> bidMembers = CollectionUtils.isEmpty(bidIdList)
                ? Collections.emptyList()
                : bidMemberMapper.selectList(new LambdaQueryWrapper<BidMember>().in(BidMember::getBidId, bidIdList));
        Map<Long, BidMember> principalByBidId = bidMembers.stream()
                .filter(r -> BidContractConstant.BID_CONTRACT_PRINCIPAL.equals(r.getRoleInBid()))
                .collect(Collectors.toMap(BidMember::getBidId, r -> r, (a, b) -> a));
        Map<Long, BidMember> salesmanByBidId = bidMembers.stream()
                .filter(r -> BidContractConstant.BID_CONTRACT_SALESMAN.equals(r.getRoleInBid()))
                .collect(Collectors.toMap(BidMember::getBidId, r -> r, (a, b) -> a));

        List<OrgIdAndPersonIdDTO> opIdList = bidMembers.stream()
                .filter(r -> r.getPersonId() != null && BidContractConstant.BID_CONTRACT_PRINCIPAL.equals(r.getRoleInBid()))
                .map(r -> new OrgIdAndPersonIdDTO(r.getOrgId(), r.getPersonId())).toList();

        List<PersonFlagReq> spIdList = bidMembers.stream()
                .filter(r -> r.getPersonId() != null && BidContractConstant.BID_CONTRACT_SALESMAN.equals(r.getRoleInBid()))
                // flag 作为返回映射标识，按你的语义继续使用 orgId；personId 传真实人员ID用于查询
                .map(r -> new PersonFlagReq(String.valueOf(r.getOrgId()), String.valueOf(r.getPersonId()))).toList();


        Map<Long, PersonDetailRes> managerPersonResMap = Collections.emptyMap();
        if (!CollectionUtils.isEmpty(opIdList)) {
            Map<Long, PersonDetailRes> managerMap = personService.queryProjectManager(opIdList);
            if (managerMap != null) {
                managerPersonResMap = managerMap;
            }
        }
        Map<Long, PersonDetailRes> salesPersonResMap = Collections.emptyMap();
        if (!CollectionUtils.isEmpty(spIdList)) {
            Map<Long, PersonDetailRes> salesMap = personService.batchFlagPersonByIds(spIdList);
            if (salesMap != null) {
                salesPersonResMap = salesMap;
            }
        }


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
            BidMember principalMember = principalByBidId.get(item.getBidId());
            if (principalMember != null) {
                item.setOrgId(principalMember.getOrgId());
                item.setManagerPersonId(principalMember.getPersonId());
            } else {
                item.setOrgId(null);
                item.setManagerPersonId(null);
            }
            BidMember salesmanMember = salesmanByBidId.get(item.getBidId());
            item.setSalesmanId(salesmanMember != null ? salesmanMember.getPersonId() : null);

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

            if (item.getOrgId() != null) {
                PersonDetailRes managerPerson = managerPersonResMapFinal.get(item.getOrgId());
                item.setOrgIdName(managerPerson != null ? managerPerson.getOrgName() : null);
                item.setManagerPersonName(managerPerson != null ? managerPerson.getPersonName() : null);
            }
            Long salesmanOrgId = salesmanMember != null ? salesmanMember.getOrgId() : null;
            PersonDetailRes salesPerson = salesmanOrgId != null ? salesPersonResMapFinal.get(salesmanOrgId) : null;
            item.setSalesmanName(salesPerson != null ? salesPerson.getPersonName() : null);
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
        Map<Long, PersonDetailRes> personMap = Collections.emptyMap();
        if (!ids.isEmpty()) {
            try {
                Map<Long, PersonDetailRes> map = personService.batchGetPersonByIds(ids);
                if (map != null) {
                    personMap = map;
                }
            } catch (Exception e) {
                log.warn("批量查询创建人信息失败, ids={}", ids, e);
            }
        }
        PersonDetailRes person = personMap.get(detail.getCreatedBy());
        detail.setCreatedByName(person != null ? person.getPersonName() : null);
        detail.setCreatedDeptName(person != null ? person.getOrgName() : null);
        detail.setCreatedPostName(person != null ? person.getPostName() : null);

        person = personMap.get(detail.getManagerPersonId());
        detail.setManagerPersonName(person != null ? person.getPersonName() : null);
        person = personMap.get(detail.getSalesmanId());
        detail.setSalesmanName(person != null ? person.getPersonName() : null);
    }

    @Override
    public boolean checkDeletePermission(Long bidId, Long personId) {
        // 检查投标是否存在
        Bid bid = bidMapper.selectById(bidId);
        if (bid == null) {
            return false;
        }

        // 先按人员ID判断是否为投标创建者（当前模块 created_by 存人员ID）
        if (Objects.equals(bid.getCreatedBy(), personId)) {
            return true;
        }

        return false;
    }

    private PersonDetailRes queryPrincipal(Long orgId, Long principalId) {
        if (orgId == null) {
            throw new RuntimeException("归属组织不能为空");
        }
        Long fixedPrincipalId = (principalId == null || principalId == 0) ? null : principalId;
        Map<Long, PersonDetailRes> pmMap = personService.queryProjectManager(List.of(new OrgIdAndPersonIdDTO(orgId, fixedPrincipalId)));
        if (pmMap == null || pmMap.isEmpty()) {
            throw new RuntimeException("无法获取负责人信息");
        }
        PersonDetailRes principal = pmMap.get(orgId);
        if (principal == null || principal.getPersonId() == null) {
            throw new RuntimeException("负责人信息缺失");
        }
        return principal;
    }

    @Override
    @Transactional
    public void bindSalesman(BindBidSalesmanReq req) {
        Bid bid = bidMapper.selectById(req.getBidId());
        if (bid == null) {
            throw new RuntimeException("投标信息不存在");
        }

        ApiResponse<PersonDetailRes> salesmanResp = personService.getPersonById(req.getSalesmanId());
        PersonDetailRes salesman = (salesmanResp != null && salesmanResp.isSuccess()) ? salesmanResp.getData() : null;
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

    private PersonDetailRes queryPersonById(Long personId, String errMsg) {
        if (personId == null || personId <= 0) {
            throw new RuntimeException(errMsg);
        }
        ApiResponse<PersonDetailRes> resp = personService.getPersonById(personId);
        PersonDetailRes person = (resp != null && resp.isSuccess()) ? resp.getData() : null;
        if (person == null || person.getPersonId() == null) {
            throw new RuntimeException(errMsg);
        }
        return person;
    }

    private void upsertRoleMember(Long bidId, Long orgId, PersonDetailRes person, String role, Integer isLeader, String responsibilityDesc) {
        BidMember existMember = bidMemberMapper.selectOne(new LambdaQueryWrapper<BidMember>()
                .eq(BidMember::getBidId, bidId)
                .eq(BidMember::getRoleInBid, role)
                .last("LIMIT 1"));
        if (existMember == null) {
            existMember = new BidMember();
            existMember.setBidId(bidId);
            existMember.setRoleInBid(role);
        }
        existMember.setPersonId(person.getPersonId());
        existMember.setOrgId(orgId);
        existMember.setPostId(person.getPostId());
        existMember.setIsLeader(isLeader);
        existMember.setJoinTime(LocalDateTime.now());
        existMember.setResponsibilityDesc(responsibilityDesc);
        if (existMember.getId() == null) {
            bidMemberMapper.insert(existMember);
        } else {
            bidMemberMapper.updateById(existMember);
        }
    }

    private void fillStatusTransitionFields(Bid existingBid, String targetStatus, Bid updateBid) {
        if (!BID_STATUS_SET.contains(targetStatus)) {
            throw new RuntimeException("非法投标状态: " + targetStatus);
        }
        String currentStatus = existingBid.getBidStatus();
        if (currentStatus != null && !Objects.equals(currentStatus, targetStatus)) {
            Set<String> nextSet = BID_STATUS_TRANSITIONS.get(currentStatus);
            if (nextSet == null || !nextSet.contains(targetStatus)) {
                throw new RuntimeException("不允许的状态流转: " + currentStatus + " -> " + targetStatus);
            }
        }
        updateBid.setBidStatus(targetStatus);
        updateBid.setWinFlag(BidContractConstant.BID_CONTRACT_PROJECT_WON.equals(targetStatus) ? 1 : 0);
        updateBid.setUpdatedTime(LocalDateTime.now());
    }
}
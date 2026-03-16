package jh.exp.bid.contract.service.service.bus.Impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jh.exp.auth.clinet.api.bus.AccountService;
import jh.exp.auth.clinet.api.bus.OrgUnitService;
import jh.exp.auth.clinet.api.bus.PersonService;
import jh.exp.auth.core.entity.dto.OrgIdAndPersonIdDTO;
import jh.exp.auth.core.entity.req.PersonFlagReq;
import jh.exp.auth.core.entity.res.OrgUnitDetailRes;
import jh.exp.auth.core.entity.res.PersonDetailRes;
import jh.exp.bid.contract.core.constant.BidContractConstant;
import jh.exp.bid.contract.core.constant.BidEvaluationFlowConstant;
import jh.exp.bid.contract.core.entity.Tender;
import jh.exp.bid.contract.core.entity.middle.TenderMember;
import jh.exp.bid.contract.core.entity.req.*;
import jh.exp.bid.contract.core.entity.res.TenderDetailRes;
import jh.exp.bid.contract.core.entity.dto.TenderLisDTO;
import jh.exp.bid.contract.core.entity.res.TenderListRes;
import jh.exp.bid.contract.core.mapper.TenderMapper;
//import jh.exp.bid.contract.core.mapper.middle.TenderMemberMapper;
import jh.exp.bid.contract.core.mapper.middle.TenderMemberMapper;
import jh.exp.bid.contract.service.service.bus.TenderService;
import jh.exp.bid.contract.service.service.bus.support.EvaluationFlowEligibilityService;
import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.auth.CurrentUserHolder;
import jh.exp.common.core.auth.dto.CurrentUser;
import jh.exp.common.core.constant.CommonConstant;
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
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 招标服务实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TenderServiceImpl implements TenderService {
    private static final int COMPANY_QUERY_PAGE_SIZE = 200;
    private static final int COMPANY_QUERY_MAX_PAGE = 20;

    private final TenderMapper tenderMapper;
    private final TenderMemberMapper tenderMemberMapper;

    private final PersonService personService;
    private final OrgUnitService orgUnitService;
    private final AccountService accountService;

    private final CompanyClientService companyClientService;
    private final ProjectClientService projectClientService;
    private final EvaluationFlowEligibilityService eligibilityService;



    @Override
    public SimplePageRes<TenderListRes> queryTenderList(SimplePageReq<QueryTenderReq> req) {
        Page<TenderLisDTO> page = new Page<>(req.getPageNum(), req.getPageSize());
        QueryTenderReq queryParam = req.getQueryParam();
        if (queryParam == null) {
            queryParam = new QueryTenderReq();
        }

        IPage<TenderLisDTO> result = tenderMapper.selectTenderList(page, queryParam);
        return buildTenderListPageRes(result);
    }

    @Override
    public SimplePageRes<TenderListRes> queryEvaluationEligibleTenderList(SimplePageReq<QueryTenderReq> req) {
        req.pageDefault();
        Page<TenderLisDTO> page = new Page<>(req.getPageNum(), req.getPageSize());
        QueryTenderReq queryParam = req.getQueryParam();
        if (queryParam == null) {
            queryParam = new QueryTenderReq();
        }

        List<Long> eligiblePurchaserIds = resolveEvaluationEligiblePurchaserIds();
        if (CollectionUtils.isEmpty(eligiblePurchaserIds)) {
            return new SimplePageRes<>(0L, (long) req.getPageNum(), (long) req.getPageSize(), Collections.emptyList());
        }

        IPage<TenderLisDTO> result = tenderMapper.selectTenderListByPurchaserIds(page, queryParam, eligiblePurchaserIds);
        return buildTenderListPageRes(result);
    }

    @Override
    public boolean checkEvaluationFlowEligible(Long tenderId) {
        Tender tender = tenderMapper.selectById(tenderId);
        if (tender == null || tender.getCompanyId() == null) {
            return false;
        }
        try {
            return eligibilityService.isCompanyEligible(tender.getCompanyId());
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private SimplePageRes<TenderListRes> buildTenderListPageRes(IPage<TenderLisDTO> result) {
        List<TenderLisDTO> records = result.getRecords();
        List<TenderListRes> resList = records.stream().map(dto -> {
            TenderListRes res = new TenderListRes();
            BeanUtils.copyProperties(dto, res); // 此时字段名已对齐，可自动复制
            return res;
        }).toList();

        if (!CollectionUtils.isEmpty(records)) {
            //拼接招标方
            List<Long> purchaserIds = records.stream().map(TenderLisDTO::getPurchaserId).toList();
            Map<Long, CompanyDetailRes> companyDetailResMap = companyClientService.batchDetail(purchaserIds).getData();

            //拼接负责人 查询组织的部门负责人，对比传入id与负责人id，一致返回部门负责人信息，不一致返回传入id的人员信息
            ArrayList<OrgIdAndPersonIdDTO> orgIdAndPersonIdDTOs = new ArrayList<>();
            records.forEach(record -> {
                if (record.getOrgId() != null) {
                    OrgIdAndPersonIdDTO orgIdAndPersonIdDTO = new OrgIdAndPersonIdDTO();
                    orgIdAndPersonIdDTO.setOrgId(record.getOrgId());
                    orgIdAndPersonIdDTO.setPersonId(record.getPersonId());
                    orgIdAndPersonIdDTOs.add(orgIdAndPersonIdDTO);
                }
            });
            Map<Long, PersonDetailRes> orgIdPersonMap = personService.queryProjectManager(orgIdAndPersonIdDTOs);

            //拼接业务员
            List<PersonFlagReq> personFlagReqs = records.stream()
                    .filter(r -> r.getSalesmanId() != null)
                    .map(r -> {
                        PersonFlagReq Preq = new PersonFlagReq();
                        Preq.setFlag(String.valueOf(r.getTenderId()));
                        Preq.setPersonId(String.valueOf(r.getSalesmanId()));
                        return Preq;
                    }).toList();

            Map<Long, PersonDetailRes> busPersonMap = personService.batchFlagPersonByIds(personFlagReqs);


            //拼接项目信息
            List<Long> projectIds = records.stream().map(TenderLisDTO::getProjectId).toList();
            Map<Long, Project> projectDetailResMap = projectClientService.batchGetProjectByIds(projectIds).getData();


            for (TenderListRes item : resList) {
                // 设置招标方名称
                if (item.getPurchaserId() != null) {
                    String companyName = companyDetailResMap.get(item.getPurchaserId()).getCompanyName();
                    item.setPurchaserName(companyName);
                }

                // 设置负责人信息（包括姓名和组织名称）
                if (item.getOrgId() != null) {
                    PersonDetailRes person = orgIdPersonMap.get(item.getOrgId());
                    item.setPersonIdName(person.getPersonName());
                    item.setOrgName(person.getOrgName());
                }

                // 设置业务员名称
                if (item.getSalesmanId() != null) {
                    String personName = busPersonMap.get(item.getTenderId()).getPersonName();
                    item.setSalesmanName(personName);
                }

                // 设置项目名称
                if (item.getProjectId() != null) {
                    String projectName = projectDetailResMap.get(item.getProjectId()).getProjectName();
                    item.setProjectName(projectName);
                }


            }
        }

        SimplePageRes<TenderListRes> pageRes = new SimplePageRes<>();
        pageRes.setList(resList);
        pageRes.setTotal(result.getTotal());
        pageRes.setPage(result.getCurrent());
        pageRes.setSize(result.getSize());
        return pageRes;
    }

    private List<Long> resolveEvaluationEligiblePurchaserIds() {
        Set<Long> purchaserIds = new LinkedHashSet<>();
        purchaserIds.addAll(queryEligibleCompanyIdsByName());
        purchaserIds.addAll(queryEligibleCompanyIdsByType());
        return new ArrayList<>(purchaserIds);
    }

    private Set<Long> queryEligibleCompanyIdsByName() {
        QueryCompanyReq companyReq = new QueryCompanyReq();
        companyReq.setCompanyName(BidEvaluationFlowConstant.FLOW_COMPANY_NAME);
        return queryCompanyIds(companyReq);
    }

    private Set<Long> queryEligibleCompanyIdsByType() {
        QueryCompanyReq companyReq = new QueryCompanyReq();
        companyReq.setCompanyType(BidEvaluationFlowConstant.FLOW_COMPANY_TYPE_SELF);
        return queryCompanyIds(companyReq);
    }

    private Set<Long> queryCompanyIds(QueryCompanyReq companyReq) {
        Set<Long> ids = new LinkedHashSet<>();
        for (int pageNum = 1; pageNum <= COMPANY_QUERY_MAX_PAGE; pageNum++) {
            SimplePageReq<QueryCompanyReq> pageReq = new SimplePageReq<>();
            pageReq.setPageNum(pageNum);
            pageReq.setPageSize(COMPANY_QUERY_PAGE_SIZE);
            pageReq.setQueryParam(companyReq);
            ApiResponse<SimplePageRes<CompanyListRes>> response = companyClientService.list(pageReq);
            if (response == null || !response.isSuccess() || response.getData() == null) {
                throw new RuntimeException("查询公司列表失败，无法筛选可进入评标/定标流程的招标");
            }
            if (CollectionUtils.isEmpty(response.getData().getList())) {
                break;
            }
            response.getData().getList().stream()
                    .filter(this::isEvaluationFlowCompany)
                    .map(CompanyListRes::getCompanyId)
                    .filter(Objects::nonNull)
                    .forEach(ids::add);

            if (response.getData().getList().size() < COMPANY_QUERY_PAGE_SIZE) {
                break;
            }
        }
        return ids;
    }

    private boolean isEvaluationFlowCompany(CompanyListRes company) {
        if (company == null) {
            return false;
        }
        String companyName = safeTrim(company.getCompanyName());
        String companyType = safeTrim(company.getCompanyType());
        return BidEvaluationFlowConstant.FLOW_COMPANY_NAME.equals(companyName)
                || BidEvaluationFlowConstant.FLOW_COMPANY_TYPE_SELF.equalsIgnoreCase(companyType);
    }

    private String safeTrim(String source) {
        return source == null ? null : source.trim();
    }

    @Override
    public TenderDetailRes getTenderById(Long tenderId) {
        TenderDetailRes tenderDetail = tenderMapper.selectTenderDetailById(tenderId);
        if (tenderDetail == null) {
            throw new RuntimeException("招标信息不存在");
        }
        enrichTenderDetailOrThrow(tenderDetail);
        return tenderDetail;
    }

    @Override
    @Transactional
    public TenderDetailRes createTender(CreateTenderReq req) {
        if (checkTenderCodeExists(req.getTenderCode(), null)) {
            throw new RuntimeException("招标编号已存在");
        }

        // 强校验公司必须存在，避免写入脏数据
        getCompanyNameOrThrow(req.getCompanyId());
//
//        TenderDetailRes projectInfo = getProjectManagerByProjectId(req.getProjectId());
//        if (projectInfo.getProjectId() == null) {
//            throw new RuntimeException("项目信息不存在");
//        }
//        if (projectInfo.getProjectManagerId() == null) {
//            throw new RuntimeException("项目负责人未设置，无法创建招标");
//        }
//        if (projectInfo.getOrgManagerId() == null) {
//            throw new RuntimeException("项目归属组织的负责人未设置，无法创建招标");
//        }

        CurrentUser currentUser = CurrentUserHolder.get();
        Long CreatedById = currentUser.getUserId();
        ApiResponse<PersonDetailRes> personResp = personService.getPersonById(CreatedById);
        PersonDetailRes personDetail = (personResp != null && personResp.isSuccess()) ? personResp.getData() : null;
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
        tender.setTaxRate(req.getTaxRate());
        tender.setIsTaxIncluded(req.getIsTaxIncluded());
        tender.setPurchaseNature(req.getPurchaseNature());
        tender.setCurrency(req.getCurrency());
        tender.setTenderBrief(req.getTenderBrief());
        tender.setPublishTime(req.getPublishTime());
        tender.setBidStartTime(req.getBidStartTime());
        tender.setBidEndTime(req.getBidEndTime());
        tender.setOpenTime(req.getOpenTime());
        tender.setOpenAddress(req.getOpenAddress());
        tender.setProjectId(req.getProjectId());
        tender.setStatus(BidContractConstant.BID_CONTRACT_PROJECT_PREPARE); // 新建招标默认为准备状态
        tender.setRemark(req.getRemark());
        tender.setCreatedTime(LocalDateTime.now());
        tender.setUpdatedTime(LocalDateTime.now());
        tender.setCreatedBy(CreatedById);
        tender.setCreatedDeptId(personDetail.getOrgId());
        tender.setCreatedPostId(personDetail.getPostId());

        tenderMapper.insert(tender);

        Long personId = req.getPersonId();
        Long orgId = req.getOrgId();
        OrgIdAndPersonIdDTO orgIdAndPersonIdDTO = new OrgIdAndPersonIdDTO(orgId, personId);
        Map<Long, PersonDetailRes> longPersonDetailResMap = personService.queryProjectManager(List.of(orgIdAndPersonIdDTO));
        if (longPersonDetailResMap.size() != 1) {
            throw new RuntimeException("项目负责人信息查询错误");
        }
        personId= longPersonDetailResMap.get(orgId).getPersonId();
        TenderMember tenderMember = TenderMember.builder()
                .tenderId(tender.getTenderId())
                .personId(personId)
                .memberRole(BidContractConstant.BID_CONTRACT_PRINCIPAL)
                .orgId(orgId)
//                .postId(projectInfo.getProjectManagerPostId())
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusYears(10))
                .status(CommonConstant.ENABLED_STATUS_STR)
                .createdTime(LocalDateTime.now())
                .updatedTime(LocalDateTime.now())
                .build();
        tenderMemberMapper.insert(tenderMember);

        return getTenderById(tender.getTenderId());
    }

    @Override
    @Transactional
    public TenderDetailRes updateTender(UpdateTenderReq req) {
        Tender existingTender = tenderMapper.selectById(req.getTenderId());
        if (existingTender == null) {
            throw new RuntimeException("招标信息不存在");
        }

        getCompanyNameOrThrow(req.getCompanyId());

        Tender tender = new Tender();
        tender.setTenderId(req.getTenderId());
//        tender.setTenderCode(req.getTenderCode());
        tender.setTenderName(req.getTenderName());
        tender.setTenderType(req.getTenderType());
        tender.setTenderMode(req.getTenderMode());
        tender.setCompanyId(req.getCompanyId());
        tender.setBudgetAmount(req.getBudgetAmount());
        tender.setTaxRate(req.getTaxRate());
        tender.setIsTaxIncluded(req.getIsTaxIncluded());
        tender.setPurchaseNature(req.getPurchaseNature());
//        tender.setCurrency(req.getCurrency());
        tender.setTenderBrief(req.getTenderBrief());
        tender.setPublishTime(req.getPublishTime());
        tender.setBidStartTime(req.getBidStartTime());
        tender.setBidEndTime(req.getBidEndTime());
        tender.setOpenTime(req.getOpenTime());
        tender.setOpenAddress(req.getOpenAddress());
        tender.setRemark(req.getRemark());
        tender.setUpdatedTime(LocalDateTime.now());

        tenderMapper.updateById(tender);
        return getTenderById(req.getTenderId());
    }

    @Override
    @Transactional
    public void deleteTender(Long tenderId) {
        Tender tender = tenderMapper.selectById(tenderId);
        if (tender == null) {
            throw new RuntimeException("招标信息不存在");
        }

        CurrentUser currentUser = CurrentUserHolder.get();
        if (!checkDeletePermission(tenderId, currentUser.getUserId())) {
            throw new RuntimeException("无权限删除该招标信息");
        }

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

        for (Long tenderId : req.getTenderIds()) {
            if (!checkDeletePermission(tenderId, userId)) {
                throw new RuntimeException("无权限删除招标ID: " + tenderId);
            }
        }

        UpdateWrapper<Tender> updateWrapper = new UpdateWrapper<>();
        updateWrapper.in("tender_id", req.getTenderIds());
        tenderMapper.delete(updateWrapper);
    }

    @Override
    @Transactional
    public TenderDetailRes updateTenderStatus(TenderStatusReq req) {
        Tender existingTender = tenderMapper.selectById(req.getTenderId());
        if (existingTender == null) {
            throw new RuntimeException("招标信息不存在");
        }

        Tender tender = new Tender();
        tender.setTenderId(req.getTenderId());
        tender.setStatus(req.getStatus());
        tender.setUpdatedTime(LocalDateTime.now());

        tenderMapper.updateById(tender);
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
        Project project = getProjectOrThrow(projectId, "getProjectManagerByProjectId");

        TenderDetailRes result = new TenderDetailRes();
        result.setProjectId(project.getProjectId());
        result.setProjectName(project.getProjectName());
        result.setProjectManagerId(project.getManagerPersonId());
        result.setProjectOrgId(project.getOrgId());

        if (project.getManagerPersonId() != null) {
            PersonDetailRes projectManager = getPersonOrThrow(project.getManagerPersonId(), "project manager");
            result.setProjectManagerName(projectManager.getPersonName());
        }

        if (project.getOrgId() != null) {
            OrgUnitDetailRes orgDetail = getOrgUnitOrThrow(project.getOrgId(), "project org");
            result.setProjectOrgName(orgDetail.getOrgName());
            result.setOrgManagerId(orgDetail.getManagerPersonId());
            if (orgDetail.getManagerPersonId() != null) {
                PersonDetailRes orgManager = getPersonOrThrow(orgDetail.getManagerPersonId(), "org manager");
                result.setOrgManagerName(orgManager.getPersonName());
            }
        }
        return result;
    }

    @Override
    public boolean checkDeletePermission(Long tenderId, Long userId) {
        Tender tender = tenderMapper.selectById(tenderId);
        if (tender == null) {
            return false;
        }

        if (userId != null && userId.equals(tender.getCreatedBy())) {
            return true;
        }

        try {
            Object accountDetail = accountService.getAccountById(userId);
            if (accountDetail != null) {
                return true;
            }
        } catch (Exception e) {
            log.warn("查询账号权限失败，tenderId={}, userId={}", tenderId, userId, e);
        }

        return false;
    }

    /**
     * 按企业系统约束统一补全跨域展示字段，任一远程调用失败则整体失败。
     */
    private void enrichTenderDetailOrThrow(TenderDetailRes tenderDetail) {
        if (tenderDetail.getPurchaserId() != null) {
            tenderDetail.setPurchaserName(getCompanyNameOrThrow(tenderDetail.getPurchaserId()));
        }

        if (tenderDetail.getProjectId() != null) {
            TenderDetailRes projectInfo = getProjectManagerByProjectId(tenderDetail.getProjectId());
            tenderDetail.setProjectName(projectInfo.getProjectName());
            tenderDetail.setProjectManagerId(projectInfo.getProjectManagerId());
            tenderDetail.setProjectManagerName(projectInfo.getProjectManagerName());
            tenderDetail.setProjectOrgId(projectInfo.getProjectOrgId());
            tenderDetail.setProjectOrgName(projectInfo.getProjectOrgName());
            tenderDetail.setOrgManagerId(projectInfo.getOrgManagerId());
            tenderDetail.setOrgManagerName(projectInfo.getOrgManagerName());
        }

        if (tenderDetail.getCreatedBy() != null) {
            PersonDetailRes creator = getPersonOrThrow(tenderDetail.getCreatedBy(), "tender creator");
            tenderDetail.setCreatedByName(creator.getPersonName());
            // 岗位本次不做强依赖远程查询，优先使用人员详情内已携带字段
            tenderDetail.setCreatedPostName(creator.getPostName());
        }

        if (tenderDetail.getCreatedDeptId() != null) {
            OrgUnitDetailRes createdDept = getOrgUnitOrThrow(tenderDetail.getCreatedDeptId(), "creator org");
            tenderDetail.setCreatedDeptName(createdDept.getOrgName());
        }
    }

    private String getCompanyNameOrThrow(Long companyId) {
        try {
            ApiResponse<CompanyDetailRes> response = companyClientService.detail(companyId);
            if (response == null || !response.isSuccess() || response.getData() == null) {
                log.error("调用corp公司详情失败，companyId={}, response={}", companyId, response);
                throw new RuntimeException("查询公司信息失败");
            }
            return response.getData().getCompanyName();
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("调用corp公司详情异常，companyId={}", companyId, ex);
            throw new RuntimeException("查询公司信息失败");
        }
    }

    private PersonDetailRes getPersonOrThrow(Long personId, String scene) {
        try {
            ApiResponse<PersonDetailRes> resp = personService.getPersonById(personId);
            PersonDetailRes person = (resp != null && resp.isSuccess()) ? resp.getData() : null;
            if (person == null) {
                log.error("调用auth人员详情返回空，scene={}, personId={}", scene, personId);
                throw new RuntimeException("查询人员信息失败");
            }
            return person;
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("调用auth人员详情异常，scene={}, personId={}", scene, personId, ex);
            throw new RuntimeException("查询人员信息失败");
        }
    }

    private OrgUnitDetailRes getOrgUnitOrThrow(Long orgId, String scene) {
        try {
            OrgUnitDetailRes org = orgUnitService.getOrgUnitById(orgId);
            if (org == null) {
                log.error("调用auth组织详情返回空，scene={}, orgId={}", scene, orgId);
                throw new RuntimeException("查询组织信息失败");
            }
            return org;
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("调用auth组织详情异常，scene={}, orgId={}", scene, orgId, ex);
            throw new RuntimeException("查询组织信息失败");
        }
    }

    private Project getProjectOrThrow(Long projectId, String scene) {
        try {
            ApiResponse<Project> response = projectClientService.detail(projectId);
            if (response == null || !response.isSuccess() || response.getData() == null) {
                log.error("调用project详情失败，scene={}, projectId={}, response={}", scene, projectId, response);
                throw new RuntimeException("查询项目信息失败");
            }
            return response.getData();
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("调用project详情异常，scene={}, projectId={}", scene, projectId, ex);
            throw new RuntimeException("查询项目信息失败");
        }
    }

    @Override
    @Transactional
    public void bindSalesman(BindTenderSalesmanReq req) {
        Tender tender = tenderMapper.selectById(req.getTenderId());
        if (tender == null) {
            throw new RuntimeException("招标信息不存在");
        }

        ApiResponse<PersonDetailRes> salesmanResp = personService.getPersonById(req.getSalesmanId());
        PersonDetailRes salesman = (salesmanResp != null && salesmanResp.isSuccess()) ? salesmanResp.getData() : null;
        if (salesman == null) {
            throw new RuntimeException("业务员信息不存在");
        }

        TenderMember existMember = tenderMemberMapper.selectOne(new LambdaQueryWrapper<TenderMember>()
                .eq(TenderMember::getTenderId, req.getTenderId())
                .eq(TenderMember::getMemberRole, BidContractConstant.BID_CONTRACT_SALESMAN)
                .last("LIMIT 1"));

        LocalDateTime now = LocalDateTime.now();
        if (existMember == null) {
            TenderMember tenderMember = TenderMember.builder()
                    .tenderId(req.getTenderId())
                    .personId(req.getSalesmanId())
                    .memberRole(BidContractConstant.BID_CONTRACT_SALESMAN)
                    .orgId(salesman.getOrgId())
                    .postId(salesman.getPostId())
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusYears(10))
                    .status(CommonConstant.ENABLED_STATUS_STR)
                    .createdTime(now)
                    .updatedTime(now)
                    .build();
            tenderMemberMapper.insert(tenderMember);
            return;
        }

        existMember.setPersonId(req.getSalesmanId());
        existMember.setOrgId(salesman.getOrgId());
        existMember.setPostId(salesman.getPostId());
        existMember.setStatus(CommonConstant.ENABLED_STATUS_STR);
        existMember.setUpdatedTime(now);
        tenderMemberMapper.updateById(existMember);
    }
}
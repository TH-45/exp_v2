package jh.exp.bid.contract.service.service.bus.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jh.exp.auth.clinet.api.bus.PersonService;
import jh.exp.auth.core.entity.res.PersonDetailRes;
import jh.exp.bid.contract.core.constant.BidContractConstant;
import jh.exp.bid.contract.core.entity.Contract;
import jh.exp.bid.contract.core.entity.dto.ContractListDTO;
import jh.exp.bid.contract.core.entity.ContractOperationLog;
import jh.exp.bid.contract.core.entity.req.*;
import jh.exp.bid.contract.core.entity.res.ContractDetailRes;
import jh.exp.bid.contract.core.entity.res.ContractListRes;
import jh.exp.bid.contract.core.mapper.ContractMapper;
import jh.exp.bid.contract.core.mapper.ContractOperationLogMapper;
import jh.exp.bid.contract.service.service.bus.ContractService;
import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.auth.CurrentUserHolder;
import jh.exp.common.core.auth.dto.CurrentUser;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.corp.client.api.CompanyClientService;
import jh.exp.corp.core.entity.req.QueryCompanyReq;
import jh.exp.corp.core.entity.res.CompanyDetailRes;
import jh.exp.corp.core.entity.res.CompanyListRes;
import jh.exp.process.client.api.ProcessApprovalClient;
import jh.exp.process.core.constant.ProcessConstant;
import jh.exp.process.core.entity.req.StartProcessReq;
import jh.exp.project.client.api.ProjectClientService;
import jh.exp.project.core.entity.Project;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 合同服务实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {

    private static final int NAME_QUERY_PAGE_SIZE = 100;
    private static final int NAME_QUERY_MAX_PAGE = 5;

    private final ContractMapper contractMapper;
    private final ContractOperationLogMapper contractOperationLogMapper;
    private final PersonService personService;
//    private final ProcessFlowSupportService processFlowSupportService;
    private final CompanyClientService companyClientService;
    private final ProjectClientService projectClientService;
    private final ProcessApprovalClient processApprovalClient;

    @Override
    public SimplePageRes<ContractListRes> queryContractList(SimplePageReq<QueryContractReq> req) {
        req.pageDefault();
        QueryContractReq queryParam = req.getQueryParam() != null ? req.getQueryParam() : new QueryContractReq();
        String projectName = trimToNull(queryParam.getProjectName());
        String partnerName = trimToNull(queryParam.getPartnerName());
        String supplierName = trimToNull(queryParam.getSupplierName());
        if (projectName != null) {
            List<Long> projectIds = resolveProjectIdsByName(projectName);
            if (CollectionUtils.isEmpty(projectIds)) {
                return emptyPage(req);
            }
            queryParam.setProjectIds(projectIds);
        }
        // 合作方查询：partnerType=1 查 purchaser_id，2 查 supplier_id，空 查两者
        if (partnerName != null) {
            List<Long> companyIds = resolveCompanyIdsByName(partnerName);
            if (CollectionUtils.isEmpty(companyIds)) {
                return emptyPage(req);
            }
            String partnerType = trimToNull(queryParam.getPartnerType());
            if ("1".equals(partnerType)) {
                queryParam.setPurchaserIds(companyIds);
            } else if ("2".equals(partnerType)) {
                queryParam.setSupplierIds(companyIds);
            } else {
                queryParam.setPartnerIds(companyIds);
            }
        } else if (supplierName != null) {
            List<Long> supplierIds = resolveSupplierIdsByName(supplierName);
            if (CollectionUtils.isEmpty(supplierIds)) {
                return emptyPage(req);
            }
            queryParam.setSupplierIds(supplierIds);
        }
        Page<ContractListDTO> page = new Page<>(req.getPageNum(), req.getPageSize());
        IPage<ContractListDTO> result = contractMapper.selectContractList(page, queryParam);
        List<ContractListRes> list = result.getRecords().stream().map(dto -> {
            ContractListRes res = new ContractListRes();
            BeanUtils.copyProperties(dto, res);
            return res;
        }).toList();
        fillContractListNames(list);
        SimplePageRes<ContractListRes> pageRes = new SimplePageRes<>();
        pageRes.setList(list);
        pageRes.setTotal(result.getTotal());
        pageRes.setPage(result.getCurrent());
        pageRes.setSize(result.getSize());
        return pageRes;
    }

    private SimplePageRes<ContractListRes> emptyPage(SimplePageReq<?> req) {
        SimplePageRes<ContractListRes> pr = new SimplePageRes<>();
        pr.setList(Collections.emptyList());
        pr.setTotal(0L);
        pr.setPage((long) req.getPageNum());
        pr.setSize((long) req.getPageSize());
        return pr;
    }

    private String trimToNull(String s) {
        if (s == null || s.isBlank()) return null;
        return s.trim();
    }

    private List<Long> resolveProjectIdsByName(String projectName) {
        if (projectName == null) return null;
        Set<Long> ids = new LinkedHashSet<>();
        String keyword = projectName.toLowerCase(Locale.ROOT);
        for (int p = 1; p <= NAME_QUERY_MAX_PAGE; p++) {
            SimplePageReq<Object> pr = new SimplePageReq<>();
            pr.setPageNum(p);
            pr.setPageSize(NAME_QUERY_PAGE_SIZE);
            pr.setQueryParam(null);
            ApiResponse<SimplePageRes<Project>> resp = projectClientService.list(pr);
            if (resp == null || !resp.isSuccess() || resp.getData() == null || CollectionUtils.isEmpty(resp.getData().getList())) break;
            resp.getData().getList().stream()
                    .filter(Objects::nonNull)
                    .filter(proj -> trimToNull(proj.getProjectName()) != null && proj.getProjectName().toLowerCase(Locale.ROOT).contains(keyword))
                    .map(Project::getProjectId)
                    .filter(Objects::nonNull)
                    .forEach(ids::add);
            if (resp.getData().getList().size() < NAME_QUERY_PAGE_SIZE) break;
        }
        return new ArrayList<>(ids);
    }

    private List<Long> resolveSupplierIdsByName(String supplierName) {
        return resolveCompanyIdsByName(supplierName);
    }

    /** 按企业名称模糊解析企业ID列表（供合作方、供应商等查询使用） */
    private List<Long> resolveCompanyIdsByName(String companyName) {
        if (companyName == null) return null;
        QueryCompanyReq q = new QueryCompanyReq();
        q.setCompanyName(companyName);
        Set<Long> ids = new LinkedHashSet<>();
        for (int p = 1; p <= NAME_QUERY_MAX_PAGE; p++) {
            SimplePageReq<QueryCompanyReq> pr = new SimplePageReq<>();
            pr.setPageNum(p);
            pr.setPageSize(NAME_QUERY_PAGE_SIZE);
            pr.setQueryParam(q);
            ApiResponse<SimplePageRes<CompanyListRes>> resp = companyClientService.list(pr);
            if (resp == null || !resp.isSuccess() || resp.getData() == null || CollectionUtils.isEmpty(resp.getData().getList())) break;
            resp.getData().getList().stream()
                    .map(CompanyListRes::getCompanyId)
                    .filter(Objects::nonNull)
                    .forEach(ids::add);
            if (resp.getData().getList().size() < NAME_QUERY_PAGE_SIZE) break;
        }
        return new ArrayList<>(ids);
    }

    private void fillContractListNames(List<ContractListRes> list) {
        if (CollectionUtils.isEmpty(list)) return;
        Set<Long> projectIds = list.stream().map(ContractListRes::getProjectId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> purchaserIds = list.stream().map(ContractListRes::getPurchaserId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> supplierIds = list.stream().map(ContractListRes::getSupplierId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> projectNameMap = new HashMap<>();
        Map<Long, String> companyNameMap = new HashMap<>();
        if (!projectIds.isEmpty()) {
            ApiResponse<Map<Long, Project>> pr = projectClientService.batchGetProjectByIds(new ArrayList<>(projectIds));
            if (pr != null && pr.isSuccess() && pr.getData() != null) {
                pr.getData().forEach((id, proj) -> projectNameMap.put(id, proj.getProjectName()));
            }
        }
        Set<Long> allCompanyIds = new HashSet<>();
        allCompanyIds.addAll(purchaserIds);
        allCompanyIds.addAll(supplierIds);
        if (!allCompanyIds.isEmpty()) {
            ApiResponse<Map<Long, CompanyDetailRes>> cr = companyClientService.batchDetail(new ArrayList<>(allCompanyIds));
            if (cr != null && cr.isSuccess() && cr.getData() != null) {
                cr.getData().forEach((id, det) -> companyNameMap.put(id, det.getCompanyName()));
            }
        }
        for (ContractListRes r : list) {
            if (r.getProjectId() != null) r.setProjectName(projectNameMap.get(r.getProjectId()));
            if (r.getPurchaserId() != null) r.setPurchaserName(companyNameMap.get(r.getPurchaserId()));
            if (r.getSupplierId() != null) r.setSupplierName(companyNameMap.get(r.getSupplierId()));
        }
    }

    @Override
    public ContractDetailRes getContractById(Long contractId) {
        Contract c = contractMapper.selectById(contractId);
        if (c == null) throw new RuntimeException("合同不存在");
        ContractDetailRes res = new ContractDetailRes();
        BeanUtils.copyProperties(c, res);
        if (c.getProjectId() != null) {
            ApiResponse<jh.exp.project.core.entity.Project> pr = projectClientService.detail(c.getProjectId());
            if (pr != null && pr.isSuccess() && pr.getData() != null) res.setProjectName(pr.getData().getProjectName());
        }
        if (c.getPurchaserId() != null) {
            ApiResponse<CompanyDetailRes> cr = companyClientService.detail(c.getPurchaserId());
            if (cr != null && cr.isSuccess() && cr.getData() != null) res.setPurchaserName(cr.getData().getCompanyName());
        }
        if (c.getSupplierId() != null) {
            ApiResponse<CompanyDetailRes> cr = companyClientService.detail(c.getSupplierId());
            if (cr != null && cr.isSuccess() && cr.getData() != null) res.setSupplierName(cr.getData().getCompanyName());
        }
        // 提单人信息（创建人，当前登录人，不可修改）
        if (c.getCreatedBy() != null) {
            try {
                PersonDetailRes creator = personService.getPersonById(c.getCreatedBy());
                if (creator != null) {
                    res.setCreatorName(creator.getPersonName());
                    res.setCreatorPostName(creator.getPostName());
                    res.setCreatorMobile(creator.getMobile());
                }
            } catch (Exception e) {
                log.warn("获取提单人信息失败 createdBy={}", c.getCreatedBy(), e);
            }
        }
        // 业务员信息
        if (c.getSalesmanPersonId() != null) {
            try {
                PersonDetailRes salesman = personService.getPersonById(c.getSalesmanPersonId());
                if (salesman != null) {
                    res.setSalesmanPersonId(salesman.getPersonId());
                    res.setSalesmanName(salesman.getPersonName());
                    res.setSalesmanPostName(salesman.getPostName());
                    res.setSalesmanMobile(salesman.getMobile());
                }
            } catch (Exception e) {
                log.warn("获取业务员信息失败 salesmanPersonId={}", c.getSalesmanPersonId(), e);
            }
        }
        return res;
    }

    @Override
    @Transactional
    public ContractDetailRes createContract(CreateContractReq req) {
        if (contractMapper.countByContractCode(req.getContractCode(), null) > 0) {
            throw new RuntimeException("合同编号已存在");
        }
        CurrentUser user = CurrentUserHolder.get();
        PersonDetailRes person = personService.getPersonById(user.getUserId());
        if (person == null) {
            throw new RuntimeException("无法获取当前用户信息");
        }

        Contract c = new Contract();
        c.setContractCode(req.getContractCode());
        c.setContractName(req.getContractName());
        c.setContractType(req.getContractType());
        c.setContractCategory(req.getContractCategory());
        c.setTenderId(req.getTenderId());
        c.setBidId(req.getBidId());
        c.setProjectId(req.getProjectId());
        c.setPurchaserId(req.getPurchaserId());
        c.setSupplierId(req.getSupplierId());
        c.setSignDate(req.getSignDate());
        c.setEffectiveDate(req.getEffectiveDate());
        c.setEndDate(req.getEndDate());
        c.setAmountTotal(req.getAmountTotal());
        c.setAmountWithoutTax(req.getAmountWithoutTax());
        c.setTaxRateDefault(req.getTaxRateDefault());
        c.setCurrency(req.getCurrency());
        c.setPayTerms(req.getPayTerms());
        c.setSettleMode(req.getSettleMode());
        c.setStatus(BidContractConstant.CONTRACT_STATUS_DRAFT);
        c.setArchiveFlag(0);
        c.setCreatedBy(user.getUserId());
        c.setCreatedDeptId(person.getOrgId());
        c.setCreatedPostId(person.getPostId());
        c.setSalesmanPersonId(req.getSalesmanPersonId());
        c.setCreatedTime(LocalDateTime.now());
        c.setUpdatedTime(LocalDateTime.now());
        c.setRemark(req.getRemark());

        contractMapper.insert(c);

        log.info( "创建合同成功 contractId={}", c.getContractId());
        return getContractById(c.getContractId());
    }

    @Override
    @Transactional
    public ContractDetailRes updateContract(UpdateContractReq req) {
        Contract c = contractMapper.selectById(req.getContractId());
        if (c == null) {
            throw new RuntimeException("合同不存在");
        }
        if (!BidContractConstant.CONTRACT_STATUS_DRAFT.equals(c.getStatus())) {
            throw new RuntimeException("仅起草中的合同可编辑");
        }
        if (req.getContractCode() != null && contractMapper.countByContractCode(req.getContractCode(), req.getContractId()) > 0) {
            throw new RuntimeException("合同编号已存在");
        }

        UpdateWrapper<Contract> uw = new UpdateWrapper<>();
        if (req.getContractCode() != null) uw.set("contract_code", req.getContractCode());
        if (req.getContractName() != null) uw.set("contract_name", req.getContractName());
        if (req.getContractType() != null) uw.set("contract_type", req.getContractType());
        if (req.getContractCategory() != null) uw.set("contract_category", req.getContractCategory());
        if (req.getTenderId() != null) uw.set("tender_id", req.getTenderId());
        if (req.getBidId() != null) uw.set("bid_id", req.getBidId());
        if (req.getProjectId() != null) uw.set("project_id", req.getProjectId());
        if (req.getPurchaserId() != null) uw.set("purchaser_id", req.getPurchaserId());
        if (req.getSupplierId() != null) uw.set("supplier_id", req.getSupplierId());
        if (req.getSignDate() != null) uw.set("sign_date", req.getSignDate());
        if (req.getEffectiveDate() != null) uw.set("effective_date", req.getEffectiveDate());
        if (req.getEndDate() != null) uw.set("end_date", req.getEndDate());
        if (req.getAmountTotal() != null) uw.set("amount_total", req.getAmountTotal());
        if (req.getAmountWithoutTax() != null) uw.set("amount_without_tax", req.getAmountWithoutTax());
        if (req.getTaxRateDefault() != null) uw.set("tax_rate_default", req.getTaxRateDefault());
        if (req.getCurrency() != null) uw.set("currency", req.getCurrency());
        if (req.getPayTerms() != null) uw.set("pay_terms", req.getPayTerms());
        if (req.getSettleMode() != null) uw.set("settle_mode", req.getSettleMode());
        if (req.getRemark() != null) uw.set("remark", req.getRemark());
        if (req.getSalesmanPersonId() != null) uw.set("salesman_person_id", req.getSalesmanPersonId());
        uw.set("updated_time", LocalDateTime.now());
        uw.eq("contract_id", req.getContractId());
        contractMapper.update(null, uw);
        return getContractById(req.getContractId());
    }

    @Override
    @Transactional
    public void deleteContract(Long contractId) {
        Contract c = contractMapper.selectById(contractId);
        if (c == null) {
            throw new RuntimeException("合同不存在");
        }
        if (!BidContractConstant.CONTRACT_STATUS_DRAFT.equals(c.getStatus())) {
            throw new RuntimeException("仅起草中的合同可删除");
        }
        contractMapper.deleteById(contractId);
    }

    @Override
    @Transactional
    public Long createContractBusiness(CreateContractReq req) {
        if(BidContractConstant.BID_CONTRACT_OP_SAVE.equals(req.getAction())){
            createContract(req);
            return 0L;
        }else if (BidContractConstant.BID_CONTRACT_OP_SUBMIT.equals(req.getAction())){
            Contract contract = contractMapper.selectOne(new LambdaQueryWrapper<Contract>()
                    .eq(Contract::getContractCode, req.getContractCode())
                    .eq(Contract::getStatus, BidContractConstant.CONTRACT_STATUS_DRAFT)
                    .last("LIMIT 1"));
            if (contract == null) {
                throw new RuntimeException("合同不存在");
            }
            if (!BidContractConstant.CONTRACT_STATUS_DRAFT.equals(contract.getStatus())) {
                throw new RuntimeException("仅起草中的合同可提交审批");
            }
            // 调用流程引擎发起审批（通过 exp-process-client）
            StartProcessReq startProcessReq = new StartProcessReq();
            startProcessReq.setBusId(contract.getContractId());
            startProcessReq.setBusType(ProcessConstant.PROCESS_TYPE_CONTRACT);
            startProcessReq.setTitle(req.getContractName()+req.getContractCode());
            startProcessReq.setProcCode(ProcessConstant.PROCESS_CONTRACT_FUND_OUT);
            ApiResponse<Long> res = processApprovalClient.createProcess(startProcessReq);
            if (!res.isSuccess()) {
                throw new RuntimeException("创建流程实例失败");
            }
            // 更新合同状态
            contractMapper.update(null, new UpdateWrapper<Contract>()
                    .eq("contract_id", contract.getContractId())
                    .set("status", BidContractConstant.CONTRACT_STATUS_UNDER_REVIEW)
                    .set("updated_time", LocalDateTime.now()));
            return res.getData();

        }else {
            log.error("未知操作类型  action={}",  req.getAction());
            return null;

        }

    }

    @Override
    @Transactional
    public void updateStatusAfterProcessStart(Long contractId) {
        Contract c = contractMapper.selectById(contractId);
        if (c == null) return;
        if (!BidContractConstant.CONTRACT_STATUS_DRAFT.equals(c.getStatus())) {
            return; // 仅起草中可更新
        }
        contractMapper.update(null, new UpdateWrapper<Contract>()
                .eq("contract_id", contractId)
                .set("status", BidContractConstant.CONTRACT_STATUS_UNDER_REVIEW)
                .set("updated_time", LocalDateTime.now()));
    }

    @Override
    @Transactional
    public void updateStatusByProcess(Long contractId, String status) {
        Contract c = contractMapper.selectById(contractId);
        if (c == null) {
            return;
        }
        contractMapper.update(null, new UpdateWrapper<Contract>()
                .eq("contract_id", contractId)
                .set("status", status)
                .set("updated_time", LocalDateTime.now()));
    }

    @Override
    @Transactional
    public void updateStatusByProcessResult(Long contractId, String instanceStatus) {
        String contractStatus;
        if ("COMPLETED".equalsIgnoreCase(instanceStatus)) {
            contractStatus = BidContractConstant.CONTRACT_STATUS_PENDING_SIGN; // 同意→拟签
        } else if ("REJECTED".equalsIgnoreCase(instanceStatus)) {
            contractStatus = BidContractConstant.CONTRACT_STATUS_DRAFT; // 不同意→返回起草
        } else {
            return; // 其他状态不更新
        }
        updateStatusByProcess(contractId, contractStatus);
    }

    @Override
    @Transactional
    public void signContract(SignContractReq req) {
        Contract c = contractMapper.selectById(req.getContractId());
        if (c == null) {
            throw new RuntimeException("合同不存在");
        }
        if (!BidContractConstant.CONTRACT_STATUS_PENDING_SIGN.equals(c.getStatus())) {
            throw new RuntimeException("仅拟签状态的合同可进行签订/不签订操作");
        }
        CurrentUser user = CurrentUserHolder.get();
        PersonDetailRes person = personService.getPersonById(user.getUserId());
        if (person == null) {
            throw new RuntimeException("无法获取当前用户信息");
        }

        String action = req.getAction();
        if (BidContractConstant.SIGN_ACTION_SIGN.equalsIgnoreCase(action)) {
            // 签订 → 正常合同归档
            LocalDateTime now = LocalDateTime.now();
            contractMapper.update(null, new UpdateWrapper<Contract>()
                    .eq("contract_id", c.getContractId())
                    .set("status", BidContractConstant.CONTRACT_STATUS_ARCHIVED)
                    .set("archive_flag", 1)
                    .set("archive_time", now)
                    .set("sign_user_id", user.getUserId())
                    .set("sign_time", now)
                    .set("updated_time", now));
            insertOperationLog(c.getContractId(), BidContractConstant.OP_TYPE_SIGN,
                    "签订，意见：" + (req.getOpinion() != null ? req.getOpinion() : ""),
                    user.getUserId(), person.getOrgId());
        } else if (BidContractConstant.SIGN_ACTION_UNSIGN.equalsIgnoreCase(action)) {
            Boolean needChange = req.getNeedChange();
            if (needChange == null) {
                throw new RuntimeException("不签订时必须选择是否变更");
            }
            LocalDateTime now = LocalDateTime.now();
            if (Boolean.TRUE.equals(needChange)) {
                // 不签订+变更 → 返回合同起草
                contractMapper.update(null, new UpdateWrapper<Contract>()
                        .eq("contract_id", c.getContractId())
                        .set("status", BidContractConstant.CONTRACT_STATUS_DRAFT)
                        .set("updated_time", now));
                insertOperationLog(c.getContractId(), BidContractConstant.OP_TYPE_UNSIGN_CHANGE,
                        "不签订，选择变更，意见：" + (req.getOpinion() != null ? req.getOpinion() : ""),
                        user.getUserId(), person.getOrgId());
            } else {
                // 不签订+不变更 → 异常合同归档
                contractMapper.update(null, new UpdateWrapper<Contract>()
                        .eq("contract_id", c.getContractId())
                        .set("status", BidContractConstant.CONTRACT_STATUS_ARCHIVED_ABNORMAL)
                        .set("archive_flag", 1)
                        .set("archive_time", now)
                        .set("updated_time", now));
                insertOperationLog(c.getContractId(), BidContractConstant.OP_TYPE_UNSIGN_ARCHIVE_ABNORMAL,
                        "不签订，异常归档，意见：" + (req.getOpinion() != null ? req.getOpinion() : ""),
                        user.getUserId(), person.getOrgId());
            }
        } else {
            throw new RuntimeException("操作类型无效，应为 SIGN 或 UNSIGN");
        }
    }

    /** 记录合同操作日志 */
    private void insertOperationLog(Long contractId, String operationType, String operationContent,
                                   Long operatorUserId, Long operatorDeptId) {
        ContractOperationLog log = new ContractOperationLog();
        log.setContractId(contractId);
        log.setOperationType(operationType);
        log.setOperationContent(operationContent);
        log.setOperatorUserId(operatorUserId);
        log.setOperatorDeptId(operatorDeptId);
        log.setOperationTime(LocalDateTime.now());
        contractOperationLogMapper.insert(log);
    }

}

package jh.exp.bid.contract.service.service.bus.support;

import jh.exp.bid.contract.core.constant.BidEvaluationFlowConstant;
import jh.exp.bid.contract.core.entity.Bid;
import jh.exp.bid.contract.core.entity.BidEvaluationCommittee;
import jh.exp.bid.contract.core.entity.Tender;
import jh.exp.bid.contract.core.mapper.BidMapper;
import jh.exp.bid.contract.core.mapper.EvaluationCommitteeMapper;
import jh.exp.bid.contract.core.mapper.TenderMapper;
import jh.exp.common.core.api.ApiResponse;
import jh.exp.corp.client.api.CompanyClientService;
import jh.exp.corp.core.entity.res.CompanyDetailRes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 评标/定标流程资格校验服务
 */
@Service
@RequiredArgsConstructor
public class EvaluationFlowEligibilityService {

    private final TenderMapper tenderMapper;
    private final BidMapper bidMapper;
    private final EvaluationCommitteeMapper committeeMapper;
    private final CompanyClientService companyClientService;

    /**
     * 根据招标项目校验是否允许进入评标/定标流程。
     */
    public void ensureTenderEligible(Long tenderId) {
        Tender tender = tenderMapper.selectById(tenderId);
        if (tender == null) {
            throw new RuntimeException("招标信息不存在");
        }
        if (tender.getCompanyId() == null) {
            throw new RuntimeException("招标信息缺少招标方，无法进入评标/定标流程");
        }
        if (!isCompanyEligible(tender.getCompanyId())) {
            throw new RuntimeException("该项目非本公司招标，不进入评标/定标流程");
        }
    }

    /**
     * 根据评委会校验流程资格。
     */
    public void ensureCommitteeEligible(Long committeeId) {
        BidEvaluationCommittee committee = committeeMapper.selectById(committeeId);
        if (committee == null) {
            throw new RuntimeException("评标委员会不存在");
        }
        ensureTenderEligible(committee.getTenderId());
    }

    /**
     * 根据投标校验流程资格。
     */
    public void ensureBidEligible(Long bidId) {
        Bid bid = bidMapper.selectById(bidId);
        if (bid == null) {
            throw new RuntimeException("投标信息不存在");
        }
        ensureTenderEligible(bid.getTenderId());
    }

    /**
     * 公司维度校验：
     * companyName trim 后精确匹配，或 companyType 忽略大小写匹配 SELF。
     */
    public boolean isCompanyEligible(Long companyId) {
        ApiResponse<CompanyDetailRes> response = companyClientService.detail(companyId);
        if (response == null || !response.isSuccess() || response.getData() == null) {
            throw new RuntimeException("查询公司信息失败，无法校验评标/定标流程资格");
        }
        CompanyDetailRes company = response.getData();
        String companyName = company.getCompanyName() == null ? null : company.getCompanyName().trim();
        String companyType = company.getCompanyType() == null ? null : company.getCompanyType().trim();
        return BidEvaluationFlowConstant.FLOW_COMPANY_NAME.equals(companyName)
                || BidEvaluationFlowConstant.FLOW_COMPANY_TYPE_SELF.equalsIgnoreCase(companyType);
    }
}

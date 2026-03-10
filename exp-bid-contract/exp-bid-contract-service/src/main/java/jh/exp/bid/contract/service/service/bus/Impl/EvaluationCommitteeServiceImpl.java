package jh.exp.bid.contract.service.service.bus.Impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jh.exp.auth.clinet.api.bus.PersonService;
import jh.exp.auth.core.entity.res.PersonDetailRes;
import jh.exp.bid.contract.core.entity.req.CreateEvaluationCommitteeReq;
import jh.exp.bid.contract.core.entity.req.QueryEvaluationCommitteeReq;
import jh.exp.bid.contract.core.entity.res.EvaluationCommitteeListRes;
import jh.exp.bid.contract.core.mapper.EvaluationCommitteeMapper;
import jh.exp.bid.contract.service.service.bus.EvaluationCommitteeService;
import jh.exp.bid.contract.service.service.bus.support.EvaluationFlowEligibilityService;
import jh.exp.common.core.auth.CurrentUserHolder;
import jh.exp.common.core.auth.dto.CurrentUser;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 评标委员会服务实现类
 */
@Service
@RequiredArgsConstructor
public class EvaluationCommitteeServiceImpl implements EvaluationCommitteeService {

    private final EvaluationCommitteeMapper committeeMapper;
    private final PersonService personService;
    private final EvaluationFlowEligibilityService eligibilityService;

    @Override
    public SimplePageRes<EvaluationCommitteeListRes> queryCommitteeList(SimplePageReq<QueryEvaluationCommitteeReq> req) {
        Page<EvaluationCommitteeListRes> page = new Page<>(req.getPageNum(), req.getPageSize());

        QueryEvaluationCommitteeReq queryParam = req.getQueryParam();
        if (queryParam == null) {
            queryParam = new QueryEvaluationCommitteeReq();
        }

        IPage<EvaluationCommitteeListRes> result = committeeMapper.selectCommitteeList(page, queryParam);
        fillCommitteeNames(result.getRecords());

        SimplePageRes<EvaluationCommitteeListRes> pageRes = new SimplePageRes<>();
        pageRes.setList(result.getRecords());
        pageRes.setTotal(result.getTotal());
        pageRes.setPage(result.getCurrent());
        pageRes.setSize(result.getSize());
        return pageRes;
    }

    @Override
    public EvaluationCommitteeListRes getCommitteeById(Long committeeId) {
        EvaluationCommitteeListRes committee = committeeMapper.selectCommitteeDetailById(committeeId);
        if (committee == null) {
            throw new RuntimeException("评标委员会不存在");
        }
        fillCommitteeNames(Collections.singletonList(committee));
        return committee;
    }

    @Override
    @Transactional
    public EvaluationCommitteeListRes createCommittee(CreateEvaluationCommitteeReq req) {
        eligibilityService.ensureTenderEligible(req.getTenderId());

        // 检查招标项目是否已有评标委员会
        if (checkTenderHasCommittee(req.getTenderId(), null)) {
            throw new RuntimeException("该招标项目已存在评标委员会");
        }

        CurrentUser currentUser = CurrentUserHolder.get();
        Long creatorId = Long.valueOf(currentUser.getUserId());
        Long evaluatorUserId = req.getEvaluationDirectorId() == null ? creatorId : req.getEvaluationDirectorId();
        Integer roundNo = resolveRoundNo(req.getCommitteeCode(), req.getTenderId());
        String normalizedCode = buildCommitteeCode(roundNo);
        if (checkCommitteeCodeExists(normalizedCode, null)) {
            throw new RuntimeException("委员会编号已存在");
        }

        LocalDateTime evalTime = req.getEvaluationStartTime() != null ? req.getEvaluationStartTime() : LocalDateTime.now();
        int inserted = committeeMapper.insertCommittee(req, evaluatorUserId, roundNo, "组建中", evalTime);
        if (inserted <= 0) {
            throw new RuntimeException("创建评标委员会失败");
        }

        EvaluationCommitteeListRes created = committeeMapper.selectCommitteeByCode(req.getTenderId(), normalizedCode);
        if (created == null) {
            throw new RuntimeException("创建成功但回查失败");
        }
        return created;
    }

    @Override
    @Transactional
    public EvaluationCommitteeListRes updateCommittee(CreateEvaluationCommitteeReq req, Long committeeId) {
        EvaluationCommitteeListRes existingCommittee = getCommitteeById(committeeId);
        eligibilityService.ensureTenderEligible(existingCommittee.getTenderId());

        Integer roundNo = resolveRoundNo(req.getCommitteeCode(), existingCommittee.getTenderId());
        String normalizedCode = buildCommitteeCode(roundNo);
        if (checkCommitteeCodeExists(normalizedCode, committeeId)) {
            throw new RuntimeException("委员会编号已存在");
        }

        LocalDateTime evalTime = req.getEvaluationStartTime() != null ? req.getEvaluationStartTime() : LocalDateTime.now();
        int updated = committeeMapper.updateCommittee(
            committeeId,
            req,
            roundNo,
            existingCommittee.getStatus() == null ? "组建中" : existingCommittee.getStatus(),
            evalTime
        );
        if (updated <= 0) {
            throw new RuntimeException("更新评标委员会失败");
        }
        return getCommitteeById(committeeId);
    }

    @Override
    @Transactional
    public void deleteCommittee(Long committeeId) {
        EvaluationCommitteeListRes committee = getCommitteeById(committeeId);
        eligibilityService.ensureTenderEligible(committee.getTenderId());

        // 检查是否可以删除（有相关评标数据时不允许删除）
        // TODO: 添加删除前检查逻辑

        committeeMapper.deleteCommittee(committeeId);
    }

    @Override
    @Transactional
    public void batchDeleteCommittees(List<Long> committeeIds) {
        if (committeeIds == null || committeeIds.isEmpty()) {
            return;
        }

        // 检查每个委员会是否可以删除
        for (Long committeeId : committeeIds) {
            EvaluationCommitteeListRes committee = getCommitteeById(committeeId);
            eligibilityService.ensureTenderEligible(committee.getTenderId());
            // TODO: 添加删除前检查逻辑
        }

        committeeMapper.batchUpdateStatus(committeeIds, "已删除");
    }

    @Override
    @Transactional
    public EvaluationCommitteeListRes updateCommitteeStatus(Long committeeId, String status) {
        EvaluationCommitteeListRes committee = getCommitteeById(committeeId);
        eligibilityService.ensureTenderEligible(committee.getTenderId());

        committeeMapper.batchUpdateStatus(Collections.singletonList(committeeId), status);
        return getCommitteeById(committeeId);
    }

    @Override
    @Transactional
    public void batchUpdateCommitteeStatus(List<Long> committeeIds, String status) {
        if (committeeIds == null || committeeIds.isEmpty()) {
            return;
        }
        for (Long committeeId : committeeIds) {
            eligibilityService.ensureCommitteeEligible(committeeId);
        }
        committeeMapper.batchUpdateStatus(committeeIds, status);
    }

    @Override
    public boolean checkCommitteeCodeExists(String committeeCode, Long excludeCommitteeId) {
        return committeeMapper.countByCommitteeCode(committeeCode, excludeCommitteeId) > 0;
    }

    @Override
    public List<EvaluationCommitteeListRes> getCommitteesByTenderId(Long tenderId) {
        List<EvaluationCommitteeListRes> committees = committeeMapper.selectCommitteesByTenderId(tenderId);
        fillCommitteeNames(committees);
        return committees;
    }

    @Override
    public boolean checkTenderHasCommittee(Long tenderId, Long excludeCommitteeId) {
        List<EvaluationCommitteeListRes> committees = committeeMapper.selectCommitteesByTenderId(tenderId);
        if (excludeCommitteeId != null) {
            committees.removeIf(c -> c.getCommitteeId().equals(excludeCommitteeId));
        }
        return !committees.isEmpty();
    }

    private Integer resolveRoundNo(String committeeCode, Long tenderId) {
        if (committeeCode != null && committeeCode.startsWith("ROUND-")) {
            String suffix = committeeCode.substring("ROUND-".length());
            try {
                return Integer.parseInt(suffix);
            } catch (NumberFormatException ignored) {
                // 非标准编号时按下一轮次兜底
            }
        }
        Integer maxRoundNo = committeeMapper.selectMaxRoundNoByTenderId(tenderId);
        return (maxRoundNo == null ? 0 : maxRoundNo) + 1;
    }

    private String buildCommitteeCode(Integer roundNo) {
        return "ROUND-" + roundNo;
    }

    /**
     * 名称信息由服务层远程补齐，避免在 XML 中跨服务联查。
     */
    private void fillCommitteeNames(List<EvaluationCommitteeListRes> committees) {
        if (committees == null || committees.isEmpty()) {
            return;
        }
        Set<Long> personIds = new HashSet<>();
        for (EvaluationCommitteeListRes committee : committees) {
            if (committee.getEvaluationDirectorId() != null) {
                personIds.add(committee.getEvaluationDirectorId());
            }
            if (committee.getSupervisorId() != null) {
                personIds.add(committee.getSupervisorId());
            }
            if (committee.getCreatedBy() != null) {
                personIds.add(committee.getCreatedBy());
            }
        }
        if (personIds.isEmpty()) {
            return;
        }
        Map<Long, PersonDetailRes> personMap = personService.batchGetPersonByIds(new ArrayList<>(personIds));
        if (personMap == null || personMap.isEmpty()) {
            return;
        }
        for (EvaluationCommitteeListRes committee : committees) {
            PersonDetailRes director = personMap.get(committee.getEvaluationDirectorId());
            if (director != null) {
                committee.setEvaluationDirectorName(director.getPersonName());
            }
            PersonDetailRes supervisor = personMap.get(committee.getSupervisorId());
            if (supervisor != null) {
                committee.setSupervisorName(supervisor.getPersonName());
            }
            PersonDetailRes creator = personMap.get(committee.getCreatedBy());
            if (creator != null) {
                committee.setCreatedByName(creator.getPersonName());
            }
        }
    }
}
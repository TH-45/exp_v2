package jh.exp.bid.contract.service.service.bus.Impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jh.exp.auth.clinet.api.PersonService;

import jh.exp.bid.contract.core.entity.ExpBidEvaluationCommittee;
import jh.exp.bid.contract.core.entity.req.CreateEvaluationCommitteeReq;
import jh.exp.bid.contract.core.entity.req.QueryEvaluationCommitteeReq;
import jh.exp.bid.contract.core.entity.res.EvaluationCommitteeListRes;
import jh.exp.bid.contract.core.mapper.EvaluationCommitteeMapper;
import jh.exp.bid.contract.service.service.bus.EvaluationCommitteeService;
import jh.exp.common.auth.CurrentUserHolder;
import jh.exp.common.auth.dto.CurrentUser;
import jh.exp.common.req.SimplePageReq;
import jh.exp.common.res.SimplePageRes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评标委员会服务实现类
 */
@Service
@RequiredArgsConstructor
public class EvaluationCommitteeServiceImpl implements EvaluationCommitteeService {

    private final EvaluationCommitteeMapper committeeMapper;
    private final PersonService personService;

    @Override
    public SimplePageRes<EvaluationCommitteeListRes> queryCommitteeList(SimplePageReq<QueryEvaluationCommitteeReq> req) {
        Page<EvaluationCommitteeListRes> page = new Page<>(req.getPageNum(), req.getPageSize());

        QueryEvaluationCommitteeReq queryParam = req.getQueryParam();
        if (queryParam == null) {
            queryParam = new QueryEvaluationCommitteeReq();
        }

        IPage<EvaluationCommitteeListRes> result = committeeMapper.selectCommitteeList(page, queryParam);

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
        return committee;
    }

    @Override
    @Transactional
    public EvaluationCommitteeListRes createCommittee(CreateEvaluationCommitteeReq req) {
        // 检查委员会编号是否已存在
        if (checkCommitteeCodeExists(req.getCommitteeCode(), null)) {
            throw new RuntimeException("委员会编号已存在");
        }

        // 检查招标项目是否已有评标委员会
        if (checkTenderHasCommittee(req.getTenderId(), null)) {
            throw new RuntimeException("该招标项目已存在评标委员会");
        }

        CurrentUser currentUser = CurrentUserHolder.get();
        Long personId = Long.valueOf(currentUser.getUserId());

        PersonDetailRes personDetail = personService.getPersonById(personId);
        if (personDetail == null) {
            throw new RuntimeException("无法获取当前用户信息");
        }

        ExpBidEvaluationCommittee committee = new ExpBidEvaluationCommittee();
        committee.setTenderId(req.getTenderId());
        committee.setCommitteeCode(req.getCommitteeCode());
        committee.setCommitteeName(req.getCommitteeName());
        committee.setEvaluationMethod(req.getEvaluationMethod());
        committee.setEvaluationLocation(req.getEvaluationLocation());
        committee.setEvaluationStartTime(req.getEvaluationStartTime());
        committee.setEvaluationEndTime(req.getEvaluationEndTime());
        committee.setStatus("组建中");
        committee.setEvaluationDirectorId(req.getEvaluationDirectorId());
        committee.setSupervisorId(req.getSupervisorId());
        committee.setRemark(req.getRemark());
        committee.setCreatedTime(LocalDateTime.now());
        committee.setUpdatedTime(LocalDateTime.now());
        committee.setCreatedBy(personId);
        committee.setCreatedDeptId(personDetail.getOrgId());
        committee.setCreatedPostId(personDetail.getPostId());

        committeeMapper.insert(committee);
        return getCommitteeById(committee.getCommitteeId());
    }

    @Override
    @Transactional
    public EvaluationCommitteeListRes updateCommittee(CreateEvaluationCommitteeReq req, Long committeeId) {
        ExpBidEvaluationCommittee existingCommittee = committeeMapper.selectById(committeeId);
        if (existingCommittee == null) {
            throw new RuntimeException("评标委员会不存在");
        }

        if (checkCommitteeCodeExists(req.getCommitteeCode(), committeeId)) {
            throw new RuntimeException("委员会编号已存在");
        }

        ExpBidEvaluationCommittee committee = new ExpBidEvaluationCommittee();
        committee.setCommitteeId(committeeId);
        committee.setCommitteeCode(req.getCommitteeCode());
        committee.setCommitteeName(req.getCommitteeName());
        committee.setEvaluationMethod(req.getEvaluationMethod());
        committee.setEvaluationLocation(req.getEvaluationLocation());
        committee.setEvaluationStartTime(req.getEvaluationStartTime());
        committee.setEvaluationEndTime(req.getEvaluationEndTime());
        committee.setEvaluationDirectorId(req.getEvaluationDirectorId());
        committee.setSupervisorId(req.getSupervisorId());
        committee.setRemark(req.getRemark());
        committee.setUpdatedTime(LocalDateTime.now());

        committeeMapper.updateById(committee);
        return getCommitteeById(committeeId);
    }

    @Override
    @Transactional
    public void deleteCommittee(Long committeeId) {
        ExpBidEvaluationCommittee committee = committeeMapper.selectById(committeeId);
        if (committee == null) {
            throw new RuntimeException("评标委员会不存在");
        }

        // 检查是否可以删除（有相关评标数据时不允许删除）
        // TODO: 添加删除前检查逻辑

        committeeMapper.deleteById(committeeId);
    }

    @Override
    @Transactional
    public void batchDeleteCommittees(List<Long> committeeIds) {
        if (committeeIds == null || committeeIds.isEmpty()) {
            return;
        }

        // 检查每个委员会是否可以删除
        for (Long committeeId : committeeIds) {
            ExpBidEvaluationCommittee committee = committeeMapper.selectById(committeeId);
            if (committee == null) {
                throw new RuntimeException("评标委员会不存在: " + committeeId);
            }
            // TODO: 添加删除前检查逻辑
        }

        committeeMapper.batchUpdateStatus(committeeIds, "已删除");
    }

    @Override
    @Transactional
    public EvaluationCommitteeListRes updateCommitteeStatus(Long committeeId, String status) {
        ExpBidEvaluationCommittee committee = committeeMapper.selectById(committeeId);
        if (committee == null) {
            throw new RuntimeException("评标委员会不存在");
        }

        committee.setStatus(status);
        committee.setUpdatedTime(LocalDateTime.now());
        committeeMapper.updateById(committee);

        return getCommitteeById(committeeId);
    }

    @Override
    @Transactional
    public void batchUpdateCommitteeStatus(List<Long> committeeIds, String status) {
        if (committeeIds == null || committeeIds.isEmpty()) {
            return;
        }
        committeeMapper.batchUpdateStatus(committeeIds, status);
    }

    @Override
    public boolean checkCommitteeCodeExists(String committeeCode, Long excludeCommitteeId) {
        return committeeMapper.countByCommitteeCode(committeeCode, excludeCommitteeId) > 0;
    }

    @Override
    public List<EvaluationCommitteeListRes> getCommitteesByTenderId(Long tenderId) {
        return committeeMapper.selectCommitteesByTenderId(tenderId);
    }

    @Override
    public boolean checkTenderHasCommittee(Long tenderId, Long excludeCommitteeId) {
        List<EvaluationCommitteeListRes> committees = committeeMapper.selectCommitteesByTenderId(tenderId);
        if (excludeCommitteeId != null) {
            committees.removeIf(c -> c.getCommitteeId().equals(excludeCommitteeId));
        }
        return !committees.isEmpty();
    }
}
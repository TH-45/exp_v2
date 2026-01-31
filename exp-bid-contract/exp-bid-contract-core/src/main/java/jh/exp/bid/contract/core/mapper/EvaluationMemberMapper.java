package jh.exp.bid.contract.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import jh.exp.bid.contract.core.entity.BidEvaluationMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 评标成员Mapper接口
 */
@Mapper
public interface EvaluationMemberMapper extends BaseMapper<BidEvaluationMember> {

    /**
     * 根据委员会ID查询评标成员列表
     * @param committeeId 委员会ID
     * @return 评标成员列表
     */
    List<BidEvaluationMember> selectMembersByCommitteeId(@Param("committeeId") Long committeeId);

    /**
     * 检查专家是否已在委员会中
     * @param committeeId 委员会ID
     * @param expertUserId 专家用户ID
     * @param excludeMemberId 排除的成员ID（用于更新时检查）
     * @return 存在数量
     */
    int countByCommitteeAndExpert(@Param("committeeId") Long committeeId, @Param("expertUserId") Long expertUserId, @Param("excludeMemberId") Long excludeMemberId);

    /**
     * 批量删除委员会成员
     * @param committeeId 委员会ID
     * @param memberIds 成员ID列表
     * @return 影响行数
     */
    int batchDeleteByCommitteeId(@Param("committeeId") Long committeeId, @Param("memberIds") List<Long> memberIds);

    /**
     * 更新成员到场状态
     * @param memberId 成员ID
     * @param isPresent 是否到场
     * @return 影响行数
     */
    int updatePresentStatus(@Param("memberId") Long memberId, @Param("isPresent") Integer isPresent);
}
package jh.exp.bid.contract.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import jh.exp.bid.contract.core.entity.ExpBidEvaluationResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 评标结果Mapper接口
 */
@Mapper
public interface EvaluationResultMapper extends BaseMapper<ExpBidEvaluationResult> {

    /**
     * 根据委员会ID查询评标结果列表
     * @param committeeId 委员会ID
     * @return 评标结果列表
     */
    List<ExpBidEvaluationResult> selectResultsByCommitteeId(@Param("committeeId") Long committeeId);

    /**
     * 根据投标ID查询评标结果
     * @param bidId 投标ID
     * @return 评标结果
     */
    ExpBidEvaluationResult selectResultByBidId(@Param("bidId") Long bidId);

    /**
     * 根据委员会ID删除评标结果
     * @param committeeId 委员会ID
     * @return 影响行数
     */
    int deleteByCommitteeId(@Param("committeeId") Long committeeId);

    /**
     * 更新评标结果排序
     * @param committeeId 委员会ID
     * @return 影响行数
     */
    int updateRankingByCommitteeId(@Param("committeeId") Long committeeId);

    /**
     * 获取委员会的最高分投标
     * @param committeeId 委员会ID
     * @return 评标结果
     */
    ExpBidEvaluationResult selectHighestScoreByCommitteeId(@Param("committeeId") Long committeeId);
}
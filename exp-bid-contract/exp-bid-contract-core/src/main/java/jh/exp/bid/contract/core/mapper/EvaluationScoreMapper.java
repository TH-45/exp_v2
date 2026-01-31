package jh.exp.bid.contract.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import jh.exp.bid.contract.core.entity.BidEvaluationScore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 评标打分Mapper接口
 */
@Mapper
public interface EvaluationScoreMapper extends BaseMapper<BidEvaluationScore> {

    /**
     * 根据委员会ID和投标ID查询评分记录
     * @param committeeId 委员会ID
     * @param bidId 投标ID
     * @return 评分记录列表
     */
    List<BidEvaluationScore> selectScoresByCommitteeAndBid(@Param("committeeId") Long committeeId, @Param("bidId") Long bidId);

    /**
     * 根据委员会ID查询所有评分记录
     * @param committeeId 委员会ID
     * @return 评分记录列表
     */
    List<BidEvaluationScore> selectScoresByCommitteeId(@Param("committeeId") Long committeeId);

    /**
     * 计算投标的技术评分平均分
     * @param committeeId 委员会ID
     * @param bidId 投标ID
     * @param scoreType 评分类型
     * @return 平均分
     */
    BigDecimal calculateAverageScore(@Param("committeeId") Long committeeId, @Param("bidId") Long bidId, @Param("scoreType") String scoreType);

    /**
     * 批量更新评分状态
     * @param committeeId 委员会ID
     * @param bidId 投标ID
     * @param expertUserId 专家用户ID
     * @param scoreStatus 新状态
     * @return 影响行数
     */
    int batchUpdateScoreStatus(@Param("committeeId") Long committeeId, @Param("bidId") Long bidId, @Param("expertUserId") Long expertUserId, @Param("scoreStatus") String scoreStatus);

    /**
     * 删除委员会的评分记录
     * @param committeeId 委员会ID
     * @return 影响行数
     */
    int deleteByCommitteeId(@Param("committeeId") Long committeeId);
}
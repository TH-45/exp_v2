package jh.exp.bid.contract.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import jh.exp.bid.contract.entity.ExpBidAwardResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 定标结果Mapper接口
 */
@Mapper
public interface AwardResultMapper extends BaseMapper<ExpBidAwardResult> {

    /**
     * 根据招标ID查询定标结果
     * @param tenderId 招标ID
     * @return 定标结果
     */
    ExpBidAwardResult selectAwardResultByTenderId(@Param("tenderId") Long tenderId);

    /**
     * 根据投标ID查询定标结果
     * @param bidId 投标ID
     * @return 定标结果
     */
    ExpBidAwardResult selectAwardResultByBidId(@Param("bidId") Long bidId);

    /**
     * 检查招标项目是否已有定标结果
     * @param tenderId 招标ID
     * @param excludeAwardId 排除的定标ID（用于更新时检查）
     * @return 存在数量
     */
    int countByTenderId(@Param("tenderId") Long tenderId, @Param("excludeAwardId") Long excludeAwardId);

    /**
     * 更新定标状态
     * @param awardId 定标ID
     * @param awardStatus 新状态
     * @return 影响行数
     */
    int updateAwardStatus(@Param("awardId") Long awardId, @Param("awardStatus") String awardStatus);
}
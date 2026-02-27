package jh.exp.bid.contract.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jh.exp.bid.contract.core.entity.res.BidDetailRes;
import jh.exp.bid.contract.core.entity.res.BidListRes;
import jh.exp.bid.contract.core.entity.Bid;
import jh.exp.bid.contract.core.entity.req.QueryBidReq;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 投标Mapper接口
 */
@Mapper
public interface BidMapper extends BaseMapper<Bid> {

    /**
     * 检查投标编号是否存在
     * @param bidCode 投标编号
     * @param excludeBidId 排除的投标ID（用于更新时检查）
     * @return 存在数量
     */
    int countByBidCode(@Param("bidCode") String bidCode, @Param("excludeBidId") Long excludeBidId);

    /**
     * 分页查询投标列表（多表联查）
     * @param page 分页对象
     * @param req 查询条件
     * @return 投标列表（分页结果会自动填充到page对象中）
     */

    IPage<BidListRes> selectBidList(IPage<BidListRes> page, @Param("req") QueryBidReq req);

    /**
     * 根据投标ID查询投标详情信息（多表联查）
     * @param bidId 投标ID
     * @return 投标详情信息
     */
    BidDetailRes selectBidDetailById(@Param("bidId") Long bidId);

    /**
     * 根据招标ID查询投标列表
     * @param tenderId 招标ID
     * @return 投标列表
     */
    List<BidListRes> selectBidsByTenderId(@Param("tenderId") Long tenderId);

    /**
     * 批量更新投标状态
     * @param bidIds 投标ID列表
     * @param bidStatus 新状态
     * @return 影响行数
     */
    int batchUpdateStatus(@Param("bidIds") List<Long> bidIds, @Param("bidStatus") String bidStatus);

    /**
     * 根据招标ID检查是否已有投标记录（用于防止重复投标）
     * @param tenderId 招标ID
     * @param supplierId 投标单位ID
     * @param excludeBidId 排除的投标ID（用于更新时检查）
     * @return 存在数量
     */
    int countByTenderAndSupplier(@Param("tenderId") Long tenderId, @Param("supplierId") Long supplierId, @Param("excludeBidId") Long excludeBidId);
}
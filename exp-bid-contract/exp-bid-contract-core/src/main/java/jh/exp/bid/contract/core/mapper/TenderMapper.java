package jh.exp.bid.contract.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jh.exp.bid.contract.core.entity.res.TenderDetailRes;
import jh.exp.bid.contract.core.entity.dto.TenderLisDTO;
import jh.exp.bid.contract.core.entity.Tender;
import jh.exp.bid.contract.core.entity.req.QueryTenderReq;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 招标Mapper接口
 */
@Mapper
public interface TenderMapper extends BaseMapper<Tender> {

    /**
     * 检查招标编号是否存在
     * @param tenderCode 招标编号
     * @param excludeTenderId 排除的招标ID（用于更新时检查）
     * @return 存在数量
     */
    int countByTenderCode(@Param("tenderCode") String tenderCode, @Param("excludeTenderId") Long excludeTenderId);

    /**
     * 分页查询招标列表（多表联查）
     * @param page 分页对象
     * @param req 查询条件
     * @return 招标列表（分页结果会自动填充到page对象中）
     */
    IPage<TenderLisDTO> selectTenderList(IPage<TenderLisDTO> page, @Param("req") QueryTenderReq req);

    /**
     * 根据招标ID查询招标详情信息（多表联查）
     * @param tenderId 招标ID
     * @return 招标详情信息
     */
    TenderDetailRes selectTenderDetailById(@Param("tenderId") Long tenderId);

    /**
     * 批量更新招标状态
     * @param tenderIds 招标ID列表
     * @param status 新状态
     * @return 影响行数
     */
    int batchUpdateStatus(@Param("tenderIds") List<Long> tenderIds, @Param("status") String status);
}
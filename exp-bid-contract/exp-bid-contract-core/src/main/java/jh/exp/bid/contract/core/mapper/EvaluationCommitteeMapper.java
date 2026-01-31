package jh.exp.bid.contract.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jh.exp.bid.contract.core.entity.BidEvaluationCommittee;
import jh.exp.bid.contract.core.entity.req.QueryEvaluationCommitteeReq;
import jh.exp.bid.contract.core.entity.res.EvaluationCommitteeListRes;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 评标委员会Mapper接口
 */
@Mapper
public interface EvaluationCommitteeMapper extends BaseMapper<BidEvaluationCommittee> {

    /**
     * 检查委员会编号是否存在
     * @param committeeCode 委员会编号
     * @param excludeCommitteeId 排除的委员会ID（用于更新时检查）
     * @return 存在数量
     */
    int countByCommitteeCode(@Param("committeeCode") String committeeCode, @Param("excludeCommitteeId") Long excludeCommitteeId);

    /**
     * 分页查询评标委员会列表
     * @param page 分页对象
     * @param req 查询条件
     * @return 评标委员会列表
     */
    IPage<EvaluationCommitteeListRes> selectCommitteeList(IPage<EvaluationCommitteeListRes> page, @Param("req") QueryEvaluationCommitteeReq req);

    /**
     * 根据委员会ID查询评标委员会详情
     * @param committeeId 委员会ID
     * @return 评标委员会详情
     */
    EvaluationCommitteeListRes selectCommitteeDetailById(@Param("committeeId") Long committeeId);

    /**
     * 根据招标ID查询评标委员会列表
     * @param tenderId 招标ID
     * @return 评标委员会列表
     */
    List<EvaluationCommitteeListRes> selectCommitteesByTenderId(@Param("tenderId") Long tenderId);

    /**
     * 批量更新委员会状态
     * @param committeeIds 委员会ID列表
     * @param status 新状态
     * @return 影响行数
     */
    int batchUpdateStatus(@Param("committeeIds") List<Long> committeeIds, @Param("status") String status);
}
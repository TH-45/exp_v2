package jh.exp.process.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jh.exp.process.core.entity.WfInstance;
import jh.exp.process.core.entity.req.ApprovalTaskQueryReq;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WfInstanceMapper extends BaseMapper<WfInstance> {

    /**
     * 我发起/已关闭实例真分页查询（过滤条件下沉到 SQL）
     *
     * @param instanceStatus 已关闭 tab 时传 CLOSED，我发起 tab 时传 null
     */
    IPage<WfInstance> selectStartedPage(IPage<WfInstance> page, @Param("userId") Long userId,
                                        @Param("instanceStatus") String instanceStatus,
                                        @Param("query") ApprovalTaskQueryReq query);
}

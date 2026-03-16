package jh.exp.process.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jh.exp.process.core.entity.WfTask;
import jh.exp.process.core.entity.dto.TaskContextDTO;
import jh.exp.process.core.entity.dto.TaskHandleContextDTO;
import jh.exp.process.core.entity.req.ApprovalTaskQueryReq;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WfTaskMapper extends BaseMapper<WfTask> {

    /**
     * 根据 taskId 一次查询 task、instance、definition 关联信息（减少 N+1 查询）
     */
    TaskContextDTO selectTaskContextByTaskId(@Param("taskId") Long taskId);

    /**
     * 审批处理：一次查询 task + instance，条件 is_done=0 且 status=APPROVING
     */
    TaskHandleContextDTO selectTaskHandleContext(@Param("taskId") Long taskId);

    /**
     * 待办任务真分页查询（candidate_id 绑定人员ID，过滤条件下沉到 SQL）
     */
    IPage<WfTask> selectTodoPage(IPage<WfTask> page, @Param("personId") Long personId, @Param("query") ApprovalTaskQueryReq query);

    /**
     * 已办任务真分页查询（handler_id 绑定人员ID，过滤条件下沉到 SQL）
     */
    IPage<WfTask> selectDonePage(IPage<WfTask> page, @Param("personId") Long personId, @Param("query") ApprovalTaskQueryReq query);
}

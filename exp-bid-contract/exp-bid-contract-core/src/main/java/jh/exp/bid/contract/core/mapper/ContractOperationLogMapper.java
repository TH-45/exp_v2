package jh.exp.bid.contract.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import jh.exp.bid.contract.core.entity.ContractOperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 合同操作日志 Mapper
 */
@Mapper
public interface ContractOperationLogMapper extends BaseMapper<ContractOperationLog> {
}

package jh.exp.bid.contract.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jh.exp.bid.contract.core.entity.Contract;
import jh.exp.bid.contract.core.entity.dto.ContractListDTO;
import jh.exp.bid.contract.core.entity.req.QueryContractReq;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 合同 Mapper
 */
@Mapper
public interface ContractMapper extends BaseMapper<Contract> {

    /**
     * 分页查询合同列表
     */
    IPage<ContractListDTO> selectContractList(IPage<ContractListDTO> page, @Param("req") QueryContractReq req);

    /**
     * 根据ID查询合同详情（含项目、甲方、乙方名称）
     */
    ContractListDTO selectContractDetailById(@Param("contractId") Long contractId);

    /**
     * 检查合同编号是否存在
     */
    int countByContractCode(@Param("contractCode") String contractCode, @Param("excludeContractId") Long excludeContractId);
}

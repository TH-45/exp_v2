package jh.exp.auth.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jh.exp.auth.entity.Account;
import jh.exp.auth.entity.res.AccountDetailRes;
import jh.exp.auth.entity.res.AccountListRes;
import jh.exp.auth.entity.res.AccountRoleRes;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface AccountMapper extends BaseMapper<Account> {

    /**
     * 检查账号名称是否存在
     * @param accountName 账号名称
     * @param excludeAccountId 排除的账号ID（用于更新时检查）
     * @return 存在数量
     */
    int countByAccountName(@Param("accountName") String accountName, @Param("excludeAccountId") Long excludeAccountId);

    /**
     * 分页查询账号列表（多表联查）
     * @param page 分页对象
     * @param accountName 账号名称筛选
     * @param personName 人员姓名筛选
     * @param mobile 手机号筛选
     * @return 账号列表（分页结果会自动填充到page对象中）
     */
    IPage<AccountListRes> selectAccountList(IPage<AccountListRes> page,
                                           @Param("accountName") String accountName,
                                           @Param("personName") String personName,
                                           @Param("mobile") String mobile);

    /**
     * 根据账号ID查询账号详情信息（多表联查）
     * @param accountId 账号ID
     * @return 账号详情信息
     */
    AccountDetailRes selectAccountDetailById(@Param("accountId") Long accountId);

    /**
     * 根据账号id列表查角色信息（多表联查）
     */
    List<AccountRoleRes> selectRolesByAccountIds(@Param("accountIds") List<Long> accountIds);

}



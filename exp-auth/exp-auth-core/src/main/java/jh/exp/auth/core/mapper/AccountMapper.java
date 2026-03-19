package jh.exp.auth.core.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

import jh.exp.auth.core.entity.Account;

import jh.exp.auth.core.entity.res.AccountDetailRes;
import jh.exp.auth.core.entity.res.AccountListRes;
import jh.exp.auth.core.entity.res.AccountRoleRes;
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
                                            @Param("personCode") String personCode,
                                            @Param("mobile") String mobile);

    /**
     * 根据账号ID查询账号详情信息（多表联查）
     * @param accountId 账号ID
     * @return 账号详情信息
     */
    AccountDetailRes selectAccountDetailById(@Param("accountId") Long accountId);

    /**
     * 根据账号id列表查角色信息（仅 ACCOUNT 主体）
     */
    List<AccountRoleRes> selectRolesByAccountIds(@Param("accountIds") List<Long> accountIds);

    /**
     * 根据账号ID查询该账号拥有的所有角色（含 ACCOUNT/PERSON/POST/ORG 四类主体）
     */
    List<AccountRoleRes> selectRolesForAccount(@Param("accountId") Long accountId);

    /**
     * 根据角色ID找出所有受影响的账号ID（含 ACCOUNT/PERSON/POST/ORG 四类主体换算）
     */
    List<Long> selectAccountIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据主体类型和主体ID列表，换算为账号ID列表
     * @param principalType ACCOUNT/PERSON/POST/ORG
     * @param principalIds 主体ID列表
     */
    List<Long> selectAccountIdsByPrincipals(@Param("principalType") String principalType,
                                           @Param("principalIds") List<Long> principalIds);
}



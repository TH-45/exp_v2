package jh.exp.auth.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import jh.exp.auth.entity.Account;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AccountMapper extends BaseMapper<Account> {

//    Optional<Account> findByAccountName(String accountName);
}



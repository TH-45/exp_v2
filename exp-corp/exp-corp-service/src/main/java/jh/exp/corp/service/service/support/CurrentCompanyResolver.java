package jh.exp.corp.service.service.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jh.exp.common.core.exception.GatewayBizException;
import jh.exp.corp.core.constant.CorpErrorCode;
import jh.exp.corp.core.entity.Company;
import jh.exp.corp.core.mapper.CompanyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 当前公司解析器（单企业模式）：
 * 1. 优先取 company_type=SELF 且 status=ENABLED
 * 2. 若不存在则退化为 company_type=SELF
 */
@Component
@RequiredArgsConstructor
public class CurrentCompanyResolver {

    private final CompanyMapper companyMapper;

    public Long resolveCurrentCompanyId() {
        LambdaQueryWrapper<Company> enabledSelf = new LambdaQueryWrapper<>();
        enabledSelf.eq(Company::getCompanyType, "SELF")
                .eq(Company::getStatus, "ENABLED")
                .orderByAsc(Company::getCompanyId)
                .last("limit 1");
        Company current = companyMapper.selectOne(enabledSelf);
        if (current != null) {
            return current.getCompanyId();
        }

        LambdaQueryWrapper<Company> self = new LambdaQueryWrapper<>();
        self.eq(Company::getCompanyType, "SELF")
                .orderByAsc(Company::getCompanyId)
                .last("limit 1");
        current = companyMapper.selectOne(self);
        if (current != null) {
            return current.getCompanyId();
        }
        throw new GatewayBizException(CorpErrorCode.CURRENT_COMPANY_NOT_FOUND, "未找到当前企业（SELF）配置");
    }
}

package jh.exp.sys.servcie.dic.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jh.exp.auth.clinet.api.AccountService;
import jh.exp.auth.core.constant.AuthConstant;

import jh.exp.auth.core.entity.res.AccountRoleRes;
import jh.exp.common.core.auth.CurrentUserHolder;
import jh.exp.common.core.auth.dto.CurrentUser;
import jh.exp.common.core.constant.CommonConstant;
import jh.exp.sys.core.api.dic.SysDictService;
import jh.exp.sys.core.entity.dic.SysDictItem;
import jh.exp.sys.core.mapper.dic.SysDictMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.baomidou.mybatisplus.extension.toolkit.Db.removeById;

@Service
@RequiredArgsConstructor
public class SysDictServiceImpl implements SysDictService {
    private final SysDictMapper sysDictMapper;
    private final AccountService accountService;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createDictItem(SysDictItem item) {
        String dictCode = item.getDictCode();
        if (dictCode != null && item.getItemCode() != null) {
            QueryWrapper<SysDictItem> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("dict_code", dictCode);
            queryWrapper.eq("item_code", item.getItemCode());
            SysDictItem dictItem = sysDictMapper.selectOne(queryWrapper);
            if (dictItem != null) {
                throw new RuntimeException("字典项编码已存在");
            }
        }
        sysDictMapper.insert(item);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDictItem(SysDictItem item) {
        SysDictItem existing = sysDictMapper.selectById(item.getId());
        if (existing == null) {
            throw new RuntimeException("字典项不存在");
        }
        if (item.getItemCode() != null) {
            String dictCode = item.getDictCode() != null ? item.getDictCode() : existing.getDictCode();
            QueryWrapper<SysDictItem> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("dict_code", dictCode);
            queryWrapper.eq("item_code", item.getItemCode());
            SysDictItem dictItem = sysDictMapper.selectOne(queryWrapper);
            if (dictItem != null && !dictItem.getId().equals(item.getId())) {
                throw new RuntimeException("字典项编码不一致,非法操作");
            }
        }
        //支持部分更新
        sysDictMapper.updateById(item);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDictItem(Long id) {
        CurrentUser currentUser = CurrentUserHolder.get();
        List<AccountRoleRes> accountRoles = accountService.getAccountRoles(List.of(currentUser.getUserId()));
        List<String> roleCodes = accountRoles.stream().map(AccountRoleRes::getRoleCode).toList();
        if (!roleCodes.contains(AuthConstant.ADMIN)) {
            throw new RuntimeException("非管理员角色不允许删除字典项");
        }
        removeById(id);
    }

    @Override
    public List<SysDictItem> listByDictTypeCode(String dictTypeCode) {
        if (dictTypeCode == null) {
            return null;
        }
        return sysDictMapper.listByDictCode(dictTypeCode);
    }

    @Override
    public List<SysDictItem> listEnabledByItemCode(String itemCode) {
        QueryWrapper<SysDictItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("item_code", itemCode);
        queryWrapper.eq("status", CommonConstant.ENABLED_STATUS_STR);
        return sysDictMapper.selectList(queryWrapper);
    }

    @Override
    public SysDictItem getDicItem(String itemCode) {
        QueryWrapper<SysDictItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("item_code", itemCode);
        queryWrapper.eq("status", CommonConstant.ENABLED_STATUS_STR);
        return sysDictMapper.selectOne(queryWrapper);

    }

}
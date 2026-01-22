package jh.exp.sys.servcie.dic.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jh.exp.auth.clinet.api.AccountService;
import jh.exp.auth.core.constant.AuthConstant;

import jh.exp.common.auth.CurrentUserHolder;
import jh.exp.common.auth.dto.CurrentUser;
import jh.exp.common.constant.CommonConstant;
import jh.exp.sys.api.dic.SysDictService;
import jh.exp.sys.entity.dic.SysDictItem;
import jh.exp.sys.entity.dic.SysDictType;
import jh.exp.sys.mapper.dic.SysDictMapper;
import jh.exp.sys.mapper.dic.SysDictTypeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.baomidou.mybatisplus.extension.toolkit.Db.removeById;

@Service
@RequiredArgsConstructor
public class SysDictServiceImpl implements SysDictService {
    private final SysDictMapper sysDictMapper;
    private final SysDictTypeMapper sysDictTypeMapper;
    private final AccountService accountService;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createDictItem(SysDictItem item) {
        String dictTypeCode = item.getDictTypeCode();
        if (dictTypeCode != null) {
            SysDictItem dictItem = getDicItem(item.getItemCode());
            if (dictItem != null) {
                throw new RuntimeException("字典项编码已存在");
            }
        }
        sysDictMapper.insert(item);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDictItem(SysDictItem item) {
        SysDictItem dictItem = getDicItem(item.getItemCode());
        if (dictItem == null) {
            throw new RuntimeException("字典项不存在");
        }
        if(!dictItem.getId().equals(item.getId())){
            throw new RuntimeException("字典项编码不一致,非法操作");
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

    /**
     * 核查字典类型是否一致
     * @param dicTypeCode 目标字典类型编码
     * @param item 被核查的字典项
     */
    private Boolean checkDictType(String dicTypeCode, SysDictItem item) {
        String dictTypeCode = item.getDictTypeCode();
        QueryWrapper<SysDictType> qw = new QueryWrapper<>();
        qw.eq("dict_code", dictTypeCode);
        SysDictType dictType = sysDictTypeMapper.selectOne(qw);
        if (dictType == null) {
            return false;
        }
        return dicTypeCode.equals(dictType.getDictCode());
    }

}
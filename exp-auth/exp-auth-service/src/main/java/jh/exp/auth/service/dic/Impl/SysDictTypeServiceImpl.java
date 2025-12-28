package jh.exp.auth.service.dic.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jh.exp.auth.entity.dic.SysDictType;
import jh.exp.auth.mapper.dic.SysDictTypeMapper;
import jh.exp.auth.service.dic.SysDictTypeService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class SysDictTypeServiceImpl extends ServiceImpl<SysDictTypeMapper, SysDictType> implements SysDictTypeService {
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createDictType(SysDictType dictType) {
        // dictCode 唯一性校验
        boolean exists = lambdaQuery()
                .eq(SysDictType::getDictCode, dictType.getDictCode())
                .exists();
        if (exists) {
            throw new RuntimeException("字典类型编码已存在：" + dictType.getDictCode());
        }
        save(dictType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDictType(SysDictType dictType) {
        updateById(dictType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDictType(Long id) {
        SysDictType type = getById(id);
        if (type == null) {
            return;
        }
        // 系统内置字典不允许删除
        if (Integer.valueOf(1).equals(type.getIsSystem())) {
            throw new RuntimeException("系统内置字典不允许删除");
        }
        removeById(id);
    }

    @Override
    public SysDictType getByDictCode(String dictCode) {
        return lambdaQuery()
                .eq(SysDictType::getDictCode, dictCode)
                .one();
    }

    @Override
    public List<SysDictType> listEnabled() {
        return lambdaQuery()
                .eq(SysDictType::getStatus, "ENABLED")
                .list();
    }
}
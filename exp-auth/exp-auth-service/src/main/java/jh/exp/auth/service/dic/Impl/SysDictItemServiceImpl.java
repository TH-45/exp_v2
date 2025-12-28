package jh.exp.auth.service.dic.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jh.exp.auth.entity.dic.SysDictItem;
import jh.exp.auth.mapper.dic.SysDictItemMapper;
import jh.exp.auth.service.dic.SysDictItemService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SysDictItemServiceImpl
        extends ServiceImpl<SysDictItemMapper, SysDictItem>
        implements SysDictItemService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createDictItem(SysDictItem item) {
        // 同一字典类型下 value 唯一
        boolean exists = lambdaQuery()
                .eq(SysDictItem::getDictCode, item.getDictCode())
                .eq(SysDictItem::getItemValue, item.getItemValue())
                .exists();
        if (exists) {
            throw new RuntimeException("字典项值已存在：" + item.getItemValue());
        }
        save(item);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDictItem(SysDictItem item) {
        updateById(item);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDictItem(Long id) {
        removeById(id);
    }

    @Override
    public List<SysDictItem> listByDictCode(String dictCode) {
        return lambdaQuery()
                .eq(SysDictItem::getDictCode, dictCode)
                .orderByAsc(SysDictItem::getSortNo)
                .list();
    }

    @Override
    public List<SysDictItem> listEnabledByDictCode(String dictCode) {
        return lambdaQuery()
                .eq(SysDictItem::getDictCode, dictCode)
                .eq(SysDictItem::getStatus, "ENABLED")
                .orderByAsc(SysDictItem::getSortNo)
                .list();
    }
}
package jh.exp.sys.api.dic;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.IService;
import jh.exp.sys.entity.dic.SysDictItem;

import java.util.List;

public interface SysDictService {

    /**
     * 新增字典项
     */
    void createDictItem(SysDictItem item);

    /**
     * 修改字典项
     */
    void updateDictItem(SysDictItem item);

    /**
     * 删除字典项
     */
    void deleteDictItem(Long id);

    /**
     * 根据字典类型编码查询字典项
     */
    List<SysDictItem> listByDictTypeCode(String dictTypeCode);

    /**
     * 查询启用的字典项
     */
    List<SysDictItem> listEnabledByItemCode(String itemCode);

    /**
     * 根据字典项编码查询字典项
     * @param dictCode 字典项编码
     * @return
     */
    SysDictItem getDicItem(String dictCode);

}
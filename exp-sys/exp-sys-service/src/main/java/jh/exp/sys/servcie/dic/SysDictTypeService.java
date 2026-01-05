package jh.exp.sys.servcie.dic;


import com.baomidou.mybatisplus.extension.service.IService;
import jh.exp.sys.entity.dic.SysDictType;

import java.util.List;

public interface SysDictTypeService extends IService<SysDictType> {

    /**
     * 新增字典类型
     */
    void createDictType(SysDictType dictType);

    /**
     * 修改字典类型
     */
    void updateDictType(SysDictType dictType);

    /**
     * 删除字典类型（逻辑校验）
     */
    void deleteDictType(Long id);

    /**
     * 根据编码查询
     */
    SysDictType getByDictCode(String dictCode);

    /**
     * 查询启用的字典类型列表
     */
    List<SysDictType> listEnabled();
}
package jh.exp.sys.core.mapper.dic;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import jh.exp.sys.core.entity.dic.SysDictItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 字典项 Mapper
 * 所有字典项的 CRUD 操作统一在此完成
 */
@Mapper
public interface SysDictMapper extends BaseMapper<SysDictItem> {

    List<SysDictItem> listByDictCode(@Param("dictCode") String dictCode);
}
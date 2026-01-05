package jh.exp.sys.mapper.dic;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import jh.exp.sys.entity.dic.SysDictType;
import org.apache.ibatis.annotations.Mapper;

/**
 * 字典类型 Mapper
 * 所有字典类型的 CRUD 操作统一在此完成
 */
@Mapper
public interface SysDictTypeMapper extends BaseMapper<SysDictType> {
}
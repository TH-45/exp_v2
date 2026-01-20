package jh.exp.sys.mapper.dic;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import jh.exp.sys.entity.dic.SysDictItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 字典项 Mapper
 * 所有字典项的 CRUD 操作统一在此完成
 */
@Mapper
public interface SysDictMapper extends BaseMapper<SysDictItem> {

}
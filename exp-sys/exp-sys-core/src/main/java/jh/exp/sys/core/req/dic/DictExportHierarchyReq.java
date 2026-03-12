package jh.exp.sys.core.req.dic;

import jh.exp.sys.core.entity.dic.SysDictItem;
import jh.exp.sys.core.entity.dic.SysDictType;
import lombok.Data;

import java.util.List;

/**
 * 字典导出父子结构请求
 * 用于 Excel 导出时按类型分组、合并行
 */
@Data
public class DictExportHierarchyReq {
    /** 字典类型 */
    private SysDictType dictType;
    /** 该类型下的字典项 */
    private List<SysDictItem> items;
}

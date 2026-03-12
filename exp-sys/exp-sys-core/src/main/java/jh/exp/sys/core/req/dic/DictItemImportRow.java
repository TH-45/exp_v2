package jh.exp.sys.core.req.dic;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 字典项导入行（JSON/Excel 通用）
 * 按 dictCode + itemCode 判断：存在则更新，不存在则新增
 */
@Data
public class DictItemImportRow {
    /** 字典类型编码 */
    @NotBlank(message = "dictCode 不能为空")
    private String dictCode;
    /** 字典项编码 */
    private String itemCode;
    /** 字典项值（业务表存储值） */
    @NotBlank(message = "itemValue 不能为空")
    private String itemValue;
    /** 字典项显示名称 */
    @NotBlank(message = "itemLabel 不能为空")
    private String itemLabel;
    /** 排序号 */
    private Integer sortNo;
    /** 状态：ENABLED/DISABLED */
    private String status;
    /** 备注 */
    private String remark;
}

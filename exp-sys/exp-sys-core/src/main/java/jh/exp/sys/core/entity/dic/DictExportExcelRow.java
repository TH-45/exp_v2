package jh.exp.sys.core.entity.dic;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

/**
 * 字典导出 Excel 行（父子结构，类型列支持合并）
 */
@Data
public class DictExportExcelRow {
    @ExcelProperty(value = "字典类型编码", index = 0)
    @ColumnWidth(18)
    private String dictCode;

    @ExcelProperty(value = "字典类型名称", index = 1)
    @ColumnWidth(18)
    private String dictName;

    @ExcelProperty(value = "类型描述", index = 2)
    @ColumnWidth(24)
    private String typeDescription;

    @ExcelProperty(value = "类型状态", index = 3)
    @ColumnWidth(12)
    private String typeStatus;

    @ExcelProperty(value = "字典项编码", index = 4)
    @ColumnWidth(18)
    private String itemCode;

    @ExcelProperty(value = "字典项值", index = 5)
    @ColumnWidth(14)
    private String itemValue;

    @ExcelProperty(value = "字典项名称", index = 6)
    @ColumnWidth(18)
    private String itemLabel;

    @ExcelProperty(value = "排序", index = 7)
    @ColumnWidth(8)
    private Integer sortNo;

    @ExcelProperty(value = "项状态", index = 8)
    @ColumnWidth(10)
    private String itemStatus;

    @ExcelProperty(value = "备注", index = 9)
    @ColumnWidth(24)
    private String remark;
}

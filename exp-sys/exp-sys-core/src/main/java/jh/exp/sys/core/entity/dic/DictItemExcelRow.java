package jh.exp.sys.core.entity.dic;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

/**
 * 字典项 Excel 导入行模型
 */
@Data
public class DictItemExcelRow {
    @ExcelProperty(value = "字典类型编码", index = 0)
    @ColumnWidth(20)
    private String dictCode;

    @ExcelProperty(value = "字典项编码", index = 1)
    @ColumnWidth(20)
    private String itemCode;

    @ExcelProperty(value = "字典项值", index = 2)
    @ColumnWidth(15)
    private String itemValue;

    @ExcelProperty(value = "字典项名称", index = 3)
    @ColumnWidth(20)
    private String itemLabel;

    @ExcelProperty(value = "排序", index = 4)
    @ColumnWidth(8)
    private Integer sortNo;

    @ExcelProperty(value = "状态", index = 5)
    @ColumnWidth(10)
    private String status;

    @ExcelProperty(value = "备注", index = 6)
    @ColumnWidth(30)
    private String remark;
}

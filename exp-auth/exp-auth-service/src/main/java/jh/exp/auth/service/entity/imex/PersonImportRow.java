package jh.exp.auth.service.entity.imex;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class PersonImportRow {
    @ExcelProperty("人员编码")
    private String personCode;

    @ExcelProperty("姓名")
    private String personName;

    @ExcelProperty("手机号")
    private String mobile;

    @ExcelProperty("邮箱")
    private String email;

    @ExcelProperty("性别")
    private String gender;

    @ExcelProperty("组织编码")
    private String orgCode;

    @ExcelProperty("岗位编码")
    private String postCode;

    @ExcelProperty("状态")
    private String status;

    @ExcelProperty("备注")
    private String remark;
}

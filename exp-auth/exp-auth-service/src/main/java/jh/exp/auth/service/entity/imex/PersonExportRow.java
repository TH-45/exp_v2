package jh.exp.auth.service.entity.imex;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class PersonExportRow {
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

    @ExcelProperty("组织名称")
    private String orgName;

    @ExcelProperty("岗位名称")
    private String postName;

    @ExcelProperty("状态")
    private String status;
}

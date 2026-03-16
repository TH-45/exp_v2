package jh.exp.process.core.entity.req;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StartProcessReq {
//    private Long procDefId;
    //流程定义编码
    private String procCode;

    @NotBlank(message = "业务主键不能为空")
    private Long busId;

    /** 业务类型，如 contract */
    private String busType;

    //标题
    private String title;
}

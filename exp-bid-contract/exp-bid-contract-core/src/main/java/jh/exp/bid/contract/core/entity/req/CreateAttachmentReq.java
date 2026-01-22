package jh.exp.bid.contract.core.entity.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建附件请求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateAttachmentReq {

    /**
     * 业务类型（TENDER-招标文件，BID-投标文件）
     */
    @NotBlank(message = "业务类型不能为空")
    private String businessType;

    /**
     * 业务ID（招标ID或投标ID）
     */
    @NotNull(message = "业务ID不能为空")
    private Long businessId;

    /**
     * 文件类型
     */
    @NotBlank(message = "文件类型不能为空")
    private String fileType;

    /**
     * 文件分类
     */
    private String fileCategory;

    /**
     * 原始文件名
     */
    @NotBlank(message = "文件名不能为空")
    private String fileName;

    /**
     * 文件存储路径/URL
     */
    @NotBlank(message = "文件路径不能为空")
    private String filePath;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件格式/扩展名
     */
    private String fileFormat;

    /**
     * 文件MD5校验码
     */
    private String fileMd5;

    /**
     * 文件版本号
     */
    private String versionNo;

    /**
     * 保密级别
     */
    private String securityLevel;

    /**
     * 备注
     */
    private String remark;
}
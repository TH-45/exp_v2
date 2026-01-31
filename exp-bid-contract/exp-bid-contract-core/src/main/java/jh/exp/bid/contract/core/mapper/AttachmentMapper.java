package jh.exp.bid.contract.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jh.exp.bid.contract.core.entity.Attachment;
import jh.exp.bid.contract.core.entity.req.QueryAttachmentReq;
import jh.exp.bid.contract.core.entity.res.AttachmentDetailRes;
import jh.exp.bid.contract.core.entity.res.AttachmentListRes;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 附件Mapper接口
 */
@Mapper
public interface AttachmentMapper extends BaseMapper<Attachment> {

    /**
     * 分页查询附件列表
     * @param page 分页对象
     * @param req 查询条件
     * @return 附件列表
     */
    IPage<AttachmentListRes> selectAttachmentList(IPage<AttachmentListRes> page, @Param("req") QueryAttachmentReq req);

    /**
     * 根据附件ID查询附件详情
     * @param attachmentId 附件ID
     * @return 附件详情
     */
    AttachmentDetailRes selectAttachmentDetailById(@Param("attachmentId") Long attachmentId);

    /**
     * 根据业务ID查询附件列表
     * @param businessType 业务类型
     * @param businessId 业务ID
     * @return 附件列表
     */
    List<AttachmentListRes> selectAttachmentsByBusiness(@Param("businessType") String businessType, @Param("businessId") Long businessId);

    /**
     * 根据文件名和MD5检查文件是否已存在
     * @param fileName 文件名
     * @param fileMd5 MD5校验码
     * @param businessType 业务类型
     * @param businessId 业务ID
     * @return 存在数量
     */
    int countByFileNameAndMd5(@Param("fileName") String fileName, @Param("fileMd5") String fileMd5, @Param("businessType") String businessType, @Param("businessId") Long businessId);

    /**
     * 更新文件下载信息
     * @param attachmentId 附件ID
     * @return 影响行数
     */
    int updateDownloadInfo(@Param("attachmentId") Long attachmentId);

    /**
     * 批量更新文件状态
     * @param attachmentIds 附件ID列表
     * @param fileStatus 文件状态
     * @return 影响行数
     */
    int batchUpdateFileStatus(@Param("attachmentIds") List<Long> attachmentIds, @Param("fileStatus") String fileStatus);

    /**
     * 根据业务类型和业务ID删除附件
     * @param businessType 业务类型
     * @param businessId 业务ID
     * @return 影响行数
     */
    int deleteByBusiness(@Param("businessType") String businessType, @Param("businessId") Long businessId);

    /**
     * 获取文件大小统计
     * @param businessType 业务类型
     * @param businessId 业务ID
     * @return 文件总大小（字节）
     */
    Long getTotalFileSize(@Param("businessType") String businessType, @Param("businessId") Long businessId);

    /**
     * 获取附件数量统计
     * @param businessType 业务类型
     * @param businessId 业务ID
     * @return 附件数量
     */
    Integer getAttachmentCount(@Param("businessType") String businessType, @Param("businessId") Long businessId);

    /**
     * 更新旧版本文件的isLatest状态
     * @param businessType 业务类型
     * @param businessId 业务ID
     * @param fileName 文件名
     * @return 影响行数
     */
    int updateOldVersionsToNotLatest(@Param("businessType") String businessType, @Param("businessId") Long businessId, @Param("fileName") String fileName);
}
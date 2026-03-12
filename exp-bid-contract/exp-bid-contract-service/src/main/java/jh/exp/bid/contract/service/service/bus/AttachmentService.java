package jh.exp.bid.contract.service.service.bus;

import jh.exp.bid.contract.core.entity.req.CreateAttachmentReq;
import jh.exp.bid.contract.core.entity.req.CreateAttachmentBizReq;
import jh.exp.bid.contract.core.entity.req.QueryAttachmentReq;
import jh.exp.bid.contract.core.entity.res.AttachmentDetailRes;
import jh.exp.bid.contract.core.entity.res.AttachmentListRes;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 附件服务接口
 */
public interface AttachmentService {

    /**
     * 分页查询附件列表
     */
    SimplePageRes<AttachmentListRes> queryAttachmentList(SimplePageReq<QueryAttachmentReq> req);

    /**
     * 根据ID查询附件详情
     */
    AttachmentDetailRes getAttachmentById(Long attachmentId);

    /**
     * 上传附件
     */
    AttachmentDetailRes uploadAttachment(MultipartFile file, CreateAttachmentBizReq biz);

    /**
     * 批量上传附件（仅元数据，无文件流，供历史接口兼容）
     */
    List<AttachmentDetailRes> batchUploadAttachments(List<CreateAttachmentReq> attachments);

    /**
     * 多文件上传（文件必填，每个文件对应一份 biz，全成全败；任一失败则回滚并删除已上传的存储对象）
     *
     * @param files   文件列表，不可为空且每个文件不可 empty
     * @param bizList 与 files 一一对应的业务参数列表，files.size() 必须等于 bizList.size()
     * @return 按顺序的附件详情列表
     */
    List<AttachmentDetailRes> uploadAttachments(List<MultipartFile> files, List<CreateAttachmentBizReq> bizList);

    /**
     * 更新附件信息
     */
    AttachmentDetailRes updateAttachment(Long attachmentId, CreateAttachmentReq req);

    /**
     * 删除附件
     */
    void deleteAttachment(Long attachmentId);

    /**
     * 批量删除附件
     */
    void batchDeleteAttachments(List<Long> attachmentIds);

    /**
     * 更新文件状态
     */
    AttachmentDetailRes updateFileStatus(Long attachmentId, String fileStatus);

    /**
     * 批量更新文件状态
     */
    void batchUpdateFileStatus(List<Long> attachmentIds, String fileStatus);

    /**
     * 根据业务查询附件列表
     */
    List<AttachmentListRes> getAttachmentsByBusiness(String businessType, Long businessId);

    /**
     * 下载附件（记录下载信息）
     */
    AttachmentDetailRes downloadAttachment(Long attachmentId);

    /**
     * 检查文件是否已存在
     */
    boolean checkFileExists(String fileName, String fileMd5, String businessType, Long businessId);

    /**
     * 获取业务附件统计信息
     */
    AttachmentStatistics getBusinessAttachmentStatistics(String businessType, Long businessId);

    /**
     * 清理过期或无效的附件
     */
    void cleanupInvalidAttachments();

    /**
     * 附件统计信息类
     */
    class AttachmentStatistics {
        private Integer totalCount;
        private Long totalSize;
        private Integer downloadCount;

        public AttachmentStatistics() {}

        public AttachmentStatistics(Integer totalCount, Long totalSize, Integer downloadCount) {
            this.totalCount = totalCount;
            this.totalSize = totalSize;
            this.downloadCount = downloadCount;
        }

        // Getters and Setters
        public Integer getTotalCount() { return totalCount; }
        public void setTotalCount(Integer totalCount) { this.totalCount = totalCount; }

        public Long getTotalSize() { return totalSize; }
        public void setTotalSize(Long totalSize) { this.totalSize = totalSize; }

        public Integer getDownloadCount() { return downloadCount; }
        public void setDownloadCount(Integer downloadCount) { this.downloadCount = downloadCount; }
    }
}
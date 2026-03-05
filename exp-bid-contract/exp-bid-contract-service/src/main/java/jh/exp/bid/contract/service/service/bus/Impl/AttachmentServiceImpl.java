package jh.exp.bid.contract.service.service.bus.Impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jh.exp.auth.clinet.api.bus.PersonService;
import jh.exp.auth.core.entity.res.PersonDetailRes;
import jh.exp.bid.contract.core.entity.Attachment;
import jh.exp.bid.contract.core.entity.req.CreateAttachmentBizReq;
import jh.exp.bid.contract.core.entity.req.CreateAttachmentReq;
import jh.exp.bid.contract.core.entity.req.QueryAttachmentReq;
import jh.exp.bid.contract.core.entity.res.AttachmentDetailRes;
import jh.exp.bid.contract.core.entity.res.AttachmentListRes;
import jh.exp.bid.contract.core.mapper.AttachmentMapper;
import jh.exp.bid.contract.service.service.bus.AttachmentService;
import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.auth.CurrentUserHolder;
import jh.exp.common.core.auth.dto.CurrentUser;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.sys.client.api.storage.StorageService;
import jh.exp.sys.core.req.storage.StorageDeleteReq;
import jh.exp.sys.core.req.storage.StorageUploadBizReq;
import jh.exp.sys.core.res.storage.StorageUploadRes;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 附件服务实现类
 */
@Service
@RequiredArgsConstructor
public class AttachmentServiceImpl implements AttachmentService {

    private final AttachmentMapper attachmentMapper;
    private final PersonService personService;
    private final StorageService storageService;

    @Override
    public SimplePageRes<AttachmentListRes> queryAttachmentList(SimplePageReq<QueryAttachmentReq> req) {
        Page<AttachmentListRes> page = new Page<>(req.getPageNum(), req.getPageSize());

        QueryAttachmentReq queryParam = req.getQueryParam();
        if (queryParam == null) {
            queryParam = new QueryAttachmentReq();
        }

        IPage<AttachmentListRes> result = attachmentMapper.selectAttachmentList(page, queryParam);

        SimplePageRes<AttachmentListRes> pageRes = new SimplePageRes<>();
        pageRes.setList(result.getRecords());
        pageRes.setTotal(result.getTotal());
        pageRes.setPage(result.getCurrent());
        pageRes.setSize(result.getSize());
        return pageRes;
    }

    @Override
    public AttachmentDetailRes getAttachmentById(Long attachmentId) {
        AttachmentDetailRes attachment = attachmentMapper.selectAttachmentDetailById(attachmentId);
        if (attachment == null) {
            throw new RuntimeException("附件不存在");
        }
        return attachment;
    }

    @Override
    @Transactional
    public AttachmentDetailRes uploadAttachment(MultipartFile file, CreateAttachmentBizReq biz) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("上传文件不能为空");
        }
        String originalFileName = StringUtils.hasText(file.getOriginalFilename()) ? file.getOriginalFilename() : "unknown.bin";
        ApiResponse<StorageUploadRes> storageRes;
        try {
            storageRes = storageService.upload(new NamedByteArrayResource(file.getBytes(), originalFileName), buildStorageBizReq(biz));
        } catch (Exception e) {
            throw new RuntimeException("调用存储服务上传失败", e);
        }
        if (storageRes == null || !storageRes.isSuccess() || storageRes.getData() == null) {
            throw new RuntimeException(storageRes == null ? "存储服务响应为空" : storageRes.getMessage());
        }
        StorageUploadRes uploadRes = storageRes.getData();

        // 检查文件是否已存在
        if (checkFileExists(originalFileName, uploadRes.getFileMd5(), biz.getBusinessType(), biz.getBusinessId())) {
            StorageDeleteReq deleteReq = new StorageDeleteReq();
            deleteReq.setObjectKey(uploadRes.getObjectKey());
            storageService.delete(deleteReq);
            throw new RuntimeException("相同文件已存在，请勿重复上传");
        }

        CreateAttachmentReq req = new CreateAttachmentReq();
        req.setBusinessType(biz.getBusinessType());
        req.setBusinessId(biz.getBusinessId());
        req.setFileType(biz.getFileType());
        req.setFileCategory(biz.getFileCategory());
        req.setFileName(uploadRes.getFileName());
        req.setFilePath(uploadRes.getObjectKey());
        req.setFileSize(uploadRes.getFileSize());
        req.setFileFormat(extractFileExt(uploadRes.getFileName()));
        req.setFileMd5(uploadRes.getFileMd5());
        req.setVersionNo(biz.getVersionNo());
        req.setSecurityLevel(biz.getSecurityLevel());
        req.setRemark(biz.getRemark());

        return saveAttachment(req);
    }

    private AttachmentDetailRes saveAttachment(CreateAttachmentReq req) {
        CurrentUser currentUser = CurrentUserHolder.get();
        Long personId = Long.valueOf(currentUser.getUserId());

        PersonDetailRes personDetail = personService.getPersonById(personId);
        if (personDetail == null) {
            throw new RuntimeException("无法获取当前用户信息");
        }

        // 如果是新版本文件，先将旧版本标记为非最新
        if (StringUtils.hasText(req.getVersionNo())) {
            attachmentMapper.updateOldVersionsToNotLatest(req.getBusinessType(), req.getBusinessId(), req.getFileName());
        }

        Attachment attachment = new Attachment();
        attachment.setBusinessType(req.getBusinessType());
        attachment.setBusinessId(req.getBusinessId());
        attachment.setFileType(req.getFileType());
        attachment.setFileCategory(req.getFileCategory());
        attachment.setFileName(req.getFileName());
        attachment.setFilePath(req.getFilePath());
        attachment.setFileSize(req.getFileSize());
        attachment.setFileFormat(req.getFileFormat());
        attachment.setFileMd5(req.getFileMd5());
        attachment.setVersionNo(req.getVersionNo());
        attachment.setIsLatest(1);
        attachment.setUploadUserId(personId);
        attachment.setUploadTime(LocalDateTime.now());
        attachment.setDownloadCount(0);
        attachment.setFileStatus("正常");
        attachment.setSecurityLevel(StringUtils.hasText(req.getSecurityLevel()) ? req.getSecurityLevel() : "内部");
        attachment.setRemark(req.getRemark());
        attachment.setCreatedTime(LocalDateTime.now());
        attachment.setUpdatedTime(LocalDateTime.now());
        attachment.setCreatedBy(personId);
        attachment.setCreatedDeptId(personDetail.getOrgId());
        attachment.setCreatedPostId(personDetail.getPostId());

        attachmentMapper.insert(attachment);
        return getAttachmentById(attachment.getAttachmentId());
    }

    @Override
    @Transactional
    public List<AttachmentDetailRes> batchUploadAttachments(List<CreateAttachmentReq> attachments) {
        List<AttachmentDetailRes> results = new ArrayList<>();
        for (CreateAttachmentReq req : attachments) {
            try {
                AttachmentDetailRes result = saveAttachment(req);
                results.add(result);
            } catch (Exception e) {
                // 记录错误但不中断整个批量操作
                // 可以考虑添加错误处理逻辑
            }
        }
        return results;
    }

    @Override
    @Transactional
    public AttachmentDetailRes updateAttachment(Long attachmentId, CreateAttachmentReq req) {
        Attachment existingAttachment = attachmentMapper.selectById(attachmentId);
        if (existingAttachment == null) {
            throw new RuntimeException("附件不存在");
        }

        // 检查文件名和MD5是否与其他文件冲突
        if (checkFileExists(req.getFileName(), req.getFileMd5(), req.getBusinessType(), req.getBusinessId())) {
            Attachment conflictAttachment = attachmentMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Attachment>()
                    .eq("file_name", req.getFileName())
                    .eq("file_md5", req.getFileMd5())
                    .eq("business_type", req.getBusinessType())
                    .eq("business_id", req.getBusinessId())
                    .ne("attachment_id", attachmentId)
            );
            if (conflictAttachment != null) {
                throw new RuntimeException("相同文件已存在");
            }
        }

        Attachment attachment = new Attachment();
        attachment.setAttachmentId(attachmentId);
        attachment.setFileType(req.getFileType());
        attachment.setFileCategory(req.getFileCategory());
        attachment.setFileName(req.getFileName());
        attachment.setFilePath(req.getFilePath());
        attachment.setFileSize(req.getFileSize());
        attachment.setFileFormat(req.getFileFormat());
        attachment.setFileMd5(req.getFileMd5());
        attachment.setVersionNo(req.getVersionNo());
        attachment.setSecurityLevel(req.getSecurityLevel());
        attachment.setRemark(req.getRemark());
        attachment.setUpdatedTime(LocalDateTime.now());

        attachmentMapper.updateById(attachment);
        return getAttachmentById(attachmentId);
    }

    @Override
    @Transactional
    public void deleteAttachment(Long attachmentId) {
        Attachment attachment = attachmentMapper.selectById(attachmentId);
        if (attachment == null) {
            throw new RuntimeException("附件不存在");
        }
        if (StringUtils.hasText(attachment.getFilePath())) {
            StorageDeleteReq deleteReq = new StorageDeleteReq();
            deleteReq.setObjectKey(attachment.getFilePath());
            storageService.delete(deleteReq);
        }

        // 逻辑删除：更新状态为已删除
        attachment.setFileStatus("已删除");
        attachment.setUpdatedTime(LocalDateTime.now());
        attachmentMapper.updateById(attachment);
    }

    @Override
    @Transactional
    public void batchDeleteAttachments(List<Long> attachmentIds) {
        if (attachmentIds == null || attachmentIds.isEmpty()) {
            return;
        }
        List<Attachment> attachments = attachmentMapper.selectBatchIds(attachmentIds);
        for (Attachment attachment : attachments) {
            if (attachment != null && StringUtils.hasText(attachment.getFilePath())) {
                StorageDeleteReq deleteReq = new StorageDeleteReq();
                deleteReq.setObjectKey(attachment.getFilePath());
                storageService.delete(deleteReq);
            }
        }
        attachmentMapper.batchUpdateFileStatus(attachmentIds, "已删除");
    }

    @Override
    @Transactional
    public AttachmentDetailRes updateFileStatus(Long attachmentId, String fileStatus) {
        attachmentMapper.updateDownloadInfo(attachmentId); // 如果是下载，更新下载信息
        Attachment attachment = attachmentMapper.selectById(attachmentId);
        if (attachment != null) {
            attachment.setFileStatus(fileStatus);
            attachment.setUpdatedTime(LocalDateTime.now());
            attachmentMapper.updateById(attachment);
        }
        return getAttachmentById(attachmentId);
    }

    @Override
    @Transactional
    public void batchUpdateFileStatus(List<Long> attachmentIds, String fileStatus) {
        if (attachmentIds == null || attachmentIds.isEmpty()) {
            return;
        }
        attachmentMapper.batchUpdateFileStatus(attachmentIds, fileStatus);
    }

    @Override
    public List<AttachmentListRes> getAttachmentsByBusiness(String businessType, Long businessId) {
        return attachmentMapper.selectAttachmentsByBusiness(businessType, businessId);
    }

    @Override
    @Transactional
    public AttachmentDetailRes downloadAttachment(Long attachmentId) {
        attachmentMapper.updateDownloadInfo(attachmentId);
        return getAttachmentById(attachmentId);
    }

    @Override
    public boolean checkFileExists(String fileName, String fileMd5, String businessType, Long businessId) {
        return attachmentMapper.countByFileNameAndMd5(fileName, fileMd5, businessType, businessId) > 0;
    }

    @Override
    public AttachmentStatistics getBusinessAttachmentStatistics(String businessType, Long businessId) {
        Integer totalCount = attachmentMapper.getAttachmentCount(businessType, businessId);
        Long totalSize = attachmentMapper.getTotalFileSize(businessType, businessId);

        // 计算总下载次数（这里简化处理，实际可能需要更复杂的统计）
        Integer downloadCount = 0; // 可以后续实现

        return new AttachmentStatistics(totalCount, totalSize, downloadCount);
    }

    @Override
    @Transactional
    public void cleanupInvalidAttachments() {
        // 这里可以实现清理逻辑，比如删除过期文件、清理无效记录等
        // 暂时留空，后续根据业务需求实现
    }

    private StorageUploadBizReq buildStorageBizReq(CreateAttachmentBizReq biz) {
        StorageUploadBizReq req = new StorageUploadBizReq();
        req.setBusinessType(biz.getBusinessType());
        req.setBusinessId(biz.getBusinessId());
        req.setFileType(biz.getFileType());
        req.setFileCategory(biz.getFileCategory());
        req.setVersionNo(biz.getVersionNo());
        req.setSecurityLevel(biz.getSecurityLevel());
        req.setRemark(biz.getRemark());
        return req;
    }

    private String extractFileExt(String fileName) {
        if (!StringUtils.hasText(fileName) || !fileName.contains(".")) {
            return null;
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }

    private static class NamedByteArrayResource extends ByteArrayResource {
        private final String filename;

        NamedByteArrayResource(byte[] byteArray, String filename) {
            super(byteArray);
            this.filename = filename;
        }

        @Override
        public String getFilename() {
            return filename;
        }
    }
}
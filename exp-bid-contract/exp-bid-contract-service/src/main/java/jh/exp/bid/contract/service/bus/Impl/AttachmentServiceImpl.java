package jh.exp.bid.contract.service.bus.Impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jh.exp.auth.clinet.api.PersonService;

import jh.exp.bid.contract.entity.ExpAttachment;
import jh.exp.bid.contract.entity.req.CreateAttachmentReq;
import jh.exp.bid.contract.entity.req.QueryAttachmentReq;
import jh.exp.bid.contract.entity.res.AttachmentDetailRes;
import jh.exp.bid.contract.entity.res.AttachmentListRes;
import jh.exp.bid.contract.mapper.AttachmentMapper;
import jh.exp.bid.contract.service.bus.AttachmentService;
import jh.exp.common.auth.CurrentUserHolder;
import jh.exp.common.auth.dto.CurrentUser;
import jh.exp.common.req.SimplePageReq;
import jh.exp.common.res.SimplePageRes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
    public AttachmentDetailRes uploadAttachment(CreateAttachmentReq req) {
        // 检查文件是否已存在
        if (checkFileExists(req.getFileName(), req.getFileMd5(), req.getBusinessType(), req.getBusinessId())) {
            throw new RuntimeException("相同文件已存在，请勿重复上传");
        }

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

        ExpAttachment attachment = new ExpAttachment();
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
        attachment.setIsLatest(StringUtils.hasText(req.getVersionNo()) ? 1 : 1); // 默认为最新版本
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
                AttachmentDetailRes result = uploadAttachment(req);
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
        ExpAttachment existingAttachment = attachmentMapper.selectById(attachmentId);
        if (existingAttachment == null) {
            throw new RuntimeException("附件不存在");
        }

        // 检查文件名和MD5是否与其他文件冲突
        if (checkFileExists(req.getFileName(), req.getFileMd5(), req.getBusinessType(), req.getBusinessId())) {
            ExpAttachment conflictAttachment = attachmentMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ExpAttachment>()
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

        ExpAttachment attachment = new ExpAttachment();
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
        ExpAttachment attachment = attachmentMapper.selectById(attachmentId);
        if (attachment == null) {
            throw new RuntimeException("附件不存在");
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
        attachmentMapper.batchUpdateFileStatus(attachmentIds, "已删除");
    }

    @Override
    @Transactional
    public AttachmentDetailRes updateFileStatus(Long attachmentId, String fileStatus) {
        attachmentMapper.updateDownloadInfo(attachmentId); // 如果是下载，更新下载信息
        ExpAttachment attachment = attachmentMapper.selectById(attachmentId);
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
}
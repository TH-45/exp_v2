package jh.exp.corp.service.service.bus.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.auth.CurrentUserHolder;
import jh.exp.common.core.auth.dto.CurrentUser;
import jh.exp.common.core.exception.GatewayBizException;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.corp.core.constant.CorpErrorCode;
import jh.exp.corp.core.entity.Qualification;
import jh.exp.corp.core.entity.QualificationAttachment;
import jh.exp.corp.core.entity.req.*;
import jh.exp.corp.core.entity.res.QualificationAttachmentDetailRes;
import jh.exp.corp.core.entity.res.QualificationAttachmentListRes;
import jh.exp.corp.core.mapper.QualificationMapper;
import jh.exp.corp.core.mapper.QualificationAttachmentMapper;
import jh.exp.corp.service.service.bus.QualificationAttachmentInternalService;
import jh.exp.corp.service.service.support.CurrentCompanyResolver;
import jh.exp.sys.client.api.storage.StorageService;
import jh.exp.sys.core.req.storage.StorageDeleteReq;
import jh.exp.sys.core.req.storage.StorageUploadBizReq;
import jh.exp.sys.core.res.storage.StorageUploadRes;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QualificationAttachmentInternalServiceImpl implements QualificationAttachmentInternalService {

    private final QualificationAttachmentMapper qualificationAttachmentMapper;
    private final QualificationMapper qualificationMapper;
    private final CurrentCompanyResolver currentCompanyResolver;
    private final StorageService storageService;

    @Override
    public SimplePageRes<QualificationAttachmentListRes> list(SimplePageReq<QueryQualificationAttachmentReq> req) {
        Long currentCompanyId = currentCompanyResolver.resolveCurrentCompanyId();
        QueryQualificationAttachmentReq query = req.getQueryParam() == null ? new QueryQualificationAttachmentReq() : req.getQueryParam();
        if (query.getQualificationId() == null) {
            throw new GatewayBizException(CorpErrorCode.QUALIFICATION_NOT_FOUND, "qualificationId不能为空");
        }
        Page<QualificationAttachment> page = new Page<>(req.getPageNum(), req.getPageSize());
        LambdaQueryWrapper<QualificationAttachment> wrapper = new LambdaQueryWrapper<>();
        Qualification qualification = qualificationMapper.selectById(query.getQualificationId());
        if (qualification == null || !currentCompanyId.equals(qualification.getCompanyId())) {
            throw new GatewayBizException(CorpErrorCode.QUALIFICATION_NOT_FOUND, "资质不存在");
        }
        wrapper.eq(QualificationAttachment::getQualificationId, query.getQualificationId())
                .like(StringUtils.hasText(query.getFileName()), QualificationAttachment::getFileName, query.getFileName())
                .eq(query.getUploadUserId() != null, QualificationAttachment::getUploadUserId, query.getUploadUserId())
                .orderByDesc(QualificationAttachment::getAttachmentId);
        IPage<QualificationAttachment> result = qualificationAttachmentMapper.selectPage(page, wrapper);
        List<QualificationAttachmentListRes> list = result.getRecords().stream().map(this::toListRes).collect(Collectors.toList());
        SimplePageRes<QualificationAttachmentListRes> pageRes = new SimplePageRes<>();
        pageRes.setList(list);
        pageRes.setPage(result.getCurrent());
        pageRes.setSize(result.getSize());
        pageRes.setTotal(result.getTotal());
        return pageRes;
    }

    @Override
    public QualificationAttachmentDetailRes detail(Long attachmentId) {
        QualificationAttachment entity = qualificationAttachmentMapper.selectById(attachmentId);
        Long currentCompanyId = currentCompanyResolver.resolveCurrentCompanyId();
        if (entity == null || !isCurrentCompanyQualification(entity.getQualificationId(), currentCompanyId)) {
            throw new GatewayBizException(CorpErrorCode.ATTACHMENT_NOT_FOUND, "资质附件不存在");
        }
        QualificationAttachmentDetailRes res = new QualificationAttachmentDetailRes();
        BeanUtils.copyProperties(entity, res);
        return res;
    }

    @Override
    @Transactional
    public QualificationAttachmentDetailRes create(CreateQualificationAttachmentReq req) {
        Long currentCompanyId = currentCompanyResolver.resolveCurrentCompanyId();
        ensureQualificationOwnership(req.getQualificationId(), currentCompanyId);
        QualificationAttachment entity = new QualificationAttachment();
        BeanUtils.copyProperties(req, entity);
        if (entity.getUploadTime() == null) {
            entity.setUploadTime(LocalDateTime.now());
        }
        qualificationAttachmentMapper.insert(entity);
        return detail(entity.getAttachmentId());
    }

    @Override
    @Transactional
    public QualificationAttachmentDetailRes update(UpdateQualificationAttachmentReq req) {
        QualificationAttachment old = qualificationAttachmentMapper.selectById(req.getAttachmentId());
        Long currentCompanyId = currentCompanyResolver.resolveCurrentCompanyId();
        if (old == null || !isCurrentCompanyQualification(old.getQualificationId(), currentCompanyId)) {
            throw new GatewayBizException(CorpErrorCode.ATTACHMENT_NOT_FOUND, "资质附件不存在");
        }
        BeanUtils.copyProperties(req, old);
        if (old.getQualificationId() != null) {
            ensureQualificationOwnership(old.getQualificationId(), currentCompanyId);
        }
        qualificationAttachmentMapper.updateById(old);
        return detail(old.getAttachmentId());
    }

    @Override
    @Transactional
    public void delete(DeleteQualificationAttachmentReq req) {
        QualificationAttachment entity = qualificationAttachmentMapper.selectById(req.getAttachmentId());
        Long currentCompanyId = currentCompanyResolver.resolveCurrentCompanyId();
        if (entity == null || !isCurrentCompanyQualification(entity.getQualificationId(), currentCompanyId)) {
            throw new GatewayBizException(CorpErrorCode.ATTACHMENT_NOT_FOUND, "资质附件不存在");
        }
        if (StringUtils.hasText(entity.getFilePath())) {
            StorageDeleteReq deleteReq = new StorageDeleteReq();
            deleteReq.setObjectKey(entity.getFilePath());
            storageService.delete(deleteReq);
        }
        qualificationAttachmentMapper.deleteById(req.getAttachmentId());
    }

    @Override
    @Transactional
    public void batchDelete(BatchDeleteQualificationAttachmentReq req) {
        if (req.getAttachmentIds() == null || req.getAttachmentIds().isEmpty()) {
            return;
        }
        Long currentCompanyId = currentCompanyResolver.resolveCurrentCompanyId();
        LambdaQueryWrapper<QualificationAttachment> attachmentWrapper = new LambdaQueryWrapper<>();
        attachmentWrapper.in(QualificationAttachment::getAttachmentId, req.getAttachmentIds());
        List<QualificationAttachment> attachments = qualificationAttachmentMapper.selectList(attachmentWrapper);
        if (attachments.isEmpty()) {
            return;
        }
        Set<Long> qualificationIds = attachments.stream().map(QualificationAttachment::getQualificationId).collect(Collectors.toSet());
        LambdaQueryWrapper<Qualification> qualificationWrapper = new LambdaQueryWrapper<>();
        qualificationWrapper.in(Qualification::getQualificationId, qualificationIds)
                .eq(Qualification::getCompanyId, currentCompanyId);
        Set<Long> currentCompanyQualificationIds = qualificationMapper.selectList(qualificationWrapper).stream()
                .map(Qualification::getQualificationId)
                .collect(Collectors.toSet());
        List<QualificationAttachment> ownedAttachments = attachments.stream()
                .filter(item -> currentCompanyQualificationIds.contains(item.getQualificationId()))
                .collect(Collectors.toList());
        if (ownedAttachments.isEmpty()) {
            return;
        }
        for (QualificationAttachment attachment : ownedAttachments) {
            if (StringUtils.hasText(attachment.getFilePath())) {
                StorageDeleteReq deleteReq = new StorageDeleteReq();
                deleteReq.setObjectKey(attachment.getFilePath());
                storageService.delete(deleteReq);
            }
        }
        List<Long> ownedAttachmentIds = ownedAttachments.stream()
                .map(QualificationAttachment::getAttachmentId)
                .collect(Collectors.toList());
        LambdaQueryWrapper<QualificationAttachment> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.in(QualificationAttachment::getAttachmentId, ownedAttachmentIds);
        qualificationAttachmentMapper.delete(deleteWrapper);
    }

    @Override
    @Transactional
    public QualificationAttachmentDetailRes upload(Long qualificationId, MultipartFile file) {
        Long currentCompanyId = currentCompanyResolver.resolveCurrentCompanyId();
        ensureQualificationOwnership(qualificationId, currentCompanyId);
        if (file == null || file.isEmpty()) {
            throw new GatewayBizException(CorpErrorCode.ATTACHMENT_NOT_FOUND, "上传文件不能为空");
        }

        String fileName = StringUtils.hasText(file.getOriginalFilename()) ? file.getOriginalFilename() : "unknown.bin";
        StorageUploadBizReq storageBiz = new StorageUploadBizReq();
        storageBiz.setBusinessType("QUALIFICATION");
        storageBiz.setBusinessId(qualificationId);
        storageBiz.setFileType(extractExt(fileName));
        storageBiz.setFileCategory("QUALIFICATION_ATTACHMENT");

        ApiResponse<StorageUploadRes> uploadRes;
        try {
            uploadRes = storageService.upload(new NamedByteArrayResource(file.getBytes(), fileName), storageBiz);
        } catch (IOException e) {
            throw new RuntimeException("读取上传文件失败", e);
        }
        if (uploadRes == null || !uploadRes.isSuccess() || uploadRes.getData() == null) {
            throw new RuntimeException(uploadRes == null ? "存储服务上传失败" : uploadRes.getMessage());
        }

        StorageUploadRes data = uploadRes.getData();
        CurrentUser currentUser = CurrentUserHolder.get();
        QualificationAttachment entity = new QualificationAttachment();
        entity.setQualificationId(qualificationId);
        entity.setFileName(data.getFileName());
        entity.setFilePath(data.getObjectKey());
        entity.setFileSize(data.getFileSize());
        entity.setUploadUserId(currentUser == null ? null : currentUser.getUserId());
        entity.setUploadTime(LocalDateTime.now());
        qualificationAttachmentMapper.insert(entity);

        Qualification qualification = qualificationMapper.selectById(qualificationId);
        if (qualification != null) {
            qualification.setAttachFlag(1);
            qualification.setUpdatedTime(LocalDateTime.now());
            qualificationMapper.updateById(qualification);
        }
        return detail(entity.getAttachmentId());
    }

    @Override
    public byte[] download(Long attachmentId) {
        QualificationAttachment entity = qualificationAttachmentMapper.selectById(attachmentId);
        Long currentCompanyId = currentCompanyResolver.resolveCurrentCompanyId();
        if (entity == null || !isCurrentCompanyQualification(entity.getQualificationId(), currentCompanyId)) {
            throw new GatewayBizException(CorpErrorCode.ATTACHMENT_NOT_FOUND, "资质附件不存在");
        }
        return storageService.download(entity.getFilePath());
    }

    private QualificationAttachmentListRes toListRes(QualificationAttachment entity) {
        QualificationAttachmentListRes res = new QualificationAttachmentListRes();
        BeanUtils.copyProperties(entity, res);
        return res;
    }

    private boolean isCurrentCompanyQualification(Long qualificationId, Long currentCompanyId) {
        Qualification qualification = qualificationMapper.selectById(qualificationId);
        return qualification != null && currentCompanyId.equals(qualification.getCompanyId());
    }

    private void ensureQualificationOwnership(Long qualificationId, Long currentCompanyId) {
        if (!isCurrentCompanyQualification(qualificationId, currentCompanyId)) {
            throw new GatewayBizException(CorpErrorCode.QUALIFICATION_NOT_FOUND, "资质不存在");
        }
    }

    private String extractExt(String fileName) {
        if (!StringUtils.hasText(fileName) || !fileName.contains(".")) {
            return null;
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }

    private static class NamedByteArrayResource extends ByteArrayResource {
        private final String fileName;

        NamedByteArrayResource(byte[] byteArray, String fileName) {
            super(byteArray);
            this.fileName = fileName;
        }

        @Override
        public String getFilename() {
            return fileName;
        }
    }
}

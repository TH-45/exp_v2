package jh.exp.corp.service.service.bus.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.auth.CurrentUserHolder;
import jh.exp.common.core.auth.dto.CurrentUser;
import jh.exp.common.core.exception.GatewayBizException;
import jh.exp.corp.core.constant.CorpErrorCode;
import jh.exp.corp.core.entity.Notice;
import jh.exp.corp.core.entity.NoticeAttachment;
import jh.exp.corp.core.entity.req.DeleteNoticeAttachmentReq;
import jh.exp.corp.core.entity.req.QueryNoticeAttachmentReq;
import jh.exp.corp.core.entity.res.NoticeAttachmentRes;
import jh.exp.corp.core.mapper.NoticeAttachmentMapper;
import jh.exp.corp.core.mapper.NoticeMapper;
import jh.exp.corp.service.service.bus.NoticeAttachmentInternalService;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoticeAttachmentInternalServiceImpl implements NoticeAttachmentInternalService {

    private final NoticeMapper noticeMapper;
    private final NoticeAttachmentMapper noticeAttachmentMapper;
    private final StorageService storageService;

    @Override
    public List<NoticeAttachmentRes> list(QueryNoticeAttachmentReq req) {
        LambdaQueryWrapper<NoticeAttachment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(req.getNoticeId() != null, NoticeAttachment::getNoticeId, req.getNoticeId())
                .orderByDesc(NoticeAttachment::getAttachmentId);
        return noticeAttachmentMapper.selectList(wrapper).stream().map(this::toRes).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public NoticeAttachmentRes upload(Long noticeId, MultipartFile file) {
        Notice notice = noticeMapper.selectById(noticeId);
        if (notice == null) {
            throw new GatewayBizException(CorpErrorCode.NOTICE_NOT_FOUND, "公告不存在");
        }
        if (file == null || file.isEmpty()) {
            throw new GatewayBizException(CorpErrorCode.ATTACHMENT_NOT_FOUND, "上传文件不能为空");
        }
        String fileName = StringUtils.hasText(file.getOriginalFilename()) ? file.getOriginalFilename() : "unknown.bin";
        StorageUploadBizReq storageBiz = new StorageUploadBizReq();
        storageBiz.setBusinessType("NOTICE");
        storageBiz.setBusinessId(noticeId);
        storageBiz.setFileType(extractExt(fileName));
        storageBiz.setFileCategory("NOTICE_ATTACHMENT");

        ApiResponse<StorageUploadRes> uploadRes;
        try {
            uploadRes = storageService.upload(new NamedByteArrayResource(file.getBytes(), fileName), storageBiz);
        } catch (IOException e) {
            throw new RuntimeException("读取上传文件失败", e);
        }
        if (uploadRes == null || !uploadRes.isSuccess() || uploadRes.getData() == null) {
            throw new RuntimeException(uploadRes == null ? "存储服务上传失败" : uploadRes.getMessage());
        }

        CurrentUser currentUser = CurrentUserHolder.get();
        Long userId = currentUser == null ? null : currentUser.getUserId();
        StorageUploadRes data = uploadRes.getData();
        NoticeAttachment attachment = new NoticeAttachment();
        attachment.setNoticeId(noticeId);
        attachment.setFileName(data.getFileName());
        attachment.setFilePath(data.getObjectKey());
        attachment.setFileSize(data.getFileSize());
        attachment.setUploadUserId(userId);
        attachment.setUploadTime(LocalDateTime.now());
        noticeAttachmentMapper.insert(attachment);

        notice.setAttachFlag(1);
        notice.setUpdatedTime(LocalDateTime.now());
        noticeMapper.updateById(notice);
        return toRes(attachment);
    }

    @Override
    @Transactional
    public void delete(DeleteNoticeAttachmentReq req) {
        NoticeAttachment attachment = noticeAttachmentMapper.selectById(req.getAttachmentId());
        if (attachment == null) {
            throw new GatewayBizException(CorpErrorCode.ATTACHMENT_NOT_FOUND, "附件不存在");
        }
        if (StringUtils.hasText(attachment.getFilePath())) {
            StorageDeleteReq deleteReq = new StorageDeleteReq();
            deleteReq.setObjectKey(attachment.getFilePath());
            storageService.delete(deleteReq);
        }
        noticeAttachmentMapper.deleteById(req.getAttachmentId());

        LambdaQueryWrapper<NoticeAttachment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoticeAttachment::getNoticeId, attachment.getNoticeId());
        Long remaining = noticeAttachmentMapper.selectCount(wrapper);
        Notice notice = noticeMapper.selectById(attachment.getNoticeId());
        if (notice != null) {
            notice.setAttachFlag(remaining != null && remaining > 0 ? 1 : 0);
            notice.setUpdatedTime(LocalDateTime.now());
            noticeMapper.updateById(notice);
        }
    }

    @Override
    public byte[] download(Long attachmentId) {
        NoticeAttachment attachment = noticeAttachmentMapper.selectById(attachmentId);
        if (attachment == null) {
            throw new GatewayBizException(CorpErrorCode.ATTACHMENT_NOT_FOUND, "附件不存在");
        }
        return storageService.download(attachment.getFilePath());
    }

    private NoticeAttachmentRes toRes(NoticeAttachment attachment) {
        NoticeAttachmentRes res = new NoticeAttachmentRes();
        BeanUtils.copyProperties(attachment, res);
        return res;
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

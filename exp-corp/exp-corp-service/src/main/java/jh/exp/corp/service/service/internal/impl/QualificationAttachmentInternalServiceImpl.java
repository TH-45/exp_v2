package jh.exp.corp.service.service.internal.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.corp.core.entity.QualificationAttachment;
import jh.exp.corp.core.entity.req.*;
import jh.exp.corp.core.entity.res.QualificationAttachmentDetailRes;
import jh.exp.corp.core.entity.res.QualificationAttachmentListRes;
import jh.exp.corp.core.mapper.QualificationAttachmentMapper;
import jh.exp.corp.service.service.internal.QualificationAttachmentInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QualificationAttachmentInternalServiceImpl implements QualificationAttachmentInternalService {

    private final QualificationAttachmentMapper qualificationAttachmentMapper;

    @Override
    public SimplePageRes<QualificationAttachmentListRes> list(SimplePageReq<QueryQualificationAttachmentReq> req) {
        QueryQualificationAttachmentReq query = req.getQueryParam() == null ? new QueryQualificationAttachmentReq() : req.getQueryParam();
        Page<QualificationAttachment> page = new Page<>(req.getPageNum(), req.getPageSize());
        LambdaQueryWrapper<QualificationAttachment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(query.getQualificationId() != null, QualificationAttachment::getQualificationId, query.getQualificationId())
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
        if (entity == null) {
            throw new RuntimeException("资质附件不存在");
        }
        QualificationAttachmentDetailRes res = new QualificationAttachmentDetailRes();
        BeanUtils.copyProperties(entity, res);
        return res;
    }

    @Override
    @Transactional
    public QualificationAttachmentDetailRes create(CreateQualificationAttachmentReq req) {
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
        if (old == null) {
            throw new RuntimeException("资质附件不存在");
        }
        BeanUtils.copyProperties(req, old);
        qualificationAttachmentMapper.updateById(old);
        return detail(old.getAttachmentId());
    }

    @Override
    @Transactional
    public void delete(DeleteQualificationAttachmentReq req) {
        if (qualificationAttachmentMapper.selectById(req.getAttachmentId()) == null) {
            throw new RuntimeException("资质附件不存在");
        }
        qualificationAttachmentMapper.deleteById(req.getAttachmentId());
    }

    @Override
    @Transactional
    public void batchDelete(BatchDeleteQualificationAttachmentReq req) {
        if (req.getAttachmentIds() == null || req.getAttachmentIds().isEmpty()) {
            return;
        }
        qualificationAttachmentMapper.deleteBatchIds(req.getAttachmentIds());
    }

    private QualificationAttachmentListRes toListRes(QualificationAttachment entity) {
        QualificationAttachmentListRes res = new QualificationAttachmentListRes();
        BeanUtils.copyProperties(entity, res);
        return res;
    }
}

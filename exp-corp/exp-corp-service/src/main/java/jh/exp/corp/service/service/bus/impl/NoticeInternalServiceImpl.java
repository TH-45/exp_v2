package jh.exp.corp.service.service.bus.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jh.exp.common.core.auth.CurrentUserHolder;
import jh.exp.common.core.auth.dto.CurrentUser;
import jh.exp.common.core.exception.GatewayBizException;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.corp.core.constant.CorpErrorCode;
import jh.exp.corp.core.entity.Notice;
import jh.exp.corp.core.entity.NoticeAttachment;
import jh.exp.corp.core.entity.req.*;
import jh.exp.corp.core.entity.res.NoticeAttachmentRes;
import jh.exp.corp.core.entity.res.NoticeDetailRes;
import jh.exp.corp.core.entity.res.NoticeListRes;
import jh.exp.corp.core.mapper.NoticeAttachmentMapper;
import jh.exp.corp.core.mapper.NoticeMapper;
import jh.exp.corp.service.service.bus.NoticeInternalService;
import jh.exp.sys.client.api.storage.StorageService;
import jh.exp.sys.core.req.storage.StorageDeleteReq;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoticeInternalServiceImpl implements NoticeInternalService {

    private static final String STATUS_DRAFT = "DRAFT";
    private static final String STATUS_PUBLISHED = "PUBLISHED";
    private static final String STATUS_WITHDRAWN = "WITHDRAWN";

    private final NoticeMapper noticeMapper;
    private final NoticeAttachmentMapper noticeAttachmentMapper;
    private final StorageService storageService;

    @Override
    public SimplePageRes<NoticeListRes> list(SimplePageReq<QueryNoticeReq> req) {
        QueryNoticeReq query =
                req.getQueryParam() == null ? new QueryNoticeReq() : req.getQueryParam();
        Page<Notice> page = new Page<>(req.getPageNum(), req.getPageSize());
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getTitle()), Notice::getTitle, query.getTitle())
                .eq(StringUtils.hasText(query.getNoticeType()), Notice::getNoticeType, query.getNoticeType())
                .eq(StringUtils.hasText(query.getPublishStatus()), Notice::getPublishStatus, query.getPublishStatus())
                .ge(StringUtils.hasText(query.getPublishStartDate()), Notice::getPublishTime, parseDateStart(query.getPublishStartDate()))
                .le(StringUtils.hasText(query.getPublishEndDate()), Notice::getPublishTime, parseDateEnd(query.getPublishEndDate()))
                .orderByDesc(Notice::getPublishTime)
                .orderByDesc(Notice::getNoticeId);
        IPage<Notice> result = noticeMapper.selectPage(page, wrapper);

        List<Notice> records = result.getRecords();
        List<Long> noticeIds = records.stream().map(Notice::getNoticeId).collect(Collectors.toList());
        Map<Long, List<NoticeAttachmentRes>> attachmentMap = getAttachmentMap(noticeIds);

        List<NoticeListRes> list = records.stream().map(item -> {
            NoticeListRes res = new NoticeListRes();
            res.setNoticeId(item.getNoticeId());
            res.setNoticeType(item.getNoticeType());
            res.setTitle(item.getTitle());
            res.setPublisherUserId(item.getPublisherUserId());
            res.setPublishTime(item.getPublishTime());
            res.setPublishStatus(item.getPublishStatus());
            res.setReadCount(0);
            res.setAttachments(attachmentMap.getOrDefault(item.getNoticeId(), Collections.emptyList()));
            return res;
        }).collect(Collectors.toList());

        SimplePageRes<NoticeListRes> pageRes = new SimplePageRes<>();
        pageRes.setList(list);
        pageRes.setPage(result.getCurrent());
        pageRes.setSize(result.getSize());
        pageRes.setTotal(result.getTotal());
        return pageRes;
    }

    @Override
    public NoticeDetailRes detail(Long noticeId) {
        Notice entity = noticeMapper.selectById(noticeId);
        if (entity == null) {
            throw new GatewayBizException(CorpErrorCode.NOTICE_NOT_FOUND, "公告不存在");
        }
        NoticeDetailRes res = new NoticeDetailRes();
        BeanUtils.copyProperties(entity, res);
        res.setReadCount(0);
        res.setAttachments(getAttachmentMap(List.of(noticeId)).getOrDefault(noticeId, Collections.emptyList()));
        return res;
    }

    @Override
    @Transactional
    public NoticeDetailRes create(CreateNoticeReq req) {
        CurrentUser currentUser = CurrentUserHolder.get();
        Long currentUserId = currentUser == null ? null : currentUser.getUserId();
        Notice entity = new Notice();
        BeanUtils.copyProperties(req, entity);
        entity.setAttachFlag(0);
        entity.setPublishStatus(STATUS_DRAFT);
        entity.setCreatorUserId(currentUserId);
        entity.setCreatedTime(LocalDateTime.now());
        entity.setUpdatedTime(LocalDateTime.now());
        noticeMapper.insert(entity);
        return detail(entity.getNoticeId());
    }

    @Override
    @Transactional
    public NoticeDetailRes update(UpdateNoticeReq req) {
        Notice old = noticeMapper.selectById(req.getNoticeId());
        if (old == null) {
            throw new GatewayBizException(CorpErrorCode.NOTICE_NOT_FOUND, "公告不存在");
        }
        BeanUtils.copyProperties(req, old);
        old.setUpdatedTime(LocalDateTime.now());
        noticeMapper.updateById(old);
        return detail(old.getNoticeId());
    }

    @Override
    @Transactional
    public void delete(DeleteNoticeReq req) {
        Notice old = noticeMapper.selectById(req.getNoticeId());
        if (old == null) {
            throw new GatewayBizException(CorpErrorCode.NOTICE_NOT_FOUND, "公告不存在");
        }
        LambdaQueryWrapper<NoticeAttachment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoticeAttachment::getNoticeId, req.getNoticeId());
        List<NoticeAttachment> attachments = noticeAttachmentMapper.selectList(wrapper);
        for (NoticeAttachment attachment : attachments) {
            if (StringUtils.hasText(attachment.getFilePath())) {
                StorageDeleteReq deleteReq = new StorageDeleteReq();
                deleteReq.setObjectKey(attachment.getFilePath());
                storageService.delete(deleteReq);
            }
        }
        noticeAttachmentMapper.delete(wrapper);
        noticeMapper.deleteById(req.getNoticeId());
    }

    @Override
    @Transactional
    public NoticeDetailRes publish(NoticeActionReq req) {
        CurrentUser currentUser = CurrentUserHolder.get();
        Notice old = noticeMapper.selectById(req.getNoticeId());
        if (old == null) {
            throw new GatewayBizException(CorpErrorCode.NOTICE_NOT_FOUND, "公告不存在");
        }
        old.setPublishStatus(STATUS_PUBLISHED);
        old.setPublishTime(LocalDateTime.now());
        old.setPublisherUserId(currentUser == null ? null : currentUser.getUserId());
        old.setUpdatedTime(LocalDateTime.now());
        noticeMapper.updateById(old);
        return detail(old.getNoticeId());
    }

    @Override
    @Transactional
    public NoticeDetailRes withdraw(NoticeActionReq req) {
        Notice old = noticeMapper.selectById(req.getNoticeId());
        if (old == null) {
            throw new GatewayBizException(CorpErrorCode.NOTICE_NOT_FOUND, "公告不存在");
        }
        old.setPublishStatus(STATUS_WITHDRAWN);
        old.setUpdatedTime(LocalDateTime.now());
        noticeMapper.updateById(old);
        return detail(old.getNoticeId());
    }

    private Map<Long, List<NoticeAttachmentRes>> getAttachmentMap(List<Long> noticeIds) {
        if (noticeIds == null || noticeIds.isEmpty()) {
            return Collections.emptyMap();
        }
        LambdaQueryWrapper<NoticeAttachment> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(NoticeAttachment::getNoticeId, noticeIds).orderByDesc(NoticeAttachment::getAttachmentId);
        List<NoticeAttachment> attachments = noticeAttachmentMapper.selectList(wrapper);
        if (attachments == null || attachments.isEmpty()) {
            return Collections.emptyMap();
        }
        List<NoticeAttachmentRes> list = new ArrayList<>(attachments.size());
        for (NoticeAttachment attachment : attachments) {
            NoticeAttachmentRes res = new NoticeAttachmentRes();
            BeanUtils.copyProperties(attachment, res);
            list.add(res);
        }
        return list.stream().collect(Collectors.groupingBy(NoticeAttachmentRes::getNoticeId));
    }

    private LocalDateTime parseDateStart(String dateText) {
        if (!StringUtils.hasText(dateText)) {
            return null;
        }
        return LocalDate.parse(dateText).atStartOfDay();
    }

    private LocalDateTime parseDateEnd(String dateText) {
        if (!StringUtils.hasText(dateText)) {
            return null;
        }
        return LocalDate.parse(dateText).atTime(LocalTime.MAX);
    }
}

package jh.exp.bid.contract.service.service.bus.Impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jh.exp.auth.clinet.api.bus.OrgUnitService;
import jh.exp.auth.clinet.api.bus.PersonService;
import jh.exp.auth.clinet.api.bus.PositionService;
import jh.exp.auth.core.entity.res.OrgUnitDetailRes;
import jh.exp.auth.core.entity.res.PersonDetailRes;
import jh.exp.auth.core.entity.res.PositionDetailRes;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 附件服务实现类
 */
@Service
@RequiredArgsConstructor
public class AttachmentServiceImpl implements AttachmentService {

    private final AttachmentMapper attachmentMapper;
    private final PersonService personService;
    private final OrgUnitService orgUnitService;
    private final PositionService positionService;
    private final StorageService storageService;

    @Override
    public SimplePageRes<AttachmentListRes> queryAttachmentList(SimplePageReq<QueryAttachmentReq> req) {
        Page<AttachmentListRes> page = new Page<>(req.getPageNum(), req.getPageSize());

        QueryAttachmentReq queryParam = req.getQueryParam();
        if (queryParam == null) {
            queryParam = new QueryAttachmentReq();
        }

        IPage<AttachmentListRes> result = attachmentMapper.selectAttachmentList(page, queryParam);
        fillAttachmentListNames(result.getRecords());

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
        fillAttachmentDetailNames(attachment);
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
            NamedByteArrayResource namedByteArrayResource = new NamedByteArrayResource(file.getBytes(), originalFileName);
            StorageUploadBizReq storageUploadBizReq = buildStorageBizReq(biz);
            storageRes = storageService.upload(namedByteArrayResource, storageUploadBizReq);
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
        LocalDateTime uploadTime = LocalDateTime.now();

        // 如果是新版本文件，先将旧版本标记为非最新
        if (StringUtils.hasText(req.getVersionNo())) {
            attachmentMapper.updateOldVersionsToNotLatest(req.getBusinessType(), req.getBusinessId(), req.getFileName());
        }

        int inserted = attachmentMapper.insertAttachment(req, personId, uploadTime);
        if (inserted <= 0) {
            throw new RuntimeException("保存附件失败");
        }
        Long attachmentId = attachmentMapper.selectLatestAttachmentId(
            req.getBusinessType(),
            req.getBusinessId(),
            req.getFileName(),
            personId
        );
        if (attachmentId == null) {
            throw new RuntimeException("附件保存成功但回查失败");
        }
        return getAttachmentById(attachmentId);
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
        AttachmentDetailRes existingAttachment = getAttachmentById(attachmentId);
        if (!StringUtils.hasText(req.getBusinessType())) {
            req.setBusinessType(existingAttachment.getBusinessType());
        }
        if (req.getBusinessId() == null) {
            req.setBusinessId(existingAttachment.getBusinessId());
        }

        // 标准表不支持 MD5 去重，按业务维度+文件名兜底校验
        if (checkFileExists(req.getFileName(), req.getFileMd5(), req.getBusinessType(), req.getBusinessId())) {
            if (!req.getFileName().equals(existingAttachment.getFileName())) {
                throw new RuntimeException("相同文件已存在");
            }
        }

        int updated = attachmentMapper.updateAttachment(attachmentId, req);
        if (updated <= 0) {
            throw new RuntimeException("更新附件失败");
        }
        return getAttachmentById(attachmentId);
    }

    @Override
    @Transactional
    public void deleteAttachment(Long attachmentId) {
        AttachmentDetailRes attachment = getAttachmentById(attachmentId);
        //检查存储的地方是否存在该文件



        if (StringUtils.hasText(attachment.getFilePath())) {
            Boolean flag = storageService.exist(attachment.getFilePath()).getData();
            if (flag) {
                StorageDeleteReq deleteReq = new StorageDeleteReq();
                deleteReq.setObjectKey(attachment.getFilePath());
                storageService.delete(deleteReq);
            }


        }

        // 标准表无逻辑删除状态字段，按ID物理删除
        attachmentMapper.deleteAttachmentById(attachmentId, attachment.getBusinessType());
    }

    @Override
    @Transactional
    public void batchDeleteAttachments(List<Long> attachmentIds) {
        if (attachmentIds == null || attachmentIds.isEmpty()) {
            return;
        }
        for (Long attachmentId : attachmentIds) {
            deleteAttachment(attachmentId);
        }
    }

    @Override
    @Transactional
    public AttachmentDetailRes updateFileStatus(Long attachmentId, String fileStatus) {
        // 标准表无文件状态字段，保留兼容返回
        return getAttachmentById(attachmentId);
    }

    @Override
    @Transactional
    public void batchUpdateFileStatus(List<Long> attachmentIds, String fileStatus) {
        // 标准表无文件状态字段，保留空实现
    }

    @Override
    public List<AttachmentListRes> getAttachmentsByBusiness(String businessType, Long businessId) {
        List<AttachmentListRes> list = attachmentMapper.selectAttachmentsByBusiness(businessType, businessId);
        fillAttachmentListNames(list);
        return list;
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

    /**
     * 姓名信息由服务层批量远程补齐，避免 XML 跨服务联查。
     */
    private void fillAttachmentListNames(List<AttachmentListRes> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        Set<Long> personIds = new HashSet<>();
        for (AttachmentListRes row : list) {
            if (row.getUploadUserId() != null) {
                personIds.add(row.getUploadUserId());
            }
            if (row.getCreatedBy() != null) {
                personIds.add(row.getCreatedBy());
            }
        }
        if (personIds.isEmpty()) {
            return;
        }
        Map<Long, PersonDetailRes> personMap = personService.batchGetPersonByIds(new ArrayList<>(personIds));
        if (personMap == null || personMap.isEmpty()) {
            return;
        }
        for (AttachmentListRes row : list) {
            PersonDetailRes uploadPerson = personMap.get(row.getUploadUserId());
            if (uploadPerson != null) {
                row.setUploadUserName(uploadPerson.getPersonName());
            }
            PersonDetailRes createdPerson = personMap.get(row.getCreatedBy());
            if (createdPerson != null) {
                row.setCreatedByName(createdPerson.getPersonName());
            }
        }
    }

    /**
     * 详情中的人员/组织/岗位名称由服务层远程补齐。
     */
    private void fillAttachmentDetailNames(AttachmentDetailRes detail) {
        if (detail == null) {
            return;
        }
        Set<Long> personIds = new HashSet<>();
        if (detail.getUploadUserId() != null) {
            personIds.add(detail.getUploadUserId());
        }
        if (detail.getCreatedBy() != null) {
            personIds.add(detail.getCreatedBy());
        }
        if (!personIds.isEmpty()) {
            Map<Long, PersonDetailRes> personMap = personService.batchGetPersonByIds(new ArrayList<>(personIds));
            if (personMap != null) {
                PersonDetailRes uploadPerson = personMap.get(detail.getUploadUserId());
                if (uploadPerson != null) {
                    detail.setUploadUserName(uploadPerson.getPersonName());
                }
                PersonDetailRes createdPerson = personMap.get(detail.getCreatedBy());
                if (createdPerson != null) {
                    detail.setCreatedByName(createdPerson.getPersonName());
                }
            }
        }
        if (detail.getCreatedDeptId() != null) {
            Map<Long, OrgUnitDetailRes> orgMap = orgUnitService.batchGetOrgUnitByIds(List.of(detail.getCreatedDeptId()));
            if (orgMap != null && orgMap.containsKey(detail.getCreatedDeptId())) {
                detail.setCreatedDeptName(orgMap.get(detail.getCreatedDeptId()).getOrgName());
            }
        }
        if (detail.getCreatedPostId() != null) {
            Map<Long, PositionDetailRes> postMap = positionService.batchGetPositionByIds(List.of(detail.getCreatedPostId()));
            if (postMap != null && postMap.containsKey(detail.getCreatedPostId())) {
                detail.setCreatedPostName(postMap.get(detail.getCreatedPostId()).getPostName());
            }
        }
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
package jh.exp.sys.servcie.dic.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.constant.CommonConstant;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.sys.core.api.dic.SysDictService;
import jh.exp.sys.core.entity.dic.SysDictItem;
import jh.exp.sys.core.entity.dic.SysDictType;
import jh.exp.sys.core.mapper.dic.SysDictMapper;
import jh.exp.sys.core.req.dic.BatchStatusReq;
import jh.exp.sys.core.req.dic.DictItemCreateReq;
import jh.exp.sys.core.req.dic.DictItemQueryReq;
import jh.exp.sys.core.req.dic.DictItemUpdateReq;
import jh.exp.sys.core.req.dic.DictTypeCreateReq;
import jh.exp.sys.core.req.dic.DictTypeDetailReq;
import jh.exp.sys.core.req.dic.DictTypeQueryReq;
import jh.exp.sys.core.req.dic.DictTypeUpdateReq;
import jh.exp.sys.core.req.dic.IdsReq;
import jh.exp.sys.core.req.dic.StatusReq;
import jh.exp.sys.core.resp.dic.DictOptionRes;
import jh.exp.sys.servcie.dic.service.SysDictApiService;
import jh.exp.sys.servcie.dic.service.SysDictTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SysDictApiServiceImpl implements SysDictApiService {
    private final SysDictService sysDictService;
    private final SysDictTypeService sysDictTypeService;
    private final SysDictMapper sysDictMapper;

    @Override
    public ApiResponse<SimplePageRes<SysDictType>> listDictType(SimplePageReq<DictTypeQueryReq> req) {
        DictTypeQueryReq queryParam = req.getQueryParam();
        Page<SysDictType> mpPage = new Page<>(req.getPageNum(), req.getPageSize());
        LambdaQueryWrapper<SysDictType> wrapper = Wrappers.lambdaQuery();
        if (queryParam != null && StringUtils.hasText(queryParam.getDictCode())) {
            wrapper.like(SysDictType::getDictCode, queryParam.getDictCode());
        }
        if (queryParam != null && StringUtils.hasText(queryParam.getDictName())) {
            wrapper.like(SysDictType::getDictName, queryParam.getDictName());
        }
        if (queryParam != null && StringUtils.hasText(queryParam.getStatus())) {
            wrapper.eq(SysDictType::getStatus, queryParam.getStatus());
        }
        Page<SysDictType> result = sysDictTypeService.page(mpPage, wrapper);
        SimplePageRes<SysDictType> pageResult = SimplePageRes.toPageRes(result, req);
        return ApiResponse.success(pageResult);
    }

    @Override
    public ApiResponse<SysDictType> getDictTypeDetail(DictTypeDetailReq req) {
        SysDictType dictType = null;
        if (req.getId() != null) {
            dictType = sysDictTypeService.getById(req.getId());
            if (dictType != null) {
                return ApiResponse.success(dictType);
            }
        }
        dictType = sysDictTypeService.getByDictCode(req.getDictCode());
        if (dictType == null) {
            throw new RuntimeException("字典类型不存在");
        }
        return ApiResponse.success(dictType);
    }

    @Override
    public ApiResponse<Map<String, Object>> createDictType(DictTypeCreateReq req) {
        SysDictType dictType = new SysDictType();
        dictType.setDictCode(req.getDictCode());
        dictType.setDictName(req.getDictName());
        dictType.setStatus(req.getStatus());
        dictType.setDescription(req.getDescription());
        sysDictTypeService.createDictType(dictType);
        return ApiResponse.success(Collections.emptyMap());
    }

    @Override
    public ApiResponse<Map<String, Object>> updateDictType(DictTypeUpdateReq req) {
        SysDictType dictType = new SysDictType();
        dictType.setId(req.getId());
        if (StringUtils.hasText(req.getDictCode())) {
            dictType.setDictCode(req.getDictCode());
        }
        if (StringUtils.hasText(req.getDictName())) {
            dictType.setDictName(req.getDictName());
        }
        if (StringUtils.hasText(req.getStatus())) {
            dictType.setStatus(req.getStatus());
        }
        if (req.getDescription() != null) {
            dictType.setDescription(req.getDescription());
        }
        sysDictTypeService.updateDictType(dictType);
        return ApiResponse.success(Collections.emptyMap());
    }

    @Override
    public ApiResponse<Map<String, Object>> deleteDictType(IdsReq req) {
        List<Long> ids = normalizeIds(req);
        List<SysDictType> types = sysDictTypeService.listByIds(ids);
        if (CollectionUtils.isEmpty(types)) {
            return ApiResponse.success(Collections.emptyMap());
        }
        for (SysDictType type : types) {
            // 删除前校验：该类型下存在字典项时禁止删除
            Long count = sysDictMapper.selectCount(Wrappers.lambdaQuery(SysDictItem.class)
                    .eq(SysDictItem::getDictCode, type.getDictCode()));
            if (count != null && count > 0) {
                throw new RuntimeException("字典类型下存在字典项，禁止删除：" + type.getDictCode());
            }
        }
        sysDictTypeService.removeByIds(ids);
        return ApiResponse.success(Collections.emptyMap());
    }

    @Override
    public ApiResponse<Map<String, Object>> updateDictTypeStatus(StatusReq req) {
        SysDictType update = new SysDictType();
        update.setId(req.getId());
        update.setStatus(req.getStatus());
        sysDictTypeService.updateById(update);
        return ApiResponse.success(Collections.emptyMap());
    }

    @Override
    public ApiResponse<Map<String, Object>> updateDictTypeStatusBatch(BatchStatusReq req) {
        sysDictTypeService.update(Wrappers.lambdaUpdate(SysDictType.class)
                .in(SysDictType::getId, req.getIds())
                .set(SysDictType::getStatus, req.getStatus()));
        return ApiResponse.success(Collections.emptyMap());
    }

    @Override
    public ApiResponse<SimplePageRes<SysDictItem>> listDictItem(SimplePageReq<DictItemQueryReq> req) {
        DictItemQueryReq query = req.getQueryParam();
        Page<SysDictItem> mpPage = new Page<>(req.getPageNum(), req.getPageSize());
        LambdaQueryWrapper<SysDictItem> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(SysDictItem::getDictCode, query.getDictCode());
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(SysDictItem::getStatus, query.getStatus());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(SysDictItem::getItemLabel, query.getKeyword())
                    .or()
                    .like(SysDictItem::getItemValue, query.getKeyword())
                    .or()
                    .like(SysDictItem::getItemCode, query.getKeyword()));
        }
        wrapper.orderByAsc(SysDictItem::getSortNo);
        Page<SysDictItem> result = sysDictMapper.selectPage(mpPage, wrapper);
        SimplePageRes<SysDictItem> pageResult = SimplePageRes.toPageRes(result, req);
        return ApiResponse.success(pageResult);
    }

    @Override
    public ApiResponse<SysDictItem> getDictItemDetail(Long id) {
        SysDictItem item = sysDictMapper.selectById(id);
        if (item == null) {
            throw new RuntimeException("字典项不存在");
        }
        return ApiResponse.success(item);
    }

    @Override
    public ApiResponse<Map<String, Object>> createDictItem(DictItemCreateReq req) {
        ensureDictTypeExists(req.getDictCode());
        checkItemCodeUnique(req.getDictCode(), req.getItemCode(), null);
        SysDictItem item = new SysDictItem();
        item.setDictCode(req.getDictCode());
        item.setItemCode(req.getItemCode());
        item.setItemValue(req.getItemValue());
        item.setItemLabel(req.getItemLabel());
        item.setSortNo(req.getSortNo());
        item.setStatus(req.getStatus());
        item.setRemark(req.getRemark());
        item.setCreatedTime(LocalDateTime.now());
        item.setUpdatedTime(LocalDateTime.now());
        sysDictService.createDictItem(item);
        return ApiResponse.success(Collections.emptyMap());
    }

    @Override
    public ApiResponse<Map<String, Object>> updateDictItem(DictItemUpdateReq req) {
        SysDictItem existing = sysDictMapper.selectById(req.getId());
        if (existing == null) {
            throw new RuntimeException("字典项不存在");
        }
        String dictCode = StringUtils.hasText(req.getDictCode()) ? req.getDictCode() : existing.getDictCode();
        if (StringUtils.hasText(req.getDictCode())) {
            ensureDictTypeExists(req.getDictCode());
        }
        checkItemCodeUnique(dictCode, req.getItemCode(), req.getId());
        SysDictItem item = new SysDictItem();
        item.setId(req.getId());
        if (StringUtils.hasText(req.getDictCode())) {
            item.setDictCode(req.getDictCode());
        }
        if (StringUtils.hasText(req.getItemCode())) {
            item.setItemCode(req.getItemCode());
        }
        if (StringUtils.hasText(req.getItemValue())) {
            item.setItemValue(req.getItemValue());
        }
        if (StringUtils.hasText(req.getItemLabel())) {
            item.setItemLabel(req.getItemLabel());
        }
        if (req.getSortNo() != null) {
            item.setSortNo(req.getSortNo());
        }
        if (StringUtils.hasText(req.getStatus())) {
            item.setStatus(req.getStatus());
        }
        if (req.getRemark() != null) {
            item.setRemark(req.getRemark());
        }
        item.setUpdatedTime(LocalDateTime.now());
        sysDictService.updateDictItem(item);
        return ApiResponse.success(Collections.emptyMap());
    }

    @Override
    public ApiResponse<Map<String, Object>> deleteDictItem(IdsReq req) {
        List<Long> ids = normalizeIds(req);
        for (Long id : ids) {
            sysDictService.deleteDictItem(id);
        }
        return ApiResponse.success(Collections.emptyMap());
    }

    @Override
    public ApiResponse<Map<String, Object>> updateDictItemStatus(StatusReq req) {
        SysDictItem update = new SysDictItem();
        update.setId(req.getId());
        update.setStatus(req.getStatus());
        sysDictMapper.updateById(update);
        return ApiResponse.success(Collections.emptyMap());
    }

    @Override
    public ApiResponse<Map<String, Object>> updateDictItemStatusBatch(BatchStatusReq req) {
        sysDictMapper.update(null, Wrappers.lambdaUpdate(SysDictItem.class)
                .in(SysDictItem::getId, req.getIds())
                .set(SysDictItem::getStatus, req.getStatus()));
        return ApiResponse.success(Collections.emptyMap());
    }

    @Override
    public ApiResponse<List<DictOptionRes>> listDictOptions(String dictCode) {
        List<SysDictItem> items = sysDictMapper.selectList(Wrappers.lambdaQuery(SysDictItem.class)
                .eq(SysDictItem::getDictCode, dictCode)
                .eq(SysDictItem::getStatus, CommonConstant.ENABLED_STATUS_STR)
                .orderByAsc(SysDictItem::getSortNo));
        List<DictOptionRes> options = new ArrayList<>();
        for (SysDictItem item : items) {
            options.add(new DictOptionRes(item.getItemValue(), item.getItemLabel()));
        }
        return ApiResponse.success(options);
    }

    @Override
    public ApiResponse<List<SysDictItem>> listAllDictItems(String dictCode) {
        List<SysDictItem> items = sysDictMapper.selectList(Wrappers.lambdaQuery(SysDictItem.class)
                .eq(SysDictItem::getDictCode, dictCode)
                .orderByAsc(SysDictItem::getSortNo));
        return ApiResponse.success(items);
    }

    private void ensureDictTypeExists(String dictCode) {
        if (sysDictTypeService.getByDictCode(dictCode) == null) {
            throw new RuntimeException("dictCode 不存在：" + dictCode);
        }
    }

    private void checkItemCodeUnique(String dictCode, String itemCode, Long excludeId) {
        if (!StringUtils.hasText(itemCode)) {
            return;
        }
        LambdaQueryWrapper<SysDictItem> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(SysDictItem::getDictCode, dictCode);
        wrapper.eq(SysDictItem::getItemCode, itemCode);
        if (excludeId != null) {
            wrapper.ne(SysDictItem::getId, excludeId);
        }
        Long count = sysDictMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new RuntimeException("字典项编码已存在：" + itemCode);
        }
    }

    private List<Long> normalizeIds(IdsReq req) {
        if (req == null) {
            return Collections.emptyList();
        }
        if (req.getId() != null) {
            return List.of(req.getId());
        }
        return req.getIds() == null ? Collections.emptyList() : req.getIds();
    }

    


}

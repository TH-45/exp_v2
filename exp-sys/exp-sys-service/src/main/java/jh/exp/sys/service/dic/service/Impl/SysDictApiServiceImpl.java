package jh.exp.sys.service.dic.service.Impl;

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
import jh.exp.sys.core.entity.dic.DictExportExcelRow;
import jh.exp.sys.core.req.dic.BatchStatusReq;
import jh.exp.sys.core.req.dic.DictExportHierarchyReq;
import jh.exp.sys.core.req.dic.DictItemCreateReq;
import jh.exp.sys.core.req.dic.DictItemImportRes;
import jh.exp.sys.core.req.dic.DictItemImportRow;
import jh.exp.sys.core.req.dic.DictItemQueryReq;
import jh.exp.sys.core.req.dic.DictItemUpdateReq;
import jh.exp.sys.core.req.dic.DictTypeCreateReq;
import jh.exp.sys.core.req.dic.DictTypeDetailReq;
import jh.exp.sys.core.req.dic.DictTypeQueryReq;
import jh.exp.sys.core.req.dic.DictTypeUpdateReq;
import jh.exp.sys.core.req.dic.IdsReq;
import jh.exp.sys.core.req.dic.StatusReq;
import jh.exp.sys.core.resp.dic.DictOptionRes;
import jh.exp.sys.service.dic.service.SysDictApiService;
import jh.exp.sys.service.dic.service.SysDictTypeService;
import lombok.RequiredArgsConstructor;
import jh.exp.sys.core.entity.dic.DictItemExcelRow;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.excel.EasyExcel;

import jh.exp.sys.service.dic.strategy.DictTypeMergeStrategy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

    @Override
    public ApiResponse<List<SysDictItem>> listAllDictItemsForExport() {
        List<SysDictItem> items = sysDictMapper.selectList(
                Wrappers.lambdaQuery(SysDictItem.class).orderByAsc(SysDictItem::getDictCode, SysDictItem::getSortNo));
        return ApiResponse.success(items);
    }

    @Override
    public ApiResponse<DictItemImportRes> importDictItems(List<DictItemImportRow> rows) {
        DictItemImportRes res = new DictItemImportRes(0, 0, new ArrayList<>());
        if (CollectionUtils.isEmpty(rows)) {
            return ApiResponse.success(res);
        }
        for (int i = 0; i < rows.size(); i++) {
            DictItemImportRow row = rows.get(i);
            try {
                saveOrUpdateDictItem(row);
                res.setSuccessCount(res.getSuccessCount() + 1);
            } catch (Exception e) {
                res.setFailCount(res.getFailCount() + 1);
                res.getErrors().add(String.format("第%d行: %s", i + 1, e.getMessage()));
            }
        }
        return ApiResponse.success(res);
    }

    @Override
    public ApiResponse<DictItemImportRes> importDictItemsFromExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("请选择要导入的 Excel 文件");
        }
        String name = file.getOriginalFilename();
        if (name == null || !name.toLowerCase().endsWith(".xlsx")) {
            throw new RuntimeException("仅支持 .xlsx 格式的 Excel 文件");
        }
        List<DictItemImportRow> rows = new ArrayList<>();
        try (InputStream is = file.getInputStream()) {
            List<DictItemExcelRow> excelRows = EasyExcel.read(is).head(DictItemExcelRow.class).sheet().doReadSync();
            for (DictItemExcelRow er : excelRows) {
                if (isEmpty(er)) continue;
                DictItemImportRow row = new DictItemImportRow();
                row.setDictCode(er.getDictCode() != null ? er.getDictCode().trim() : "");
                row.setItemCode(er.getItemCode() != null ? er.getItemCode().trim() : null);
                row.setItemValue(er.getItemValue() != null ? er.getItemValue().trim() : "");
                row.setItemLabel(er.getItemLabel() != null ? er.getItemLabel().trim() : "");
                row.setSortNo(er.getSortNo());
                row.setStatus(StringUtils.hasText(er.getStatus()) ? er.getStatus().trim() : CommonConstant.ENABLED_STATUS_STR);
                row.setRemark(er.getRemark() != null ? er.getRemark().trim() : null);
                rows.add(row);
            }
        } catch (IOException e) {
            throw new RuntimeException("读取 Excel 文件失败: " + e.getMessage());
        }
        return importDictItems(rows);
    }

    @Override
    public ApiResponse<DictItemImportRes> importDictItemsFromHierarchy(List<DictExportHierarchyReq> hierarchy) {
        if (CollectionUtils.isEmpty(hierarchy)) {
            return ApiResponse.success(new DictItemImportRes(0, 0, new ArrayList<>()));
        }
        List<DictItemImportRow> rows = new ArrayList<>();
        for (DictExportHierarchyReq req : hierarchy) {
            if (req.getDictType() == null || CollectionUtils.isEmpty(req.getItems())) continue;
            SysDictType t = req.getDictType();
            String dictCode = StringUtils.hasText(t.getDictCode()) ? t.getDictCode().trim() : null;
            if (!StringUtils.hasText(dictCode)) continue;
            // 若字典类型不存在则自动创建
            ensureDictTypeExistsOrCreate(t);
            for (SysDictItem item : req.getItems()) {
                DictItemImportRow row = new DictItemImportRow();
                row.setDictCode(dictCode);
                row.setItemCode(item.getItemCode() != null ? item.getItemCode().trim() : null);
                row.setItemValue(item.getItemValue() != null ? item.getItemValue().trim() : "");
                row.setItemLabel(item.getItemLabel() != null ? item.getItemLabel().trim() : "");
                row.setSortNo(item.getSortNo());
                row.setStatus(StringUtils.hasText(item.getStatus()) ? item.getStatus() : CommonConstant.ENABLED_STATUS_STR);
                row.setRemark(item.getRemark());
                rows.add(row);
            }
        }
        return importDictItems(rows);
    }

    /** 若字典类型不存在则创建 */
    private void ensureDictTypeExistsOrCreate(SysDictType dictType) {
        if (sysDictTypeService.getByDictCode(dictType.getDictCode()) != null) {
            return;
        }
        DictTypeCreateReq createReq = new DictTypeCreateReq();
        createReq.setDictCode(dictType.getDictCode());
        createReq.setDictName(StringUtils.hasText(dictType.getDictName()) ? dictType.getDictName() : dictType.getDictCode());
        createReq.setStatus(StringUtils.hasText(dictType.getStatus()) ? dictType.getStatus() : CommonConstant.ENABLED_STATUS_STR);
        createReq.setDescription(dictType.getDescription());
        createDictType(createReq);
    }

    /** 按 dictCode + itemCode 判断：存在则更新，不存在则新增 */
    private void saveOrUpdateDictItem(DictItemImportRow row) {
        ensureDictTypeExists(row.getDictCode());
        String itemCode = StringUtils.hasText(row.getItemCode()) ? row.getItemCode() : row.getItemValue();
        // 先按 dictCode + itemCode 匹配；若 itemCode 为空则按 dictCode + itemValue 匹配
        SysDictItem existing = sysDictMapper.selectOne(Wrappers.lambdaQuery(SysDictItem.class)
                .eq(SysDictItem::getDictCode, row.getDictCode())
                .eq(StringUtils.hasText(itemCode), SysDictItem::getItemCode, itemCode)
                .eq(!StringUtils.hasText(itemCode), SysDictItem::getItemValue, row.getItemValue()));
        if (existing != null) {
            DictItemUpdateReq req = new DictItemUpdateReq();
            req.setId(existing.getId());
            req.setDictCode(row.getDictCode());
            req.setItemCode(itemCode);
            req.setItemValue(row.getItemValue());
            req.setItemLabel(row.getItemLabel());
            req.setSortNo(row.getSortNo() != null ? row.getSortNo() : existing.getSortNo());
            req.setStatus(StringUtils.hasText(row.getStatus()) ? row.getStatus() : existing.getStatus());
            req.setRemark(row.getRemark());
            updateDictItem(req);
        } else {
            DictItemCreateReq req = new DictItemCreateReq();
            req.setDictCode(row.getDictCode());
            req.setItemCode(itemCode);
            req.setItemValue(row.getItemValue());
            req.setItemLabel(row.getItemLabel());
            req.setSortNo(row.getSortNo() != null ? row.getSortNo() : 0);
            req.setStatus(StringUtils.hasText(row.getStatus()) ? row.getStatus() : CommonConstant.ENABLED_STATUS_STR);
            req.setRemark(row.getRemark());
            createDictItem(req);
        }
    }

    private boolean isEmpty(DictItemExcelRow er) {
        return !StringUtils.hasText(er.getDictCode()) && !StringUtils.hasText(er.getItemValue()) && !StringUtils.hasText(er.getItemLabel());
    }

    @Override
    public byte[] exportDictItemsToExcel(List<SysDictItem> items) {
        List<SysDictItem> list = items != null && !items.isEmpty()
                ? items
                : (listAllDictItemsForExport().getData() != null ? listAllDictItemsForExport().getData() : Collections.emptyList());
        List<DictItemExcelRow> rows = new ArrayList<>();
        for (SysDictItem item : list) {
            DictItemExcelRow row = new DictItemExcelRow();
            row.setDictCode(item.getDictCode());
            row.setItemCode(item.getItemCode());
            row.setItemValue(item.getItemValue());
            row.setItemLabel(item.getItemLabel());
            row.setSortNo(item.getSortNo());
            row.setStatus(item.getStatus());
            row.setRemark(item.getRemark());
            rows.add(row);
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        EasyExcel.write(os, DictItemExcelRow.class).sheet("字典项").doWrite(rows);
        return os.toByteArray();
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

    @Override
    public byte[] exportDictHierarchyToExcel(List<DictExportHierarchyReq> hierarchy) {
        if (CollectionUtils.isEmpty(hierarchy)) {
            return exportDictItemsToExcel(null);
        }
        List<DictExportExcelRow> rows = new ArrayList<>();
        for (DictExportHierarchyReq req : hierarchy) {
            if (req.getDictType() == null || CollectionUtils.isEmpty(req.getItems())) continue;
            SysDictType t = req.getDictType();
            for (SysDictItem item : req.getItems()) {
                DictExportExcelRow row = new DictExportExcelRow();
                row.setDictCode(t.getDictCode());
                row.setDictName(t.getDictName());
                row.setTypeDescription(t.getDescription());
                row.setTypeStatus(t.getStatus());
                row.setItemCode(item.getItemCode());
                row.setItemValue(item.getItemValue());
                row.setItemLabel(item.getItemLabel());
                row.setSortNo(item.getSortNo());
                row.setItemStatus(item.getStatus());
                row.setRemark(item.getRemark());
                rows.add(row);
            }
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try (var writer = EasyExcel.write(os, DictExportExcelRow.class)
                .registerWriteHandler(new DictTypeMergeStrategy())
                .build()) {
            var sheet1 = EasyExcel.writerSheet(0, "字典数据").build();
            writer.write(rows, sheet1);
            List<List<String>> descRows = List.of(
                    List.of("【字段说明】"),
                    List.of("字典类型编码", "全局唯一，如 USER_STATUS"),
                    List.of("字典类型名称", "类型显示名称"),
                    List.of("类型描述", "可选描述"),
                    List.of("类型状态", "ENABLED-启用，DISABLED-停用"),
                    List.of("字典项编码", "字典项编码"),
                    List.of("字典项值", "业务表存储值"),
                    List.of("字典项名称", "显示名称"),
                    List.of("排序", "排序号"),
                    List.of("项状态", "ENABLED-启用，DISABLED-停用"),
                    List.of("备注", "可选备注")
            );
            var sheet2 = EasyExcel.writerSheet(1, "字段说明").build();
            writer.write(descRows, sheet2);
        }
        return os.toByteArray();
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

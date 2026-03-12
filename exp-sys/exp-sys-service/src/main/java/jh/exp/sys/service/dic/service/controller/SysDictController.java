package jh.exp.sys.service.dic.service.controller;

import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.sys.core.entity.dic.SysDictItem;
import jh.exp.sys.core.entity.dic.SysDictType;
import jh.exp.sys.core.req.dic.BatchStatusReq;
import jh.exp.sys.core.req.dic.DictItemCreateReq;
import jh.exp.sys.core.req.dic.DictExportHierarchyReq;
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
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jh.exp.sys.service.dic.service.SysDictApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * 系统字典控制器
 * 提供字典类型和字典项的增删改查功能
 */
@RestController
@RequestMapping("/dict")
@RequiredArgsConstructor
@Validated
public class SysDictController {

    private final SysDictApiService sysDictApiService;

    /**
     * 分页查询字典类型列表
     *
     * @param req 查询请求对象，包含分页参数和查询条件
     * @return 包含分页结果的响应对象
     */
    @PostMapping("/type/list")
    public ApiResponse<SimplePageRes<SysDictType>> listDictType(@Valid @RequestBody SimplePageReq<DictTypeQueryReq> req) {
        req.pageDefault();
        return sysDictApiService.listDictType(req);
    }

    /**
     * 获取字典类型详情
     *
     * @param req 查询请求对象，包含字典类型ID等信息
     * @return 字典类型的详细信息响应对象
     */
    @GetMapping("/type/detail")
    public ApiResponse<SysDictType> dictTypeDetail(@Valid DictTypeDetailReq req) {
        return sysDictApiService.getDictTypeDetail(req);
    }

    /**
     * 创建字典类型
     *
     * @param req 创建请求对象，包含字典类型的基本信息
     * @return 操作结果响应对象
     */
    @PostMapping("/type/create")
    public ApiResponse<Map<String, Object>> createDictType(@Valid @RequestBody DictTypeCreateReq req) {
        return sysDictApiService.createDictType(req);
    }

    /**
     * 更新字典类型
     *
     * @param req 更新请求对象，包含字典类型的更新信息
     * @return 操作结果响应对象
     */
    @PostMapping("/type/update")
    public ApiResponse<Map<String, Object>> updateDictType(@Valid @RequestBody DictTypeUpdateReq req) {
        return sysDictApiService.updateDictType(req);
    }

    /**
     * 删除字典类型
     *
     * @param req 删除请求对象，包含要删除的字典类型ID列表
     * @return 操作结果响应对象
     */
    @PostMapping("/type/delete")
    public ApiResponse<Map<String, Object>> deleteDictType(@Valid @RequestBody IdsReq req) {
        return sysDictApiService.deleteDictType(req);
    }

    /**
     * 更新字典类型状态
     *
     * @param req 状态更新请求对象，包含字典类型ID和新状态
     * @return 操作结果响应对象
     */
    @PostMapping("/type/status")
    public ApiResponse<Map<String, Object>> updateDictTypeStatus(@Valid @RequestBody StatusReq req) {
        return sysDictApiService.updateDictTypeStatus(req);
    }

    /**
     * 批量更新字典类型状态
     *
     * @param req 批量状态更新请求对象，包含多个字典类型ID和新状态
     * @return 操作结果响应对象
     */
    @PostMapping("/type/status/batch")
    public ApiResponse<Map<String, Object>> updateDictTypeStatusBatch(@Valid @RequestBody BatchStatusReq req) {
        return sysDictApiService.updateDictTypeStatusBatch(req);
    }

    /**
     * 分页查询字典项列表
     *
     * @param req 查询请求对象，包含分页参数和查询条件
     * @return 包含分页结果的响应对象
     */
    @PostMapping("/item/list")
    public ApiResponse<SimplePageRes<SysDictItem>> listDictItem(@Valid @RequestBody SimplePageReq<DictItemQueryReq> req) {
        req.pageDefault();
        // 验证查询参数不能为空
        if (req.getQueryParam() == null) {
            throw new RuntimeException("queryParam 不能为空");
        }
        return sysDictApiService.listDictItem(req);
    }

    /**
     * 获取字典项详情
     *
     * @param id 字典项ID
     * @return 字典项的详细信息响应对象
     */
    @GetMapping("/item/detail")
    public ApiResponse<SysDictItem> dictItemDetail(@NotNull(message = "id 不能为空") @RequestParam("id") Long id) {
        return sysDictApiService.getDictItemDetail(id);
    }

    /**
     * 创建字典项
     *
     * @param req 创建请求对象，包含字典项的基本信息
     * @return 操作结果响应对象
     */
    @PostMapping("/item/create")
    public ApiResponse<Map<String, Object>> createDictItem(@Valid @RequestBody DictItemCreateReq req) {
        return sysDictApiService.createDictItem(req);
    }

    /**
     * 更新字典项
     *
     * @param req 更新请求对象，包含字典项的更新信息
     * @return 操作结果响应对象
     */
    @PostMapping("/item/update")
    public ApiResponse<Map<String, Object>> updateDictItem(@Valid @RequestBody DictItemUpdateReq req) {
        return sysDictApiService.updateDictItem(req);
    }

    /**
     * 删除字典项
     *
     * @param req 删除请求对象，包含要删除的字典项ID列表
     * @return 操作结果响应对象
     */
    @PostMapping("/item/delete")
    public ApiResponse<Map<String, Object>> deleteDictItem(@Valid @RequestBody IdsReq req) {
        return sysDictApiService.deleteDictItem(req);
    }

    /**
     * 更新字典项状态
     *
     * @param req 状态更新请求对象，包含字典项ID和新状态
     * @return 操作结果响应对象
     */
    @PostMapping("/item/status")
    public ApiResponse<Map<String, Object>> updateDictItemStatus(@Valid @RequestBody StatusReq req) {
        return sysDictApiService.updateDictItemStatus(req);
    }

    /**
     * 批量更新字典项状态
     *
     * @param req 批量状态更新请求对象，包含多个字典项ID和新状态
     * @return 操作结果响应对象
     */
    @PostMapping("/item/status/batch")
    public ApiResponse<Map<String, Object>> updateDictItemStatusBatch(@Valid @RequestBody BatchStatusReq req) {
        return sysDictApiService.updateDictItemStatusBatch(req);
    }

    /**
     * 获取字典选项列表
     *
     * @param dictCode 字典编码
     * @return 字典选项列表响应对象
     */
    @GetMapping("/item/options")
    public ApiResponse<List<DictOptionRes>> listDictOptions(@NotBlank(message = "dictCode 不能为空") @RequestParam("dictCode") String dictCode) {
        return sysDictApiService.listDictOptions(dictCode);
    }

    /**
     * 获取所有字典项列表
     *
     * @param dictCode 字典编码
     * @return 所有字典项列表响应对象
     */
    @GetMapping("/item/all")
    public ApiResponse<List<SysDictItem>> listAllDictItems(@NotBlank(message = "dictCode 不能为空") @RequestParam("dictCode") String dictCode) {
        return sysDictApiService.listAllDictItems(dictCode);
    }

    /**
     * 获取全部字典项（用于 JSON 导出）
     */
    @GetMapping("/item/export/all")
    public ApiResponse<List<SysDictItem>> listAllDictItemsForExport() {
        return sysDictApiService.listAllDictItemsForExport();
    }

    /**
     * 导出字典项为 Excel 文件（GET 导出全部，POST 传入选中项导出指定项）
     */
    @GetMapping("/item/export/excel")
    public ResponseEntity<byte[]> exportDictItemsExcelGet() {
        return buildExcelResponse(sysDictApiService.exportDictItemsToExcel(null));
    }

    @PostMapping("/item/export/excel")
    public ResponseEntity<byte[]> exportDictItemsExcelPost(@RequestBody(required = false) List<SysDictItem> items) {
        return buildExcelResponse(sysDictApiService.exportDictItemsToExcel(items));
    }

    /**
     * 导出字典父子结构为 Excel（类型合并行 + 字段说明 sheet）
     */
    @PostMapping("/item/export/excel/hierarchy")
    public ResponseEntity<byte[]> exportDictHierarchyExcel(@RequestBody List<DictExportHierarchyReq> hierarchy) {
        byte[] bytes = sysDictApiService.exportDictHierarchyToExcel(hierarchy);
        String fileName = "字典项_" + System.currentTimeMillis() + ".xlsx";
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    private ResponseEntity<byte[]> buildExcelResponse(byte[] bytes) {
        String fileName = "字典项_" + System.currentTimeMillis() + ".xlsx";
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    /**
     * 批量导入字典项（JSON）
     */
    @PostMapping("/item/import/json")
    public ApiResponse<DictItemImportRes> importDictItemsJson(@Valid @RequestBody List<DictItemImportRow> rows) {
        return sysDictApiService.importDictItems(rows);
    }

    /**
     * 批量导入字典项（Excel）
     */
    @PostMapping("/item/import/excel")
    public ApiResponse<DictItemImportRes> importDictItemsExcel(@RequestParam("file") MultipartFile file) {
        return sysDictApiService.importDictItemsFromExcel(file);
    }
}

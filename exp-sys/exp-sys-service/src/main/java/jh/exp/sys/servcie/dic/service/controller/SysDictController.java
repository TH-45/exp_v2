package jh.exp.sys.servcie.dic.service.controller;

import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.sys.core.entity.dic.SysDictItem;
import jh.exp.sys.core.entity.dic.SysDictType;
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
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jh.exp.sys.servcie.dic.service.SysDictApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dict")
@RequiredArgsConstructor
@Validated
public class SysDictController {

    private final SysDictApiService sysDictApiService;

    @PostMapping("/type/list")
    public ApiResponse<SimplePageRes<SysDictType>> listDictType(@Valid @RequestBody SimplePageReq<DictTypeQueryReq> req) {
        req.pageDefault();
        return sysDictApiService.listDictType(req);
    }

    @GetMapping("/type/detail")
    public ApiResponse<SysDictType> dictTypeDetail(@Valid DictTypeDetailReq req) {
        return sysDictApiService.getDictTypeDetail(req);
    }

    @PostMapping("/type/create")
    public ApiResponse<Map<String, Object>> createDictType(@Valid @RequestBody DictTypeCreateReq req) {
        return sysDictApiService.createDictType(req);
    }

    @PostMapping("/type/update")
    public ApiResponse<Map<String, Object>> updateDictType(@Valid @RequestBody DictTypeUpdateReq req) {
        return sysDictApiService.updateDictType(req);
    }

    @PostMapping("/type/delete")
    public ApiResponse<Map<String, Object>> deleteDictType(@Valid @RequestBody IdsReq req) {
        return sysDictApiService.deleteDictType(req);
    }

    @PostMapping("/type/status")
    public ApiResponse<Map<String, Object>> updateDictTypeStatus(@Valid @RequestBody StatusReq req) {
        return sysDictApiService.updateDictTypeStatus(req);
    }

    @PostMapping("/type/status/batch")
    public ApiResponse<Map<String, Object>> updateDictTypeStatusBatch(@Valid @RequestBody BatchStatusReq req) {
        return sysDictApiService.updateDictTypeStatusBatch(req);
    }

    @PostMapping("/item/list")
    public ApiResponse<SimplePageRes<SysDictItem>> listDictItem(@Valid @RequestBody SimplePageReq<DictItemQueryReq> req) {
        req.pageDefault();
        if (req.getQueryParam() == null) {
            throw new RuntimeException("queryParam 不能为空");
        }
        return sysDictApiService.listDictItem(req);
    }

    @GetMapping("/item/detail")
    public ApiResponse<SysDictItem> dictItemDetail(@NotNull(message = "id 不能为空") @RequestParam("id") Long id) {
        return sysDictApiService.getDictItemDetail(id);
    }

    @PostMapping("/item/create")
    public ApiResponse<Map<String, Object>> createDictItem(@Valid @RequestBody DictItemCreateReq req) {
        return sysDictApiService.createDictItem(req);
    }

    @PostMapping("/item/update")
    public ApiResponse<Map<String, Object>> updateDictItem(@Valid @RequestBody DictItemUpdateReq req) {
        return sysDictApiService.updateDictItem(req);
    }

    @PostMapping("/item/delete")
    public ApiResponse<Map<String, Object>> deleteDictItem(@Valid @RequestBody IdsReq req) {
        return sysDictApiService.deleteDictItem(req);
    }

    @PostMapping("/item/status")
    public ApiResponse<Map<String, Object>> updateDictItemStatus(@Valid @RequestBody StatusReq req) {
        return sysDictApiService.updateDictItemStatus(req);
    }

    @PostMapping("/item/status/batch")
    public ApiResponse<Map<String, Object>> updateDictItemStatusBatch(@Valid @RequestBody BatchStatusReq req) {
        return sysDictApiService.updateDictItemStatusBatch(req);
    }

    @GetMapping("/item/options")
    public ApiResponse<List<DictOptionRes>> listDictOptions(@NotBlank(message = "dictCode 不能为空") @RequestParam("dictCode") String dictCode) {
        return sysDictApiService.listDictOptions(dictCode);
    }

    @GetMapping("/item/all")
    public ApiResponse<List<SysDictItem>> listAllDictItems(@NotBlank(message = "dictCode 不能为空") @RequestParam("dictCode") String dictCode) {
        return sysDictApiService.listAllDictItems(dictCode);
    }
}

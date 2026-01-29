package jh.exp.sys.servcie.dic.service;

import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.api.PageResult;
import jh.exp.sys.core.entity.dic.SysDictItem;
import jh.exp.sys.core.entity.dic.SysDictType;
import jh.exp.sys.core.req.dic.BatchStatusReq;
import jh.exp.sys.core.req.dic.DictItemCreateReq;
import jh.exp.sys.core.req.dic.DictItemListReq;
import jh.exp.sys.core.req.dic.DictItemUpdateReq;
import jh.exp.sys.core.req.dic.DictTypeCreateReq;
import jh.exp.sys.core.req.dic.DictTypeDetailReq;
import jh.exp.sys.core.req.dic.DictTypeListReq;
import jh.exp.sys.core.req.dic.DictTypeUpdateReq;
import jh.exp.sys.core.req.dic.IdsReq;
import jh.exp.sys.core.req.dic.StatusReq;
import jh.exp.sys.core.resp.dic.DictOptionRes;

import java.util.List;
import java.util.Map;

public interface SysDictApiService {
    ApiResponse<PageResult<SysDictType>> listDictType(DictTypeListReq req);

    ApiResponse<SysDictType> getDictTypeDetail(DictTypeDetailReq req);

    ApiResponse<Map<String, Object>> createDictType(DictTypeCreateReq req);

    ApiResponse<Map<String, Object>> updateDictType(DictTypeUpdateReq req);

    ApiResponse<Map<String, Object>> deleteDictType(IdsReq req);

    ApiResponse<Map<String, Object>> updateDictTypeStatus(StatusReq req);

    ApiResponse<Map<String, Object>> updateDictTypeStatusBatch(BatchStatusReq req);

    ApiResponse<PageResult<SysDictItem>> listDictItem(DictItemListReq req);

    ApiResponse<SysDictItem> getDictItemDetail(Long id);

    ApiResponse<Map<String, Object>> createDictItem(DictItemCreateReq req);

    ApiResponse<Map<String, Object>> updateDictItem(DictItemUpdateReq req);

    ApiResponse<Map<String, Object>> deleteDictItem(IdsReq req);

    ApiResponse<Map<String, Object>> updateDictItemStatus(StatusReq req);

    ApiResponse<Map<String, Object>> updateDictItemStatusBatch(BatchStatusReq req);

    ApiResponse<List<DictOptionRes>> listDictOptions(String dictCode);

    ApiResponse<List<SysDictItem>> listAllDictItems(String dictCode);
}

package jh.exp.sys.servcie.dic.service;

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

import java.util.List;
import java.util.Map;

public interface SysDictApiService {
    ApiResponse<SimplePageRes<SysDictType>> listDictType(SimplePageReq<DictTypeQueryReq> req);

    ApiResponse<SysDictType> getDictTypeDetail(DictTypeDetailReq req);

    ApiResponse<Map<String, Object>> createDictType(DictTypeCreateReq req);

    ApiResponse<Map<String, Object>> updateDictType(DictTypeUpdateReq req);

    ApiResponse<Map<String, Object>> deleteDictType(IdsReq req);

    ApiResponse<Map<String, Object>> updateDictTypeStatus(StatusReq req);

    ApiResponse<Map<String, Object>> updateDictTypeStatusBatch(BatchStatusReq req);

    ApiResponse<SimplePageRes<SysDictItem>> listDictItem(SimplePageReq<DictItemQueryReq> req);

    ApiResponse<SysDictItem> getDictItemDetail(Long id);

    ApiResponse<Map<String, Object>> createDictItem(DictItemCreateReq req);

    ApiResponse<Map<String, Object>> updateDictItem(DictItemUpdateReq req);

    ApiResponse<Map<String, Object>> deleteDictItem(IdsReq req);

    ApiResponse<Map<String, Object>> updateDictItemStatus(StatusReq req);

    ApiResponse<Map<String, Object>> updateDictItemStatusBatch(BatchStatusReq req);

    ApiResponse<List<DictOptionRes>> listDictOptions(String dictCode);

    ApiResponse<List<SysDictItem>> listAllDictItems(String dictCode);
}

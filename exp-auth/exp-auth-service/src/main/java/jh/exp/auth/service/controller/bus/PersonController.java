package jh.exp.auth.service.controller.bus;




import jh.exp.auth.core.entity.dto.OrgIdAndPersonIdDTO;
import jh.exp.auth.core.entity.req.*;
import jh.exp.auth.core.entity.res.PersonDetailRes;
import jh.exp.auth.core.entity.res.PersonInfoRes;
import jh.exp.auth.service.service.bus.PersonService;
import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/person")
@RequiredArgsConstructor
public class PersonController {

    private final PersonService personService;

    /**
     * 分页查询人员列表
     */
    @PostMapping("/list")
    public ApiResponse<SimplePageRes<PersonInfoRes>> list(@RequestBody SimplePageReq<QueryPersonReq> req) {
        req.pageDefault();
        SimplePageRes<PersonInfoRes> result = personService.queryPersonInfo(req);
        return ApiResponse.success(result);
    }

    /**
     * 批量查询组织的部门负责人/人员信息（供内部服务调用，返回原始数据不包 ApiResponse）
     * 对比传入 personId 与组织的 managerPersonId：一致返回部门负责人信息，不一致返回传入 id 的人员信息
     *
     * @param orgIdAndPersonIds 组织ID与人员ID对列表
     * @return 组织ID -> 人员详情（每个 orgId 对应一条人员详情）
     */
    @PostMapping("/list/project-manager")
    public Map<Long, PersonDetailRes> queryProjectManager(
            @RequestBody List<OrgIdAndPersonIdDTO> orgIdAndPersonIds) {
        return personService.queryProjectManager(orgIdAndPersonIds);
    }

    /**
     * 根据ID查询人员详情
     */
    @GetMapping("/detail")
    public ApiResponse<PersonDetailRes> detail(@RequestParam Long personId) {
        PersonDetailRes result = personService.getPersonById(personId);
        return ApiResponse.success(result);
    }

    /**
     * 批量查询人员详情
     * @param personIds
     * @return
     */
    @PostMapping("/batch")
    public Map<Long, PersonDetailRes> batchGetPersonByIds(@RequestBody List<Long> personIds) {
        return personService.batchGetPersonByIds(personIds);
    }

    /**
     * 批量查询人员详情（按标识+人员id）
     * 入参：标识和人员id list，返回：标识 -> 人员详情 map
     *
     * @param personFlagReqList 标识与人员id列表
     * @return 标识(Long) -> 人员详情
     */
    @PostMapping("/batch/flag")
    public Map<Long, PersonDetailRes> batchFlagPersonByIds(@RequestBody List<PersonFlagReq> personFlagReqList) {
        return personService.batchFlagPersonByIds(personFlagReqList);
    }

    /**
     * 创建人员
     */
    @PostMapping("/create")
    public ApiResponse<PersonDetailRes> create(@RequestBody @Valid CreatePersonReq req) {
        PersonDetailRes result = personService.createPerson(req);
        return ApiResponse.success(result);
    }

    /**
     * 更新人员
     */
    @PostMapping("/update")
    public ApiResponse<PersonDetailRes> update(@RequestBody @Valid UpdatePersonReq req) {
        PersonDetailRes result = personService.updatePerson(req);
        return ApiResponse.success(result);
    }

    /**
     * 删除人员
     */
    @PostMapping("/delete")
    public ApiResponse<Void> delete(@RequestBody DeletePersonReq req) {
        personService.deletePerson(req.getPersonId());
        return ApiResponse.success(null);
    }

    /**
     * 批量删除人员
     */
    @PostMapping("/batchDelete")
    public ApiResponse<Void> batchDelete(@RequestBody @Valid BatchDeletePersonReq req) {
        personService.batchDeletePersons(req);
        return ApiResponse.success(null);
    }

    /**
     * 更改人员状态
     */
    @PostMapping("/status")
    public ApiResponse<PersonDetailRes> updateStatus(@RequestBody @Valid PersonStatusReq req) {
        PersonDetailRes result = personService.updatePersonStatus(req);
        return ApiResponse.success(result);
    }

    /**
     * 批量更改人员状态
     */
    @PostMapping("/batchStatus")
    public ApiResponse<Void> batchUpdateStatus(@RequestBody @Valid BatchPersonStatusReq req) {
        personService.batchUpdatePersonStatus(req);
        return ApiResponse.success(null);
    }

    /**
     * 检查人员工号是否存在
     */
    @GetMapping("/checkPersonCode")
    public ApiResponse<Boolean> checkPersonCode(@RequestParam String personCode,
                                                @RequestParam(required = false) Long excludePersonId) {
        boolean exists = personService.checkPersonCodeExists(personCode, excludePersonId);
        return ApiResponse.success(exists);
    }
}

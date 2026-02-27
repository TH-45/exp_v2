package jh.exp.auth.service.controller.bus;




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
import org.springframework.web.service.annotation.PostExchange;

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
    @PostExchange("/batch")
    Map<Long , PersonDetailRes> batchGetPersonByIds(@RequestBody List<Long> personIds){

        return personService.batchGetPersonByIds(personIds);
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

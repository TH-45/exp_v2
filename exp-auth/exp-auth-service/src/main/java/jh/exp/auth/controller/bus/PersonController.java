package jh.exp.auth.controller.bus;

import cn.hutool.core.lang.Assert;
import jakarta.validation.constraints.NotNull;
import jh.exp.auth.entity.req.PersonExpReq;
import jh.exp.auth.entity.req.QueryPersonReq;
import jh.exp.auth.entity.res.PersonInfoRes;
import jh.exp.auth.service.bus.PersonService;
import jh.exp.common.api.ApiResponse;
import jh.exp.common.req.SimplePageReq;
import jh.exp.common.res.SimplePageRes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/person")
public class PersonController {
    private static final Logger log = LoggerFactory.getLogger(PersonController.class);

    @Autowired
    private PersonService personService;

    @PostMapping("/queryPersonInfo")
    public SimplePageRes<PersonInfoRes> queryPersonInfo(@RequestBody SimplePageReq<QueryPersonReq> personReq) {

        //page参数校验
        personReq.pageDefault();
        try{
            return personService.queryPersonInfo(personReq);
        }catch (Exception e){
            log.error("查询用户信息异常!", e);
            throw new RuntimeException(e.getMessage());
        }

    }

    /**
     * 修改用户状态
     * @param
     * @return
     */
    @PostMapping("/enabledPerson")
    public ApiResponse<Object> enabledPerson(@RequestBody @NotNull PersonExpReq personExpReq) {
        Long personId = personExpReq.getPersonId();
        String status = personExpReq.getStatus();
        Assert.notNull(personId,"用户ID不能为空");
        Assert.isTrue(personId > 0,"用户ID必须大于0");
        Assert.notEmpty(status,"状态不能为空");
        try {
            personService.updatePersonStatus(personId,status);
            return ApiResponse.success(null);
        }catch (Exception e){
            log.error("启用用户异常!", e);
            return ApiResponse.fail(null, e.getMessage());
        }
    }
    /**
     * 修改用户信息
     */
    @PostMapping("/updatePersonInfo")
    public ApiResponse<Object> updatePersonInfo(@RequestBody @NotNull PersonExpReq personExpReq) {
        Assert.notNull(personExpReq.getPersonId(),"用户id不能为空");
        try {
            personService.updatePersonInfo(personExpReq);
            return ApiResponse.success(null);
        }catch (Exception e){
            log.error("修改用户信息异常!", e);
            return ApiResponse.fail(null, e.getMessage());
        }
    }
}

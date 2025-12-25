package jh.exp.auth.controller.bus;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jh.exp.auth.entity.Position;

import jh.exp.auth.entity.req.QueryPositionReq;
import jh.exp.auth.service.bus.PositionService;
import jh.exp.common.annotation.RequiresPermissions;
import jh.exp.common.auth.CurrentUserHolder;
import jh.exp.common.auth.dto.CurrentUser;

import jh.exp.common.req.SimplePageReq;
import jh.exp.common.res.SimplePageRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/position",produces = "application/json;charset=UTF-8")
public class PositionController {

    @Autowired
    private PositionService positionService;

    /**
     * 分页查询岗位信息
     */
    @PostMapping(value = "/queryList")
    @RequiresPermissions( value = {"position:query"} )
    public SimplePageRes<Position> queryList(@RequestBody @Valid SimplePageReq<QueryPositionReq> positionReq) {
        CurrentUser currentUser = CurrentUserHolder.get();
        //page参数校验
        positionReq.pageDefault();
        try{
            return positionService.queryPosition(positionReq);
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }

    }





}

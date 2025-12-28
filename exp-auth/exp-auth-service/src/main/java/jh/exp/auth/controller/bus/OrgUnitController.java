package jh.exp.auth.controller.bus;

import jh.exp.auth.entity.OrgUnit;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/orgunit")
public class OrgUnitController {

    /**
     * 查询部门信息
     */
    public ApiResult<List<OrgUnit>> queryOrgUnit() {

}

package jh.exp.auth.controller.bus;

import jh.exp.auth.entity.res.MenusRes;
import jh.exp.auth.service.bus.MenuService;
import jh.exp.common.api.ApiResponse;
import jh.exp.common.auth.CurrentUserHolder;
import jh.exp.common.auth.dto.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/roles")
public class RolesController {

    @Autowired
    private MenuService menuService;

    //角色绑定菜单
    @GetMapping("/menus")
    public ApiResponse<MenusRes> getMenus() {
        CurrentUser currentUser = CurrentUserHolder.get();
        ApiResponse<MenusRes> apiResponse = new ApiResponse<>();
        try{
            MenusRes menusRes = menuService.getMenus(currentUser);
            apiResponse.setSuccess(true);
            apiResponse.setData(menusRes);
        }catch (Exception e){
            apiResponse.setSuccess(false);
            apiResponse.setMessage(e.getMessage());
        }
        return apiResponse;
    }

}

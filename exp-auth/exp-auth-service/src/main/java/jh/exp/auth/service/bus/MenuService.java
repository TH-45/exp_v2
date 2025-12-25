package jh.exp.auth.service.bus;

import jh.exp.auth.entity.res.MenusRes;
import jh.exp.common.auth.dto.CurrentUser;
import org.springframework.stereotype.Service;

@Service
public interface MenuService {

    MenusRes getMenus(CurrentUser currentUser);
}

package jh.exp.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jh.exp.auth.entity.ExpMenu;
import jh.exp.auth.entity.res.MenuDetailRes;
import jh.exp.auth.entity.res.MenuListRes;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MenuMapper extends BaseMapper<ExpMenu> {

    /**
     * 分页查询菜单列表
     */
    IPage<MenuListRes> selectMenuList(IPage<MenuListRes> page,
                                     @Param("menuCode") String menuCode,
                                     @Param("menuName") String menuName,
                                     @Param("menuType") String menuType,
                                     @Param("status") String status);

    /**
     * 根据菜单ID查询菜单详情信息（多表联查）
     */
    MenuDetailRes selectMenuDetailById(@Param("menuId") Long menuId);

    /**
     * 检查菜单编码是否存在
     */
    int countByMenuCode(@Param("menuCode") String menuCode, @Param("excludeMenuId") Long excludeMenuId);

    /**
     * 根据菜单ID列表批量查询菜单详情
     */
    List<MenuDetailRes> selectMenuDetailByIds(@Param("menuIds") List<Long> menuIds);

}

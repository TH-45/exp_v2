package jh.exp.auth.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jh.exp.auth.core.entity.Position;

import jh.exp.auth.core.entity.res.PositionListRes;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

@Mapper
public interface PositionMapper extends BaseMapper<Position> {

    // 不分页
    List<PositionListRes> selectPositionListByOrg(@Param("orgId") Long orgId, @Param("status") String status
    );

    // 分页（MyBatis-Plus 自动识别）
    IPage<PositionListRes> selectPositionPageByOrg(Page<?> page, @Param("orgId") Long orgId, @Param("status") String status);



}

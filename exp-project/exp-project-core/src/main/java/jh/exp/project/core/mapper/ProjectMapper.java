package jh.exp.project.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import jh.exp.project.core.entity.Project;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProjectMapper extends BaseMapper<Project> {
}

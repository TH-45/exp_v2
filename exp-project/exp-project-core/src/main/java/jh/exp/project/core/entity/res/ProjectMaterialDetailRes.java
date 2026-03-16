package jh.exp.project.core.entity.res;

import lombok.Data;

import java.util.List;

@Data
public class ProjectMaterialDetailRes {
    private List<ProjectMaterialRes> materials;
    private Integer total;
    private Integer lowStock;
    private Integer outOfStock;
}

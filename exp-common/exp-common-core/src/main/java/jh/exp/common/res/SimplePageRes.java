package jh.exp.common.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public  class SimplePageRes<T> {
    private Long total;
    private Long page;
    private Long size;
    private List<T> list;

}

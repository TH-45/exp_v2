package jh.exp.common.core.imex;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImexTaskErrorItem {
    private Integer rowNo;
    private String errorType;
    private String message;
}

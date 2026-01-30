package jh.exp.common.core.req;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimplePageReq<T> {
     private int pageNum;
     private int pageSize;
     /**
      * 排序字段
      */
     private String sort;
     @Valid
     private T queryParam;


     //倒序
     private static final String DESC=" DESC";
     //默认值校验
     public void pageDefault(){
          if(this.pageNum<=0){
               this.pageNum = 1;
          }
          if(this.pageSize<=0){
               this.pageSize = 10;
          }
          if (this.sort == null){
               this.sort = DESC;
          }

     }
}

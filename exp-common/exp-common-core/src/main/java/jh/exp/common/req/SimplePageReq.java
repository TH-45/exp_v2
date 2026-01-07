package jh.exp.common.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
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
     private T queryParam;


     //倒序
     private static final String DESC=" DESC";
     //创建时间时间倒序
     private static final String CREATE_TIME_DESC=" CREATE_TIME DESC";
     // 正序
     private static final String ASC=" ASC";
     //创建时间时间正序
     private static final String CREATE_TIME_ASC=" CREATE_TIME ASC";

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

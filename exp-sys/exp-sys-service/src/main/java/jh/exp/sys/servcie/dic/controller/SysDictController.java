package jh.exp.sys.servcie.dic.controller;


import jh.exp.sys.api.dic.SysDictService;
import jh.exp.sys.entity.dic.SysDictItem;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/Sys/Dic")
@RequiredArgsConstructor
public class SysDictController {

    private final SysDictService sysDicService;

    /**
     * 根据字典编码获取字典项
     *
     */
    public SysDictItem getDictItems(String dictCode) {
        return sysDicService.getDicItem(dictCode);
    }
    /**
     * 根据字典类型获取字典列表
     */

}

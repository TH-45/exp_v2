package jh.exp.sys.servcie.dic.service.controller;


import jh.exp.sys.core.api.dic.SysDictService;
import jh.exp.sys.core.entity.dic.SysDictItem;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

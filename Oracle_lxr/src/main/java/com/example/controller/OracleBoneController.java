package com.example.controller;

import com.example.entity.CharTable;
import com.example.common.Result;
import com.example.common.enums.ResultCodeEnum;
import com.example.service.CharTableService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/oracle-bone")

public class OracleBoneController {
    
    
    private static final Logger log = LoggerFactory.getLogger(OracleBoneController.class);
    private final CharTableService charTableService;
    
    public OracleBoneController(CharTableService charTableService) {
        this.charTableService = charTableService;
    }
    
    /**
     * 传递一个简体汉字，返回对应的甲骨文图片链接
     */
    @GetMapping("/query")
    public Result getOracleBone(@RequestParam String character) {
        if (character == null || character.length() != 1) {
            return Result.error(ResultCodeEnum.PARAM_ERROR);
        }
        try {
            String url = charTableService.selectByChar(character).getOBC();
            if (url == null)
                return Result.error(ResultCodeEnum.CHAR_TABLE_NOT_FOUND);
            return Result.success(url);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.error(ResultCodeEnum.CHAR_TABLE_NOT_FOUND);
        }
    }
}

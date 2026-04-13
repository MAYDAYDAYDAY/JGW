package com.example.controller;

import cn.hutool.core.util.ObjectUtil;
import com.example.common.Result;
import com.example.entity.PicHistory;
import com.example.service.PicHistoryService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/pic")
public class PicHistoryController {
    @Resource
    private PicHistoryService picHistoryService;

    @PostMapping("/addHistory")
    public Result addPicHistory(@RequestBody PicHistory picture){
        System.out.println(picture.toString());
        if(ObjectUtil.isNull(picture)){
            return Result.error();
        }
        int result = picHistoryService.addPicHistory(picture);
        if(result == 1){
            return Result.success();
        }else{
            return Result.error();
        }

    }

    @GetMapping("/getHistory")
    public Result getPicHistory(@RequestParam Integer id){
        System.out.println("id:"+id);
        if(ObjectUtil.isNull(id)){
            return Result.error();
        }
        return Result.success(picHistoryService.getPicHistoryById(id));
    }
}

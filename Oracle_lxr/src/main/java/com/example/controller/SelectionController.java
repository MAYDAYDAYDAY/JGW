package com.example.controller;

import com.example.common.Result;
import com.example.entity.Selection;
import com.example.service.SelectionService;
import com.example.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/selection")
public class SelectionController {

    @Resource
    private SelectionService selectionService;
    @Resource
    private UserService userService;

    @GetMapping("/get")
    public Result getSelection() {
        Selection selection = selectionService.getSelection();
        return Result.success(selection);
    }

    @PutMapping("/incScores")
    public Result incScores( Integer id) {

        userService.incScores(id);
        return Result.success();
    }
}

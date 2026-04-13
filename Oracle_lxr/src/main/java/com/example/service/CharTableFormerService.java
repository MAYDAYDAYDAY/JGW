package com.example.service;

import com.example.common.enums.ResultCodeEnum;
import com.example.entity.CharTable;
import com.example.exception.CustomException;
import com.example.mapper.CharTableFormerMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class CharTableFormerService {

    private final String OSS_URL = "https://jgwlxr.obs.cn-south-1.myhuaweicloud.com/";

    @Resource
    CharTableFormerMapper charTableFormerMapper;

   public CharTable selectByID(Integer id) {
        List<CharTable> charTableList = charTableFormerMapper.selectByID(id);
        if (charTableList.isEmpty()) {
            throw new CustomException(ResultCodeEnum.CHAR_TABLE_NOT_FOUND);
        }
        CharTable charTable = charTableList.get(0);
        if(charTable.getOBC() == null){
            System.out.println("TRUE");
//            charTable.setCharUrl("https://jgw.obs.cn-north-4.myhuaweicloud.com/"+id);
        }
//        System.out.println("charTable  CharUrl:"+charTable.getCharUrl());
        return charTable;
    }

    public List<CharTable> selectRandomList() {
        return charTableFormerMapper.selectRandomList();
    }

        /*
    通过汉字查询对应的甲骨文
     */


}

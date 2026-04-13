package com.example.service;

import com.example.common.enums.ResultCodeEnum;
import com.example.entity.CharTable;
import com.example.exception.CustomException;
import com.example.mapper.ChartTableMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
@Service
public class CharTableService {

    @Resource
    private ChartTableMapper chartTableMapper;


    public CharTable selectByID(Integer id) {
        List<CharTable> charTableList = chartTableMapper.selectByID(id);
        if (charTableList.isEmpty()) {
            throw new CustomException(ResultCodeEnum.CHAR_TABLE_NOT_FOUND);
        }
        CharTable charTable = charTableList.get(0);
        if(charTable.getOBC() == null){
            System.out.println("TRUE");
        }
        return charTable;
    }

    public List<CharTable> selectRandomList() {
        return chartTableMapper.selectRandomList();
    }
    public CharTable selectByChar(String character) throws Exception {
        List<CharTable> charTableList = chartTableMapper.selectByChar(character);
        if (charTableList.isEmpty()) {
            throw new Exception("没有找到对应的甲骨文");
        }
        return charTableList.get(0);

    }

}

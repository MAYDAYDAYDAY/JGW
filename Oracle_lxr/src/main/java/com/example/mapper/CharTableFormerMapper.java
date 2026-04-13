package com.example.mapper;

import com.example.entity.CharTable;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface CharTableFormerMapper {

    @Select("select * from chartable1 where id = #{id}")
    List<CharTable>selectByID(Integer id);

    List<CharTable>selectRandomList();
    
    // 通过汉字查询对应的甲骨文
    @Select("select * from chartable1 where sWord = #{character}")
    List<CharTable> selectByChar(String character);
    
}

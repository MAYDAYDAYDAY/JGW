package com.example.mapper;

import com.example.entity.CharTable;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ChartTableMapper {
    @Select("select * from chartable where id = #{id}")
    List<CharTable> selectByID(Integer id);

    @Select("SELECT * FROM chartable" +
            "        WHERE sWord REGEXP '^[^0-9]+$'" +
            "        ORDER BY RAND()" +
            "            LIMIT 4")
    List<CharTable>selectRandomList();
    // 通过汉字查询对应的甲骨文
    @Select("select * from chartable where sWord = #{character}")
    List<CharTable> selectByChar(String character);
}

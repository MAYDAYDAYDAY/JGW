package com.example.mapper;

import com.example.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface UserMapper {

    @Select("select * from user where username = #{username}")
    User selectByUsername(String username);

    @Select("select * from user where id = #{userId}")
    User selectById(Integer userId);

    int updateById(User user);


    int insert(User user);
}

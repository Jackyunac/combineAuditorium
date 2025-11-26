package com.combine.auditorium.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.combine.auditorium.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}


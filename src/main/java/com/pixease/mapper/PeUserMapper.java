package com.pixease.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pixease.entity.PeUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper接口，继承MyBatis-Plus BaseMapper
 */
@Mapper
public interface PeUserMapper extends BaseMapper<PeUser> {
}

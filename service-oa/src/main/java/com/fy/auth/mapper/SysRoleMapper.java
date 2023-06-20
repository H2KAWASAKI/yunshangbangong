package com.fy.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fy.model.system.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {
}

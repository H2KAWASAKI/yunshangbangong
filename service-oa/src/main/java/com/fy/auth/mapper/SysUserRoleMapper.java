package com.fy.auth.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fy.model.system.SysUserRole;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * <p>
 * 用户角色 Mapper 接口
 * </p>
 *
 * @author fy
 * @since 2023-06-02
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

}

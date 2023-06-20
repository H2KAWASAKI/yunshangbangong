package com.fy.auth.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fy.model.system.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * <p>
 * 用户表 Mapper 接口
 * </p>
 *
 * @author fy
 * @since 2023-06-02
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

}

package com.fy.auth.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fy.model.system.SysRoleMenu;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * <p>
 * 角色菜单 Mapper 接口
 * </p>
 *
 * @author fy
 * @since 2023-06-03
 */
@Mapper
public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenu> {

}

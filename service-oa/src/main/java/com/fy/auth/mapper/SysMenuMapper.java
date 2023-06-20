package com.fy.auth.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fy.model.system.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 菜单表 Mapper 接口
 * </p>
 *
 * @author fy
 * @since 2023-06-03
 */
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    //多表联查
    List<SysMenu> findMenuListByUserId(@Param("userId") Long userId);
}

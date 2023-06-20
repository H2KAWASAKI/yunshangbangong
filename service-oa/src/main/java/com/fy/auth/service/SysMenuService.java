package com.fy.auth.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.fy.model.system.SysMenu;
import com.fy.vo.system.AssignMenuVo;
import com.fy.vo.system.RouterVo;

import java.util.List;

/**
 * <p>
 * 菜单表 服务类
 * </p>
 *
 * @author fy
 * @since 2023-06-03
 */
public interface SysMenuService extends IService<SysMenu> {
    public List<SysMenu> findNodes();
    /**
     * 根据角色获取授权权限数据
     * @return
     */
    List<SysMenu> findSysMenuByRoleId(Long roleId);

    /**
     * 保存角色权限
     * @param  assignMenuVo
     * 角色分配菜单
     */
    void doAssign(AssignMenuVo assignMenuVo);

    //4 根据用户id获取用户可以操作的菜单列表（要查询数据库，动态构建路由结构）
    List<RouterVo> findUserMenuListByUserId(Long userId);

    //5根据用户id获取用户可以操作的菜单列表
    List<String> findUserPermsByUserId(Long userId);
}

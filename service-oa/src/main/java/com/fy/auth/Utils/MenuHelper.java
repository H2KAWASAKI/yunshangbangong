package com.fy.auth.Utils;

import com.fy.model.system.SysMenu;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 根据菜单数据构建菜单数据
 * </p>
 *
 */
public class MenuHelper {

    public static List<SysMenu> buildTree(List<SysMenu> sysMenuList){
        List<SysMenu> tree = new ArrayList<>();
        for(SysMenu sysMenu: sysMenuList){
            if(sysMenu.getParentId()==0){
                tree.add(findChildren(sysMenu,sysMenuList));
            }
        }
        return tree;
    }

    private static SysMenu findChildren(SysMenu sysMenu, List<SysMenu> treeNodes) {
        sysMenu.setChildren(new ArrayList<SysMenu>());
        for(SysMenu it:treeNodes){
            if(sysMenu.getId()==it.getParentId()){
                if (sysMenu.getChildren() == null) {
                    sysMenu.setChildren(new ArrayList<>());
                }
                sysMenu.getChildren().add(findChildren(it,treeNodes));
            }
        }
        return sysMenu;
    }
}
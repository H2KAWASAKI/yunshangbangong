package com.fy.wechat.controller;


import com.fy.common.result.Result;
import com.fy.vo.wechat.MenuVo;
import com.fy.wechat.service.MenuService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 菜单 前端控制器
 * </p>
 *
 * @author atguigu
 * @since 2023-02-16
 */
@RestController
@RequestMapping("/admin/wechat/menu")
@CrossOrigin
public class MenuController {

    @Autowired
    private MenuService menuService;


    //@PreAuthorize("hasAuthority('bnt.menu.syncMenu')")
    @ApiOperation(value = "同步菜单")
    @GetMapping("syncMenu")
    public Result createMenu() {
        menuService.syncMenu();
        return Result.ok();
    }
    

    @ApiOperation(value = "获取全部菜单")
    @GetMapping("findMenuInfo")
    public Result findMenuInfo() {
        List<MenuVo> menuList = menuService.findMenuInfo();
        return Result.ok(menuList);
    }

    /*@PreAuthorize("hasAuthority('bnt.menu.removeMenu')")*/
    @ApiOperation(value = "删除菜单")
    @DeleteMapping("removeMenu")
    public Result removeMenu() {
        menuService.removeMenu();
        return Result.ok();
    }

}


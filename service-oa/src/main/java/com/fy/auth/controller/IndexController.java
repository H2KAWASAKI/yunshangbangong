package com.fy.auth.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fy.auth.service.SysMenuService;
import com.fy.auth.service.SysUserService;
import com.fy.common.execption.FyException;
import com.fy.common.jwt.JwtHelper;
import com.fy.common.result.Result;
import com.fy.common.utils.MD5;
import com.fy.model.system.SysUser;
import com.fy.vo.system.LoginVo;
import com.fy.vo.system.RouterVo;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台登录登出
 */
@Api(tags = "后台登录管理")
@RestController
@RequestMapping("/admin/system/index")
public class IndexController {

    @Autowired
    SysUserService sysUserService;
    @Autowired
    SysMenuService sysMenuService;
    /**
     * 登录
     * @return
     */
    @PostMapping("login")
    public Result login(@RequestBody LoginVo loginVo) {
        /*Map<String, Object> map = new HashMap<>();
        map.put("token","admin");
        return Result.ok(map);*/
        //1 获取输入用户名和密码
        String username = loginVo.getUsername();
        //2 根据用户名查询数据库
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        SysUser sysUser = sysUserService.getOne(wrapper);
        //3 用户信息是否存在
        if(sysUser == null){
            throw new FyException(201, "用户不存在");
        }
        //4 判断密码
            //数据库中的密码
        String passwordMD5 = sysUser.getPassword();
            //获取的密码
        String password = loginVo.getPassword();
        String encrypt = MD5.encrypt(password);
        if(!encrypt.equals(passwordMD5)){
            throw new FyException(201, "密码错误");
        }
        //5 判断用户是否被禁止 1可用 0禁用
        if(sysUser.getStatus() == 0){
            throw new FyException(201, "用户被禁用");
        }
        //6 使用jwt根据用户id和用户名称生成token字符串
        String token =
                JwtHelper.createToken(sysUser.getId(), sysUser.getUsername());
        //7 返回
        Map<String,Object> map = new HashMap<>();
        map.put("token",token);
        return Result.ok(map);
    }
    /**
     * 获取用户信息
     * @return
     */
    @GetMapping("info")
    public Result info(HttpServletRequest request) {

        /*map.put("roles","[admin]");
        map.put("name","admin");
        map.put("avatar","https://oss.aliyuncs.com/aliyun_id_photo_bucket/default_handsome.jpg");*/
        //1 从请求头里获取用户信息（获取请求头token字符串）
        String token = request.getHeader("token");
        //2 从token字符串中回去用户id或用户名
        //Long userId = 2L;//
        Long userId = JwtHelper.getUserId(token);
        //3 根据用户id查询数据库吗，获取用户信息
        SysUser sysUser = sysUserService.getById(userId);
        //4 根据用户id获取用户可以操作的菜单列表（要查询数据库，动态构建路由结构）
        List<RouterVo> routerList = sysMenuService.findUserMenuListByUserId(userId);
        //5根据用户id获取用户可以操作的菜单列表
        List<String > permsList = sysMenuService.findUserPermsByUserId(userId);
        //6返回响应数据
        Map<String, Object> map = new HashMap<>();
        map.put("roles","[admin]");
        map.put("name",sysUser.getName());
        map.put("avatar","https://oss.aliyuncs.com/aliyun_id_photo_bucket/default_handsome.jpg");

        //返回用户可以操作菜单
        map.put("routers",routerList);
        //返回用户可以操作按钮
        map.put("buttons", permsList);
        return Result.ok(map);
    }
    /**
     * 退出
     * @return
     */
    @PostMapping("logout")
    public Result logout(){
        return Result.ok();
    }

}
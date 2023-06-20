package com.fy.auth.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fy.auth.mapper.SysUserRoleMapper;
import com.fy.auth.service.SysRoleService;
import com.fy.common.execption.FyException;
import com.fy.common.result.Result;
import com.fy.model.system.SysRole;
import com.fy.model.system.SysUserRole;
import com.fy.vo.system.AssignRoleVo;
import com.fy.vo.system.SysRoleQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Api(tags = "角色管理接口")
@RestController
@RequestMapping("/admin/system/sysRole")
public class SysRoleController {

    @Autowired
    private SysRoleService sysRoleService;

  /*  @GetMapping("/findAll")
    public List<SysRole> findAll() {
        List<SysRole> roleList = sysRoleService.list();
        return roleList;
    }*/
    @GetMapping("findAll")
    @ApiOperation("查询所有角色")
    public Result findAll() {
       List<SysRole> list = sysRoleService.list();

       return Result.ok(list);
    }
    @ApiOperation("条件分页查询")
    @GetMapping("{page}/{limit}")
      public Result pageQueryRole(@PathVariable Long page,
                                  @PathVariable Long limit,
                                  SysRoleQueryVo sysRoleQueryVo){
        //调用service的方法
        //1。创建Page对象
        Page<SysRole> pageParam = new Page<>(page,limit);
        //2.封装条件，判断条件是否为空
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        String roleName = sysRoleQueryVo.getRoleName();
        if(!StringUtils.isEmpty(roleName)){
            wrapper.like(SysRole::getRoleName, roleName);
        }

        IPage<SysRole> rolePage = sysRoleService.page(pageParam, wrapper);
        return Result.ok(rolePage);
    }
    /*@ApiOperation("添加角色")
    @GetMapping("save")
    public Result save(SysRole role){
        //调用service方法
        boolean is_success = sysRoleService.save(role);
        if(is_success){
            return Result.ok();
        }
        return Result.fail();
    }*/
    @ApiOperation("添加角色")
    @PostMapping("save")
    public Result save(@RequestBody SysRole role){
        //调用service方法
        boolean is_success = sysRoleService.save(role);
        if(is_success){
            return Result.ok();
        }
        return Result.fail();
    }
    //修改角色-根据id查询
    @ApiOperation(value = "根据id查询")
    @GetMapping("get/{id}")
    public Result get(@PathVariable Long id) {
        SysRole sysRole = sysRoleService.getById(id);
        return Result.ok(sysRole);
    }
    //修改角色-最终实现
    @ApiOperation(value = "修改角色")
    @PutMapping("update")
    public Result updateById(@RequestBody SysRole role) {
        boolean is_success = sysRoleService.updateById(role);
        if(is_success){
            return Result.ok();
        }
        return Result.fail();
    }
    //根据id删除
    @ApiOperation(value = "根据id删除角色")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id) {
        boolean is_success = sysRoleService.removeById(id);
        if(is_success){
            return Result.ok();
        }
        return Result.fail();
    }
    //批量删除
    @ApiOperation(value = "根据id列表删除")
    @DeleteMapping("batchRemove")
    public Result batchRemove(@RequestBody List<Long> idList) {
        boolean is_success = sysRoleService.removeByIds(idList);
        if(is_success){
            return Result.ok();
        }
        return Result.fail();
    }

    @ApiOperation(value = "根据用户获取角色数据")
    @GetMapping("/toAssign/{userId}")
    public Result toAssign(@PathVariable Long userId) {
        Map<String, Object> roleMap = sysRoleService.findRoleByUserId(userId);
        return Result.ok(roleMap);
    }

    @ApiOperation(value = "根据用户分配角色")
    @PostMapping("/doAssign")
    public Result doAssign(@RequestBody AssignRoleVo assignRoleVo) {
        sysRoleService.doAssign(assignRoleVo);
        return Result.ok();
    }

}
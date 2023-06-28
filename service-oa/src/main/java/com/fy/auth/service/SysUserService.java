package com.fy.auth.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.fy.model.system.SysUser;

import java.util.Map;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author fy
 * @since 2023-06-02
 */
public interface SysUserService extends IService<SysUser> {

    void updateStatus(Long id, Integer status);

    //根据用户名查询
    SysUser getUserByUserName(String username);

    Map<String, Object> getCurrentUser();
}

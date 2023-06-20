package com.fy.auth;

import com.fy.auth.mapper.SysRoleMapper;
import com.fy.model.system.SysRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class TestDemo1 {
    @Autowired
    private SysRoleMapper mapper;
    @Test
    public void test1(){
        List<SysRole> sysRoles = mapper.selectList(null);
        sysRoles.forEach(System.out::println);
    }

    @Test
    public void addTest(){
        SysRole sysRole = new SysRole();
        sysRole.setRoleName("角色管理员");
        sysRole.setRoleCode("role");
        sysRole.setDescription("角色管理员");
        int rows = mapper.insert(sysRole);
        System.out.println(rows);
        System.out.println(sysRole);
    }
}

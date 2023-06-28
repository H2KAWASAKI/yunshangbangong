package com.fy.security.filter;

import com.alibaba.fastjson.JSON;
import com.fy.common.jwt.JwtHelper;
import com.fy.common.result.ResponseUtil;
import com.fy.common.result.Result;
import com.fy.common.result.ResultCodeEnum;
import com.fy.security.custom.LoginUserInfoHelper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TokenAuthenticationFilter extends OncePerRequestFilter {
    private RedisTemplate redisTemplate;
    public TokenAuthenticationFilter(RedisTemplate redisTemplate){
        this.redisTemplate = redisTemplate;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        //如果是登录接口，直接放行
        if("/admin/system/index/login".equals(request.getRequestURI())) {
            chain.doFilter(request, response);
            return;
        }

        UsernamePasswordAuthenticationToken authentication = getAuthentication(request);
        if(null != authentication) {
            SecurityContextHolder.getContext().setAuthentication(authentication);
            chain.doFilter(request, response);
        } else {
            ResponseUtil.out(response, Result.build(null, ResultCodeEnum.LOGIN_ERROR));
        }
    }

    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
        //请求头是否有token
        String token = request.getHeader("token");
        if(StringUtils.hasLength(token)){
            String username = JwtHelper.getUsername(token);
            if(StringUtils.hasLength(username)){
                //将当前用户信息放到ThreadLocal里面
                LoginUserInfoHelper.setUserId(JwtHelper.getUserId(token));
                LoginUserInfoHelper.setUsername(JwtHelper.getUsername(token));
                //通过username获取redis中的权限数据
                String authString = (String) redisTemplate.opsForValue().get(username);
                //将权限数据转换为我们想要的List<SimpleGrantedAuthority>
                if(StringUtils.hasLength(authString)){
                    List<Map> maplist = JSON.parseArray(authString, Map.class);
                    System.out.println(maplist);
                    List<SimpleGrantedAuthority> authList = new ArrayList<>();
                    for(Map map:maplist){
                        String authority = (String) map.get("authority");
                        authList.add(new SimpleGrantedAuthority(authority));

                    }
                    return new UsernamePasswordAuthenticationToken(username, null,authList);
                }else{
                    return new UsernamePasswordAuthenticationToken(username, null,new ArrayList<>());
                }
            }
        }
        return null;
    }
}

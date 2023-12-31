package com.fy.wechat.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fy.auth.service.SysUserService;
import com.fy.common.jwt.JwtHelper;
import com.fy.common.result.Result;
import com.fy.model.system.SysUser;
import com.fy.vo.wechat.BindPhoneVo;
import io.swagger.annotations.ApiOperation;
import lombok.Lombok;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;


@Controller
@RequestMapping("/admin/wechat")
@CrossOrigin
public class WechatController {
    @Resource
    private SysUserService sysUserService;

    @Autowired
    private WxMpService wxMpService;

    @Value("${wechat.userInfoUrl}")
    private String userInfoUrl;

    @GetMapping("/authorize")
    public String authorize(@RequestParam("returnUrl") String returnUrl, HttpServletRequest request){
        //第一个参数: 授权路径，在那个路径获取微信信息
        //第二个参数 :固定值，授权类型 WxConsts.OAuth2Scope.SNSAPI_USERINFO
        //第三个参数: 授权成功之后，跳转路径，将‘guiguoa’转换成‘#’
        String redirectURL = wxMpService.getOAuth2Service().buildAuthorizationUrl(userInfoUrl,
                WxConsts.OAuth2Scope.SNSAPI_USERINFO,
                URLEncoder.encode(returnUrl.replace("guiguoa", "#")));
        return "redirect:" + redirectURL;
    }
    @GetMapping("/userInfo")
    public String userInfo(@RequestParam("code") String code,
                           @RequestParam("state") String returnUrl) throws Exception{
        //获取accessToken
        WxOAuth2AccessToken accessToken = wxMpService.getOAuth2Service().getAccessToken(code);
        //使用accessToken获取openId
        String openId = accessToken.getOpenId();
        System.out.println("openId:"+openId);
        WxOAuth2UserInfo wxMpUser = wxMpService.getOAuth2Service().getUserInfo(accessToken, null);
        System.out.println("微信用户信息"+JSON.toJSONString(wxMpUser));
        //根据openId查询用户表
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper();
        wrapper.eq(SysUser::getOpenId, openId);
        SysUser sysUser = sysUserService.getOne(wrapper);
        String token = "";
        //判断openId是否存在
        if(sysUser!=null){
            token= JwtHelper.createToken(sysUser.getId(), sysUser.getUsername());
        }
        if(returnUrl.indexOf("?") == -1) {
            return "redirect:" + returnUrl + "?token=" + token + "&openId=" + openId;
        } else {
            return "redirect:" + returnUrl + "&token=" + token + "&openId=" + openId;
        }
    }
    @ApiOperation(value = "微信账号绑定手机")
    @PostMapping("bindPhone")
    @ResponseBody
    public Result bindPhone(@RequestBody BindPhoneVo bindPhoneVo){
        // 根据手机号去查询数据库
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper();
        wrapper.eq(SysUser::getPhone,bindPhoneVo.getPhone() );
        SysUser sysUser = sysUserService.getOne(wrapper);
        // 如果存在，更新记录openId
        if(sysUser!=null){
            sysUser.setOpenId(bindPhoneVo.getOpenId());
            sysUserService.updateById(sysUser);

            String token = JwtHelper.createToken(sysUser.getId(), sysUser.getUsername());
            return Result.ok(token);
        }else{//如果不存在，通知管理员
            return Result.fail("手机号不存在，请联系管理员修改");
        }
    }
}

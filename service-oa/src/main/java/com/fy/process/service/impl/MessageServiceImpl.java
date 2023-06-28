package com.fy.process.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fy.auth.service.SysUserService;
import com.fy.model.process.Process;
import com.fy.model.process.ProcessTemplate;
import com.fy.model.system.SysUser;
import com.fy.process.service.MessageService;
import com.fy.process.service.OaProcessService;
import com.fy.process.service.OaProcessTemplateService;
import com.fy.security.custom.LoginUserInfoHelper;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import org.activiti.engine.TaskService;
import org.checkerframework.checker.units.qual.A;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private OaProcessService processService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private OaProcessTemplateService processTemplateService;
    @Autowired
    private WxMpService wxMpService;

    @Override
    public void pushPendingMessage(Long processId, Long userId, String taskId) {
        //1 根据这些id将这些数据查出来
        Process process = processService.getById(processId);
        //根据userid查询要推送人的信息
        SysUser sysUser = sysUserService.getById(userId);
        //获取审批模板信息
        ProcessTemplate processTemplate = processTemplateService.getById(processId);
        //获取提交审批人的信息
        SysUser submitSysUser = sysUserService.getById(process.getUserId());

        //设置消息发送的信息
        String openId = sysUser.getOpenId();
        if(!StringUtils.hasLength(openId)){
            //TODO
            openId="oqJs36ki6w_WfZC3bVl0SEH1pkbQ";
        }
        WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder()
                .toUser(openId)//给谁发送,人的openId值
                .templateId("DZR5ExMdaOMeCQxuRSMWrmGfOOV9_2ycY7Bu128Q0l0")
                .url("http://awd.free.idcfengye.com/#/show/" + processId + "/" + taskId)//点击消息要跳转的地址
                .build();



        JSONObject jsonObject = JSON.parseObject(process.getFormValues());
        JSONObject formShowData = jsonObject.getJSONObject("formShowData");
        StringBuffer content = new StringBuffer();
        for (Map.Entry entry : formShowData.entrySet()) {
            content.append(entry.getKey()).append("：").append(entry.getValue()).append("\n ");
        }

        //设置模板里面参数的值
        /*templateMessage.addData(new WxMpTemplateData("first","内容"));*/
        templateMessage.addData(new WxMpTemplateData("first", submitSysUser.getName()+"提交了"+processTemplate.getName()+"审批申请，请注意查看。", "#272727"));
        templateMessage.addData(new WxMpTemplateData("keyword1", process.getProcessCode(), "#272727"));
        templateMessage.addData(new WxMpTemplateData("keyword2", new DateTime(process.getCreateTime()).toString("yyyy-MM-dd HH:mm:ss"), "#272727"));
        templateMessage.addData(new WxMpTemplateData("content", content.toString(), "#272727"));
        //调用方法
        try {
            String msg = wxMpService.getTemplateMsgService().sendTemplateMsg(templateMessage);
            System.out.println(msg);
        } catch (WxErrorException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void pushProcessedMessage(Long processId, Long userId, Integer status) {
        Process process = processService.getById(processId);
        ProcessTemplate processTemplate = processTemplateService.getById(process.getProcessTemplateId());
        SysUser sysUser = sysUserService.getById(userId);
        SysUser currentSysUser = sysUserService.getById(LoginUserInfoHelper.getUserId());
        String openid = sysUser.getOpenId();
        if(StringUtils.isEmpty(openid)) {
            openid = "omwf25izKON9dktgoy0dogqvnGhk";
        }
        WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder()
                .toUser(openid)//要推送的用户openid
                .templateId("I0kVeto7T0WIDP6tyoHh-hx83wa9_pe7Nx9eT93-6sc")//模板id
                .url("http://oa.atguigu.cn/#/show/"+processId+"/0")//点击模板消息要访问的网址
                .build();
        JSONObject jsonObject = JSON.parseObject(process.getFormValues());
        JSONObject formShowData = jsonObject.getJSONObject("formShowData");
        StringBuffer content = new StringBuffer();
        for (Map.Entry entry : formShowData.entrySet()) {
            content.append(entry.getKey()).append("：").append(entry.getValue()).append("\n ");
        }
        templateMessage.addData(new WxMpTemplateData("first", "你发起的"+processTemplate.getName()+"审批申请已经被处理了，请注意查看。", "#272727"));
        templateMessage.addData(new WxMpTemplateData("keyword1", process.getProcessCode(), "#272727"));
        templateMessage.addData(new WxMpTemplateData("keyword2", new DateTime(process.getCreateTime()).toString("yyyy-MM-dd HH:mm:ss"), "#272727"));
        templateMessage.addData(new WxMpTemplateData("keyword3", currentSysUser.getName(), "#272727"));
        templateMessage.addData(new WxMpTemplateData("keyword4", status == 1 ? "审批通过" : "审批拒绝", status == 1 ? "#009966" : "#FF0033"));
        templateMessage.addData(new WxMpTemplateData("content", content.toString(), "#272727"));
        try {
            String msg = wxMpService.getTemplateMsgService().sendTemplateMsg(templateMessage);
        } catch (WxErrorException e) {
            throw new RuntimeException(e);
        }
    }
}

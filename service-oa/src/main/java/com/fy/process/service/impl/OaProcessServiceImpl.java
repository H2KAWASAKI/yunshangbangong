package com.fy.process.service.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fy.auth.service.SysUserService;
import com.fy.model.process.Process;
import com.fy.model.process.ProcessRecord;
import com.fy.model.process.ProcessTemplate;
import com.fy.model.system.SysUser;
import com.fy.process.mapper.OaProcessMapper;
import com.fy.process.service.MessageService;
import com.fy.process.service.OaProcessRecordService;
import com.fy.process.service.OaProcessService;
import com.fy.process.service.OaProcessTemplateService;
import com.fy.security.custom.LoginUserInfoHelper;
import com.fy.vo.process.ApprovalVo;
import com.fy.vo.process.ProcessFormVo;
import com.fy.vo.process.ProcessQueryVo;
import com.fy.vo.process.ProcessVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.activiti.bpmn.model.*;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;


import javax.security.auth.message.callback.PrivateKeyCallback;
import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipInputStream;
/**
 * 审批类型 服务实现类
 */
@Service
public class OaProcessServiceImpl extends ServiceImpl<OaProcessMapper, Process> implements OaProcessService {

    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private OaProcessTemplateService oaprocessTemplateService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private OaProcessRecordService oaProcessRecordService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private MessageService messageService;


    //审批管理列表
    @Override
    public IPage<ProcessVo> selectPage(Page<ProcessVo> pageParam, ProcessQueryVo processQueryVo) {
        IPage<ProcessVo> pageModel = baseMapper.selectPage(pageParam,processQueryVo);
        return pageModel;
    }

    //部署流程定义
    @Override
    public void deployByZip(String deployPath) {
        InputStream inputStream =
                this.getClass().getClassLoader().getResourceAsStream(deployPath);
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        //部署
        Deployment deployment = repositoryService.createDeployment()
                .addZipInputStream(zipInputStream)
                .name("请假申请流程")
                .deploy();
        System.out.println(deployment.getId());
        System.out.println(deployment.getName());
    }

    @Override
    public void startUp(ProcessFormVo processFormVo) {
        //1 根据用户id获取用户信息
        SysUser sysUser = sysUserService.getById(LoginUserInfoHelper.getUserId());
        //2 根据审批模板id把模板信息查询
        ProcessTemplate processTemplate = oaprocessTemplateService.getById(processFormVo.getProcessTemplateId());
        //3 保存提交审批信息到业务表 oa_process
        Process process = new Process();
        //将processFormVo复制到process
        BeanUtils.copyProperties(processFormVo, process);
        String workNo = System.currentTimeMillis() + "";
        process.setProcessCode(workNo);
        process.setUserId(LoginUserInfoHelper.getUserId());
        process.setFormValues(processFormVo.getFormValues());
        process.setTitle(sysUser.getName() + "发起" + processTemplate.getName() + "申请");
        process.setStatus(1);
        baseMapper.insert(process);
        //4 启动流程实例  RuntimeService
        //4.1 流程定义key
        String processDefinitionKey = processTemplate.getProcessDefinitionKey();
        //4.2 业务key processId
        String businessKey = String.valueOf(process.getId());
        //4.3 流程参数 form表单json数据，转换map集合
        String formValues = processFormVo.getFormValues();
        //formData
        JSONObject jsonObject = JSON.parseObject(formValues);
        JSONObject formData = jsonObject.getJSONObject("formData");
        //遍历formData，得到内容，封装到map
        Map<String,Object> map = new HashMap<>();
        for(Map.Entry<String,Object> entry:formData.entrySet()){
            map.put(entry.getKey(), entry.getValue());
        }
        Map<String,Object> variables = new HashMap<>();
        variables.put("data", map);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processDefinitionKey, businessKey, variables);
        //5 查询下一个审批人
        //审批人可能会有多个
        List<Task> taskList = this.getCurrentTaskList(processInstance.getId());
        List<String> nameList = new ArrayList<>();
        for(Task task : taskList){
            String assigneeName = task.getAssignee();
            SysUser user = sysUserService.getUserByUserName(assigneeName);
            String name = user.getName();
            nameList.add(name);
            //TODO 6 推送消息
            messageService.pushPendingMessage(process.getId(),user.getId(),task.getId());
        }
        process.setProcessInstanceId(processInstance.getId());
        process.setDescription("等待" + StringUtils.join(nameList.toArray(), ",") + "审批");
        //7 业务和流程关联 更新oa_process
        baseMapper.updateById(process);
        //记录操作审批信息记录
        oaProcessRecordService.record(process.getId(), 1, "发起申请");
    }

    @Override
    public IPage<ProcessVo> findPending(Page<java.lang.Process> pageParam) {
        //1 封装查询的条件，根据当前登录的用户名称
        TaskQuery query = taskService.createTaskQuery()
                .taskAssignee(LoginUserInfoHelper.getUsername())
                .orderByTaskCreateTime()
                .desc();

        //2 调用方法进行分页条件查询，，返回list，待办任务集合
        //第一个参数，开始位置，第二个每页记录数
        int begin = (int) ((pageParam.getCurrent() - 1) * pageParam.getSize());
        int size = (int) pageParam.getSize();
        List<Task> taskList = query.listPage(begin, size);
        long totalCount = query.count();
        //3 封装返回list集合数据，到List<ProcessVo>
        List<ProcessVo> processVoList = new ArrayList<>();
        for(Task task : taskList){
            //从task里获取流程实例的id
            String processInstanceId = task.getProcessInstanceId();
            //根据流程实例id获取实例对象
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .singleResult();
            //从流程实例对象获取业务key
            String businessKey = processInstance.getBusinessKey();
            if(businessKey == null){
                continue;
            }
            //个悲剧业务key获取Process对象
            long processId = Long.parseLong(businessKey);
            Process process = baseMapper.selectById(processId);
            //Process对象 复制 ProcessVo对象
            ProcessVo processVo = new ProcessVo();
            BeanUtils.copyProperties(process, processVo);
            processVo.setTaskId(task.getId());
            //放到最终list集合
            processVoList.add(processVo);
        }
        //4 封装返回IPage对象

        IPage<ProcessVo> page = new Page<>(pageParam.getCurrent(),pageParam.getSize(),totalCount);
        page.setRecords(processVoList);
        return page;
    }

    @Override
    public Map<String, Object> show(Long id) {
        //1 根据流程id获取流程信息Process
        Process process = baseMapper.selectById(id);
        //2 根据流程id获取流程相关记录信息
        LambdaQueryWrapper<ProcessRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProcessRecord::getProcessId, id);
        List<ProcessRecord> list = oaProcessRecordService.list(wrapper);
        //3 根据模板id查询模板信息
        ProcessTemplate processTemplate = oaprocessTemplateService.getById(process.getProcessTemplateId());
        //4 判断当前用户是否可以进行审批
        //不能重复审批
        boolean isApprove = false;
        List<Task> currentTaskList = this.getCurrentTaskList(process.getProcessInstanceId());
        for(Task task:currentTaskList){
            //判断任务审批人是否是当前用户
            if(task.getAssignee().equals(LoginUserInfoHelper.getUsername())){
                isApprove=true;
            }
        }
        Map<String,Object> map = new HashMap<>();
        map.put("process", process);
        map.put("processRecordList", list);
        map.put("processTemplate", processTemplate);
        map.put("isApprove",isApprove);
        return map;
    }

    @Override
    public void approve(ApprovalVo approvalVo) {
        //1 从approvalVo获取任务id，根据任务id获取流程变量
        String taskId = approvalVo.getTaskId();
        Map<String, Object> variables = taskService.getVariables(taskId);
        for(Map.Entry<String,Object> entry:variables.entrySet()){
            System.out.println(entry.getKey());
            System.out.println(entry.getValue());
        }
        //2 判断审批状态值
        if(approvalVo.getStatus() == 1){
            //2.1 状态值=1 审批通过
            Map<String,Object> variable = new HashMap<>();
            taskService.complete(taskId,variable);
        }else{
            //2.2 状态值= -1 驳回审批直接结束
            this.endTask(taskId);
        }
        //3 记录审批相关过程 oa_process_record
        String description = approvalVo.getStatus().intValue() == 1 ? "已经通过" : "驳回";
        oaProcessRecordService.record(approvalVo.getProcessId(), approvalVo.getStatus(),description);
        //4 查询下一个审批人
        Process process = baseMapper.selectById(approvalVo.getProcessId());
        //查询待办任务
        List<Task> taskList = this.getCurrentTaskList(process.getProcessInstanceId());
        if(!CollectionUtils.isEmpty(taskList)){
            List<String> assignList = new ArrayList<>();
            for(Task task:taskList){
                String assignee = task.getAssignee();
                SysUser sysUser = sysUserService.getUserByUserName(assignee);
                assignList.add(sysUser.getName());

                //TODO 消息推送
                messageService.pushProcessedMessage(process.getId(), sysUser.getId(), process.getStatus());
            }
            //更新process信息
            process.setDescription("等待"+StringUtils.join(assignList.toArray(),",")+"审批");
            process.setStatus(1);
        }else{
            if(approvalVo.getStatus() == 1){
                process.setDescription("审批通过");
                process.setStatus(2);
            }else{
                process.setDescription("审批完成（拒绝）");
                process.setStatus(-1);
            }
        }
        baseMapper.updateById(process);
    }


    //已处理
    @Override
    public IPage<ProcessVo> findProcesed(Page<java.lang.Process> pageParam) {
        //封装查询条件
        HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(LoginUserInfoHelper.getUsername())
                .finished()
                .orderByTaskCreateTime()
                .desc();
        //调用方法
        int begin = (int)((pageParam.getCurrent()-1)*pageParam.getSize());
        int size = (int) pageParam.getSize();
        List<HistoricTaskInstance> list = query.listPage(begin, size);
        long totalCount = query.count();
        List<ProcessVo> processVoList = new ArrayList<>();
        for(HistoricTaskInstance item : list){
            String processInstanceId = item.getProcessInstanceId();
            //根据流程实例id查询获取process信息
            LambdaQueryWrapper<Process> wrapper = new LambdaQueryWrapper();
            wrapper.eq(Process::getProcessInstanceId, processInstanceId);
            Process process = baseMapper.selectOne(wrapper);
            //process -- processVo
            ProcessVo processVo = new ProcessVo();
            BeanUtils.copyProperties(process, processVo);
            //放到list
            processVoList.add(processVo);
        }
        //封装到Ipage
        IPage<ProcessVo> page =new Page<ProcessVo>(pageParam.getCurrent(),size,totalCount);
        page.setRecords(processVoList);
        return page;
    }

    //已发起
    @Override
    public IPage<ProcessVo> findStarted(Page<ProcessVo> pageParam) {
        ProcessQueryVo processVo = new ProcessQueryVo();
        processVo.setUserId(LoginUserInfoHelper.getUserId());
        IPage<ProcessVo> pageModel = baseMapper.selectPage(pageParam, processVo);
        return pageModel;
    }

    private void endTask(String taskId) {
        //1 根据任务id获取任务对象
        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .singleResult();
        //2 获取流程定义模型 BpmnModel
        BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
        //3 获取结束的流向节点
        List<EndEvent> endEventList = bpmnModel.getMainProcess().findFlowElementsOfType(EndEvent.class);
        if(CollectionUtils.isEmpty(endEventList)){
            return;
        }
        FlowNode endFlowNode = (FlowNode)endEventList.get(0);
        //4 获取当前流向节点
        FlowNode currentFlowNode = (FlowNode)bpmnModel.getMainProcess().getFlowElement(task.getTaskDefinitionKey());
        //  临时保存当前活动的原始方向
        List originalSequenceFlowList = new ArrayList<>();
        originalSequenceFlowList.addAll(currentFlowNode.getOutgoingFlows());
        //5 清理当前流动方向
        currentFlowNode.getOutgoingFlows().clear();
        //6 创建新流向
        SequenceFlow newSequenceFlow = new SequenceFlow();
        newSequenceFlow.setId("newSequenceFlow");
        newSequenceFlow.setSourceFlowElement(currentFlowNode);
        newSequenceFlow.setTargetFlowElement(endFlowNode);
        //7 当前节点指向新方向
        List newSequenceFlowList = new ArrayList();
        newSequenceFlowList.add(newSequenceFlow);
        currentFlowNode.setOutgoingFlows(newSequenceFlowList);
        //8 完成当前任务
        taskService.complete(taskId);
    }

    //当前任务的列表
    private List<Task> getCurrentTaskList(String id) {
        List<Task> list = taskService.createTaskQuery().processInstanceId(id).list();
        return list;
    }



}

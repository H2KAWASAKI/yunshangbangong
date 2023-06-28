package com.fy.process.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fy.model.process.ProcessTemplate;
import com.fy.model.process.ProcessType;
import com.fy.process.mapper.OaProcessTemplateMapper;
import com.fy.process.service.OaProcessService;
import com.fy.process.service.OaProcessTemplateService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fy.process.service.OaProcessTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * <p>
 * 审批模板 服务实现类
 * </p>
 *
 * @author fy
 * @since 2023-06-23
 */
@Service
public class OaProcessTemplateServiceImpl extends ServiceImpl<OaProcessTemplateMapper, ProcessTemplate> implements OaProcessTemplateService {

    @Autowired
    private OaProcessTypeService processTypeService;
    @Autowired
    private OaProcessService oaProcessService;
    @Override
    //分页查询审批模板，把审批类型对应名称查询
    public IPage<ProcessTemplate> selectPage(Page<ProcessTemplate> pageParam) {
        //1 调用mapper的方法实现分页查询
        Page<ProcessTemplate> processTemplatePage = baseMapper.selectPage(pageParam, null);
        //2 第一部分页查询返回分页数据，从分页数据获取列表list集合
        List<ProcessTemplate> processTemplateList = processTemplatePage.getRecords();
        //3 遍历list集合，得到每个对象对应的审批类型id
        for(ProcessTemplate processTemplate:processTemplateList){
            Long processTypeId = processTemplate.getProcessTypeId();
            //4 根据审批类型id，查询对应的名称
            LambdaQueryWrapper<ProcessType> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ProcessType::getId, processTypeId);
            ProcessType processType = processTypeService.getOne(wrapper);
            if(processType == null){
                continue;
            }
            //5 晚上封装
            processTemplate.setProcessTypeName(processType.getName());
        }
        return processTemplatePage;
    }

    @Override
    public void publish(Long id) {
        ProcessTemplate processTemplate = baseMapper.selectById(id);
        processTemplate.setStatus(1);
        baseMapper.updateById(processTemplate);

        if(StringUtils.hasLength(processTemplate.getProcessDefinitionPath())){
            oaProcessService.deployByZip(processTemplate.getProcessDefinitionPath());
        }

    }
}

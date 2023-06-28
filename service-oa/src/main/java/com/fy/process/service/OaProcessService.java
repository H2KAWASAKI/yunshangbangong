package com.fy.process.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fy.model.process.Process;
import com.fy.model.process.ProcessType;
import com.fy.vo.process.ApprovalVo;
import com.fy.vo.process.ProcessFormVo;
import com.fy.vo.process.ProcessQueryVo;
import com.fy.vo.process.ProcessVo;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 审批类型 服务类
 * </p>
 *
 * @author fy
 * @since 2023-06-24
 */
public interface OaProcessService extends IService<Process> {

    IPage<ProcessVo> selectPage(Page<ProcessVo> pageParam, ProcessQueryVo processQueryVo);
    //部署流程定义
    void deployByZip(String deployPath);

    void startUp(ProcessFormVo processFormVo);

    IPage<ProcessVo> findPending(Page<java.lang.Process> pageParam);

    Map<String, Object> show(Long id);

    void approve(ApprovalVo approvalVo);

    IPage<ProcessVo> findProcesed(Page<java.lang.Process> pageParam);

    IPage<ProcessVo> findStarted(Page<ProcessVo> pageParam);
}

package com.fy.process.controller.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fy.auth.service.SysUserService;
import com.fy.common.result.Result;
import com.fy.model.process.ProcessTemplate;
import com.fy.process.service.OaProcessService;
import com.fy.process.service.OaProcessTemplateService;
import com.fy.process.service.OaProcessTypeService;
import com.fy.vo.process.ApprovalVo;
import com.fy.vo.process.ProcessFormVo;
import com.fy.vo.process.ProcessVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Api(tags = "审批流管理")
@RestController
@RequestMapping(value="/admin/process")
@CrossOrigin  //跨域
public class ProcessController {

    @Autowired
    private OaProcessTypeService oaProcessTypeService;
    @Autowired
    private OaProcessTemplateService oaProcessTemplateService;
    @Autowired
    private OaProcessService oaProcessService;
    @Autowired
    private SysUserService sysUserService;

    @ApiOperation(value = "启动流程")
    @PostMapping("/startUp")
    public Result start(@RequestBody ProcessFormVo processFormVo){
        oaProcessService.startUp(processFormVo);
        return Result.ok();
    }

    @GetMapping("getProcessTemplate/{processTemplateId}")
    public Result getProcessTemplate(@PathVariable Long processTemplateId){
        ProcessTemplate processTemplate = oaProcessTemplateService.getById(processTemplateId);
        return Result.ok(processTemplate);
    }

    @ApiOperation(value = "获取全部审批分类及模板")
    @GetMapping("findProcessType")
    public Result findProcessType() {
        return Result.ok(oaProcessTypeService.findProcessType());
    }

    @ApiOperation(value = "待处理")
    @GetMapping("/findPending/{page}/{limit}")
    public Result findPending(
            @ApiParam(name = "page", value = "当前页码", required = true)
            @PathVariable Long page,
            @ApiParam(name = "limit", value = "每页记录数", required = true)
            @PathVariable Long limit) {
        Page<Process> pageParam = new Page<>(page,limit);
        IPage<ProcessVo> pageModel = oaProcessService.findPending(pageParam);
        return Result.ok(pageModel);
    }

    @GetMapping("show/{id}")
    public Result show(@PathVariable Long id){
        Map<String,Object> map = oaProcessService.show(id);
        return Result.ok(map);
    }
    @ApiOperation(value = "审批")
    @PostMapping("approve")
    public Result approve(@RequestBody ApprovalVo approvalVo){
        oaProcessService.approve(approvalVo);
        return null;
    }
    @ApiOperation(value = "已处理")
    @GetMapping("/findProcessed/{page}/{limit}")
    public Result findProcessed(
            @ApiParam(name = "page", value = "当前页码", required = true)
            @PathVariable Long page,
            @ApiParam(name = "limit", value = "每页记录数", required = true)
            @PathVariable Long limit) {
        Page<Process> pageParam = new Page<>(page, limit);
        IPage<ProcessVo> pageModel = oaProcessService.findProcesed(pageParam);
        return Result.ok(pageModel);
    }
    @ApiOperation(value = "已发起")
    @GetMapping("/findStarted/{page}/{limit}")
    public Result findStarted(
            @ApiParam(name = "page", value = "当前页码", required = true)
            @PathVariable Long page,

            @ApiParam(name = "limit", value = "每页记录数", required = true)
            @PathVariable Long limit) {
        Page<ProcessVo> pageParam = new Page<>(page, limit);
        IPage<ProcessVo> pageModel = oaProcessService.findStarted(pageParam);
        return Result.ok(pageModel);
    }

}
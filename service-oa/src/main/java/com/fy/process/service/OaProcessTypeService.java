package com.fy.process.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.fy.model.process.ProcessType;

import java.util.List;

/**
 * <p>
 * 审批类型 服务类
 * </p>
 *
 * @author fy
 * @since 2023-06-23
 */
public interface OaProcessTypeService extends IService<ProcessType> {
    List<ProcessType> findProcessType();
}

package com.fy.common.handler;

import com.fy.common.execption.FyException;
import com.fy.common.result.Result;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * 全局异常处理类
 *
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result error(Exception e){
        e.printStackTrace();
        return Result.fail();
    }

    @ExceptionHandler(ArithmeticException.class)
    @ResponseBody
    public Result error(ArithmeticException e){
        e.printStackTrace();
        return Result.fail().message("执行了特定异常处理");
    }
    @ExceptionHandler(FyException.class)
    @ResponseBody
    public Result error(FyException e){
        e.printStackTrace();
        return Result.fail().message(e.getMessage()).code(e.getCode());
        //return Result.fail().code(e.code).message(e.getMessage());
    }
}
package com.atguigu.lease.common.exception;

import com.atguigu.lease.common.result.Result;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

//全局异常处理器
@ControllerAdvice
public class GlobalExceptionHandler {
    //异常匹配原则，谁更精确谁处理

    //方法的返回值会作为发生异常时的返回值
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result catchException(Exception e){
        e.printStackTrace();
        return Result.fail();
    }

    //专门处理leaseException，因为Exception没有getCode方法
    @ExceptionHandler(LeaseException.class)
    @ResponseBody
    public Result handler(LeaseException e){
        e.printStackTrace();
        return Result.fail(e.getCode(),e.getMessage());
    }

}

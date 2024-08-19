package com.atguigu.lease.common.exception;

import com.atguigu.lease.common.result.ResultCodeEnum;
import lombok.Data;

@Data
public class LeaseException extends RuntimeException{
//应该有错误码和响应信息，响应信息是传递过来的，不用自己定义
    private Integer code;

    public LeaseException(Integer code,String message){
        super(message);
        this.code = code;
    }

    //这样传入参数只需要传入ResultCOdeEnum
    public LeaseException(ResultCodeEnum resultCodeEnum){
        super(resultCodeEnum.getMessage());
        this.code = resultCodeEnum.getCode();
    }


}

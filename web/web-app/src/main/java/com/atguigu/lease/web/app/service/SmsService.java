package com.atguigu.lease.web.app.service;

//发送短信
public interface SmsService {

    void sendCode(String phone,String code);
}

package com.atguigu.lease.web.app.service.impl;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.atguigu.lease.web.app.service.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SmsServiceImpl implements SmsService {

    @Autowired
    private Client client;

    @Override
    public void sendCode(String phone,String code){
        //client发送验证码必须参数
        SendSmsRequest sendSmsRequest = new SendSmsRequest();
        //手机号码，签名，传给模板的参数
        sendSmsRequest.setPhoneNumbers(phone);
        sendSmsRequest.setSignName("阿里云短信测试");
        sendSmsRequest.setTemplateCode("SMS_154950909");
        sendSmsRequest.setTemplateParam("{\"code\":\"" + code + "\"}");

        try {
            client.sendSms(sendSmsRequest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}

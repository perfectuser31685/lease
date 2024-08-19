package com.atguigu.lease.common.sms;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
//这个注解相当于用多个Value绑定符合条件的所有值
@ConfigurationProperties(prefix = "aliyun.sms")
public class AliyunSMSProperties {

    private String accessKeyId;

    private String accessKeySecret;

    private String endpoint;
}

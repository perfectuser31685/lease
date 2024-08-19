package com.atguigu.lease.common.sms;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.teaopenapi.models.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AliyunSMSProperties.class)
@ConditionalOnProperty(name = "aliyun.sms.endpoint")
public class AliyunSmsConfiguration {

    @Autowired
    private AliyunSMSProperties properties;

    //config用来配置accessKeyId,accessKeySecret,endpoint
    @Bean
    public Client smsClient() {
        Config config = new Config();
        config.setAccessKeyId(properties.getAccessKeyId());
        config.setAccessKeySecret(properties.getAccessKeySecret());
        config.setEndpoint(properties.getEndpoint());
        //创建可能有异常
        try {
            return new Client(config);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}

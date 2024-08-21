package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.common.constant.RedisConstant;
import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.result.ResultCodeEnum;
import com.atguigu.lease.common.utils.CodeUtil;
import com.atguigu.lease.common.utils.JwtUtil;
import com.atguigu.lease.model.entity.UserInfo;
import com.atguigu.lease.model.enums.BaseStatus;
import com.atguigu.lease.web.app.mapper.UserInfoMapper;
import com.atguigu.lease.web.app.service.LoginService;
import com.atguigu.lease.web.app.service.SmsService;
import com.atguigu.lease.web.app.vo.user.LoginVo;
import com.atguigu.lease.web.app.vo.user.UserInfoVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private SmsService smsService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private UserInfoMapper userInfoMapper;

    //需要将key，验证码保存到Redis中
    //需要控制短信发送频率，每分钟一条
    @Override
    public void getCode(String phone) {
        String code = CodeUtil.getRandomCode(6);
        String key = RedisConstant.APP_LOGIN_PREFIX + phone;
        //发送验证码前加入判断，看是否一分钟内发送过验证码，先看是否发过，是否有key，有的话再看是否过了一分钟
        Boolean hasKey = stringRedisTemplate.hasKey(key);
        if(hasKey){
            Long ttl = stringRedisTemplate.getExpire(key,TimeUnit.SECONDS);
            if(RedisConstant.APP_LOGIN_CODE_TTL_SEC-ttl<60){
                throw new LeaseException(ResultCodeEnum.APP_SEND_SMS_TOO_OFTEN);
            }
        }
        smsService.sendCode(phone,code);
        stringRedisTemplate.opsForValue().set(key,code,RedisConstant.APP_LOGIN_CODE_TTL_SEC, TimeUnit.SECONDS);
    }

    @Override
    public String login(LoginVo loginVo) {
        System.out.println(loginVo.toString());
        if(loginVo.getPhone() == null){
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_PHONE_EMPTY);
        }
        if(loginVo.getCode() == null){
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_CODE_EMPTY);
        }
        String key = RedisConstant.APP_LOGIN_PREFIX + loginVo.getPhone();
        System.out.println(key);
        String code = stringRedisTemplate.opsForValue().get(key);
        System.out.println(code);
        if(code == null){
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_CODE_EXPIRED);
        }
        if(!code.equals(loginVo.getCode())){
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_CODE_ERROR);
        }
        //看看是否注册过
        LambdaQueryWrapper<UserInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserInfo::getPhone,loginVo.getPhone());
        UserInfo userInfo = userInfoMapper.selectOne(queryWrapper);

        //看看是否是新用户
        //是，注册
        if(userInfo == null){
            userInfo = new UserInfo();
            userInfo.setPhone(loginVo.getPhone());
            userInfo.setStatus(BaseStatus.ENABLE);
            userInfo.setNickname("用户-" + loginVo.getPhone().substring(7));
            userInfoMapper.insert(userInfo);
        }else {
            //不是，看看是否被禁用
            if (userInfo.getStatus() == BaseStatus.DISABLE) {
                throw new LeaseException(ResultCodeEnum.APP_ACCOUNT_DISABLED_ERROR);
            }
        }

        return JwtUtil.createToken(userInfo.getId(),userInfo.getPhone());
    }

    @Override
    public UserInfoVo getUserById(Long id) {
        UserInfo userInfo = userInfoMapper.selectById(id);
        UserInfoVo userInfoVo = new UserInfoVo(userInfo.getNickname(),userInfo.getAvatarUrl());
        return userInfoVo;
    }
}

package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.common.constant.RedisConstant;
import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.common.result.ResultCodeEnum;
import com.atguigu.lease.common.utils.JwtUtil;
import com.atguigu.lease.model.entity.SystemUser;
import com.atguigu.lease.model.enums.BaseStatus;
import com.atguigu.lease.web.admin.mapper.SystemUserMapper;
import com.atguigu.lease.web.admin.service.LoginService;
import com.atguigu.lease.web.admin.service.SystemUserService;
import com.atguigu.lease.web.admin.vo.login.CaptchaVo;
import com.atguigu.lease.web.admin.vo.login.LoginVo;
import com.atguigu.lease.web.admin.vo.system.user.SystemUserInfoVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wf.captcha.SpecCaptcha;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class LoginServiceImpl implements LoginService {

    //生成UUID作为key，图形验证码的值作为value保存在Redis中,还要设置一个TTL，即有效时间
    //UUID是唯一标识，因为可能会有多个用户同时进行验证码验证，只有加上才能根据UUID去匹配正确的验证码

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private SystemUserMapper systemUserMapper;

    @Override
    public CaptchaVo getCaptcha() {
        //生成图形验证码，不区分大小写
        SpecCaptcha specCaptcha = new SpecCaptcha(130,48,5);
        String value = specCaptcha.text().toLowerCase();
        String key = RedisConstant.ADMIN_LOGIN_PREFIX + UUID.randomUUID();
        //四个成员，键、值、有效期、有效期单位
        stringRedisTemplate.opsForValue().set(key,value,RedisConstant.ADMIN_LOGIN_CAPTCHA_TTL_SEC, TimeUnit.SECONDS);
        return new CaptchaVo(specCaptcha.toBase64(), key);
    }

    //先判断验证码，先判断是否为空，再判断是否正确
    //然后判断账号是否存在
    //然后判断帐号是否被禁用
    //然后判断密码正确性
    //最后创建jwt返还给浏览器
    @Override
    public String getToken(LoginVo loginVo) {
        //用户名/密码/验证码key/code
        //验证码输入了吗
        if (!StringUtils.hasText(loginVo.getCaptchaCode())) {
            throw new LeaseException(ResultCodeEnum.ADMIN_CAPTCHA_CODE_NOT_FOUND);
        }
        //验证码正确吗
        String code = stringRedisTemplate.opsForValue().get(loginVo.getCaptchaKey());
        if (code == null) {
            throw new LeaseException(ResultCodeEnum.ADMIN_CAPTCHA_CODE_EXPIRED);
        }
        if (!code.equals(loginVo.getCaptchaCode().toLowerCase())) {
            throw new LeaseException(ResultCodeEnum.ADMIN_CAPTCHA_CODE_ERROR);
        }
        //账号存在吗
//        LambdaQueryWrapper<SystemUser> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(SystemUser::getUsername,loginVo.getUsername());
//        //注意这里password字段不能被通用select获取，需要自己定义才能取出password
        SystemUser systemUser = systemUserMapper.selectOneByUsername(loginVo.getUsername());
        if (systemUser == null) {
            throw new LeaseException(ResultCodeEnum.ADMIN_ACCOUNT_NOT_EXIST_ERROR);
        }
        //账号是否被封禁
        if(systemUser.getStatus() == BaseStatus.DISABLE){
            throw new LeaseException(ResultCodeEnum.ADMIN_ACCOUNT_DISABLED_ERROR);
        }
        //验证密码，获取的密码是加密后的，传进来的是加密前的
        if (!systemUser.getPassword().equals(DigestUtils.md5Hex(loginVo.getPassword()))) {
            throw new LeaseException(ResultCodeEnum.ADMIN_ACCOUNT_ERROR);
        }

        //创建token
        return JwtUtil.createToken(systemUser.getId(),systemUser.getUsername());

    }

    @Override
    public SystemUserInfoVo getUserInfoVo(long userId) {
        SystemUserInfoVo systemUserInfoVo = new SystemUserInfoVo();
        SystemUser systemUser = systemUserMapper.selectById(userId);
        systemUserInfoVo.setName(systemUser.getName());
        systemUser.setAvatarUrl(systemUser.getAvatarUrl());
        return systemUserInfoVo;
    }
}

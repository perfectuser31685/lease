package com.atguigu.lease.web.admin.controller.login;


import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.common.utils.JwtUtil;
import com.atguigu.lease.web.admin.service.LoginService;
import com.atguigu.lease.web.admin.vo.login.CaptchaVo;
import com.atguigu.lease.web.admin.vo.login.LoginVo;
import com.atguigu.lease.web.admin.vo.system.user.SystemUserInfoVo;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "后台管理系统登录管理")
@RestController
@RequestMapping("/admin")
public class LoginController {

    @Autowired
    private LoginService loginService;

    @Operation(summary = "获取图形验证码")
    @GetMapping("login/captcha")
    public Result<CaptchaVo> getCaptcha() {
        CaptchaVo result = loginService.getCaptcha();
        return Result.ok(result);
    }

    //登陆成功返还token，所以是String
    @Operation(summary = "登录")
    @PostMapping("login")
    public Result<String> login(@RequestBody LoginVo loginVo) {
        String token = loginService.getToken(loginVo);
        return Result.ok(token);
    }

    //没有参数,从token中获取参数,但是如果两次调用token(拦截器和本方法),更好的处理是将拦截器处理后的数据保存在threadLocal中
    //threadLocal是Java提供的线程本地存储机制，将数据缓存在线程内部，该线程在任意时刻，任意方法中访问该数据
    @Operation(summary = "获取登陆用户个人信息")
    @GetMapping("info")
    //要指定获取请求头中哪个参数
    public Result<SystemUserInfoVo> info(@RequestHeader("access-token") String token) {
        Claims claims = JwtUtil.parseToken(token);
        long userId = claims.get("userId",Long.class);
        SystemUserInfoVo systemUserInfoVo = loginService.getUserInfoVo(userId);
        return Result.ok(systemUserInfoVo);
    }
}
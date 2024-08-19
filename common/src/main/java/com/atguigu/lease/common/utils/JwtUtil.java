package com.atguigu.lease.common.utils;

import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.result.ResultCodeEnum;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

public class JwtUtil {

    //定义签名算法的密钥,HS对应的就是下列方法
    private static SecretKey secretKey = Keys.hmacShaKeyFor("nVZ7X83)vOho*ABc0b]My#2J9./4k1Ww".getBytes());    //密码不能太弱，否则会报密码弱的错，密码字节数应该大于256

    //创建token，token中应该包含用户的标识和名称，所以这里用id和name,header一般不用定义，需要定义的只有payload和签名,要指定签名算法，算法需要先定义密钥
    public static String createToken(Long userId, String username) {
        //官方字段用set,自己定义的用claim
        String jwt = Jwts.builder()
                .setExpiration(new Date(System.currentTimeMillis() + 3600000*24))       //设置的是过期的时间点，而不是有效时长,所以可用当前时间加上有效时长
                .setSubject("LOGIN_USER")        //主题
                .claim("userId", userId)
                .claim("username", username)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
        return jwt;
    }

    //在获取用户Vo信息的时候,需要从token中获取一些信息
    public static Claims parseToken(String token) {
        if(token == null){
            throw new LeaseException(ResultCodeEnum.ADMIN_LOGIN_AUTH);
        }
        try{
            //密码要匹配
            JwtParser jwtParser = Jwts.parserBuilder().setSigningKey(secretKey).build();
            Jws<Claims> claimsJws = jwtParser.parseClaimsJws(token);  //这个对象能够获取到jwt中的payload
            return claimsJws.getBody();
        }catch (ExpiredJwtException e){
            throw new LeaseException(ResultCodeEnum.TOKEN_EXPIRED);
        }catch (JwtException e){
            throw new LeaseException(ResultCodeEnum.TOKEN_INVALID);
        }
    }

    public static void main(String[] args) {
        System.out.println(createToken(2L,"user"));
    }


}

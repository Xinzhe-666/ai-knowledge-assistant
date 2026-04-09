package com.xinzhe.aiassistant.common.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT工具类
 * 负责Token的生成、解析、校验
 */
@Component // 把这个类交给Spring管理，其他地方可以用@Autowired注入
public class JwtUtil {

    /**
     * JWT签名密钥，必须足够长（至少32个字符），绝对不能泄露！
     * 我们写在配置文件里，不用硬编码在代码里
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * Token过期时间，单位：毫秒
     * 我们配置2小时过期：2 * 60 * 60 * 1000 = 7200000毫秒
     */
    @Value("${jwt.expire-time}")
    private Long expireTime;

    /**
     * 1. 生成加密用的密钥
     * 把我们配置的字符串密钥，转换成JWT要求的SecretKey对象
     */
    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 2. 生成Token（核心方法）
     * @param userId 用户ID
     * @param username 用户名
     * @return 生成的JWT Token字符串
     */
    public String generateToken(Long userId, String username) {
        Date now = new Date();
        // 计算过期时间：当前时间 + 配置的过期时长
        Date expireDate = new Date(now.getTime() + expireTime);

        // 构建并生成Token
        return Jwts.builder()
                // Header：指定加密算法
                .signWith(getSecretKey(), Jwts.SIG.HS256)
                // Payload：存入用户信息（非敏感）
                .claim("userId", userId) // 自定义字段：用户ID
                .claim("username", username) // 自定义字段：用户名
                // 签发时间
                .issuedAt(now)
                // 过期时间
                .expiration(expireDate)
                // 压缩成字符串
                .compact();
    }

    /**
     * 3. 解析Token，获取Payload里的Claims（所有数据）
     * @param token 前端传来的Token
     * @return Claims对象，包含Token里的所有数据
     */
    public Claims parseToken(String token) {
        try {
            // 解析Token，校验签名、过期时间
            return Jwts.parser()
                    .verifyWith(getSecretKey()) // 用密钥校验签名
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            // Token过期异常
            throw new RuntimeException("Token已过期，请重新登录");
        } catch (JwtException e) {
            // Token签名错误、格式错误等异常
            throw new RuntimeException("无效的Token，请重新登录");
        }
    }

    /**
     * 4. 从Token中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);
    }

    /**
     * 5. 从Token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("username", String.class);
    }

    /**
     * 6. 校验Token是否有效
     * @param token 前端传来的Token
     * @return true=有效，false=无效
     */
    public boolean validateToken(String token) {
        try {
            // 能正常解析，就说明Token有效、没过期、签名正确
            parseToken(token);
            return true;
        } catch (Exception e) {
            // 解析失败，说明Token无效
            return false;
        }
    }
}
package com.bbplay.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bbplay.app.dto.auth.LoginRequest;
import com.bbplay.app.dto.auth.RegisterRequest;
import com.bbplay.app.entity.SysUser;
import com.bbplay.app.exception.BusinessException;
import com.bbplay.app.mapper.SysUserMapper;
import com.bbplay.app.service.UserService;
import com.bbplay.app.util.JwtUtil;
import com.bbplay.app.vo.AuthVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户认证服务实现。
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        LambdaQueryWrapper<SysUser> query = new LambdaQueryWrapper<>();
        query.eq(SysUser::getUsername, request.getUsername());
        if (userMapper.selectCount(query) > 0) {
            throw new BusinessException("用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);
    }

    @Override
    public AuthVO login(LoginRequest request) {
        LambdaQueryWrapper<SysUser> query = new LambdaQueryWrapper<>();
        query.eq(SysUser::getUsername, request.getUsername());
        SysUser user = userMapper.selectOne(query);

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        return new AuthVO(token, user.getUsername(), user.getRole());
    }

    @Override
    public AuthVO validateToken(String token) {
        if (!jwtUtil.validateToken(token)) {
            throw new BusinessException("Token无效或已过期");
        }

        String username = jwtUtil.getUsernameFromToken(token);
        String role = jwtUtil.getRoleFromToken(token);
        return new AuthVO(token, username, role);
    }
}

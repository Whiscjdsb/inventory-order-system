package com.example.inventorypractice.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.inventorypractice.dto.LoginRequest;
import com.example.inventorypractice.dto.RegisterRequest;
import com.example.inventorypractice.entity.SysUser;
import com.example.inventorypractice.exception.BusinessException;
import com.example.inventorypractice.mapper.SysUserMapper;
import com.example.inventorypractice.security.JwtTokenProvider;
import com.example.inventorypractice.vo.LoginVO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {
    private final SysUserMapper sysUserMapper  ;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(SysUserMapper sysUserMapper, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.sysUserMapper = sysUserMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public void register(RegisterRequest request) {
    LambdaQueryWrapper<SysUser>  queryWrapper = new LambdaQueryWrapper<>();
    String username = request.getUsername().trim();
    queryWrapper.eq(SysUser::getUsername, username);

    Long count = sysUserMapper.selectCount(queryWrapper);

    if (count > 0) {
        throw new BusinessException(400,"用户名已存在");
    }
    SysUser user = new SysUser();
    user.setUsername(username);
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user.setRole("USER");
    user.setStatus(1);
    user.setCreateTime(LocalDateTime.now());
    user.setUpdateTime(LocalDateTime.now());
    sysUserMapper.insert(user);
    }

    public LoginVO login(LoginRequest request) {
        String username = request.getUsername().trim();
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getUsername, username);
        SysUser user = sysUserMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        boolean passwordCorrect = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!passwordCorrect) {
            throw new BusinessException(401, "用户名或密码错误");
        }if (user.getStatus() == 0){
            throw new BusinessException(403, "用户已禁用");
        }
        String token = jwtTokenProvider.generateToken(user);
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setTokenType("Bearer");
        loginVO.setExpiresInSeconds(jwtTokenProvider.getExpirationSeconds());
        return loginVO;
    }
}

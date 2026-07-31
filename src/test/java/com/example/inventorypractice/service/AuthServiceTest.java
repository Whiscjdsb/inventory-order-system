package com.example.inventorypractice.service;

import com.example.inventorypractice.dto.LoginRequest;
import com.example.inventorypractice.dto.RegisterRequest;
import com.example.inventorypractice.entity.SysUser;
import com.example.inventorypractice.exception.BusinessException;
import com.example.inventorypractice.mapper.SysUserMapper;
import com.example.inventorypractice.security.JwtTokenProvider;
import com.example.inventorypractice.vo.LoginVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;
    @Test
    void shouldLoginSuccessfully() {
        LoginRequest request = new LoginRequest();
        request.setUsername("backend_intern");
        request.setPassword("Backend123");

        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("backend_intern");
        user.setPassword("encoded-password");
        user.setRole("ADMIN");
        user.setStatus(1);

        when(sysUserMapper.selectOne(any()))
                .thenReturn(user);

        when(passwordEncoder.matches(
                "Backend123",
                "encoded-password"
        )).thenReturn(true);

        when(jwtTokenProvider.generateToken(user))
                .thenReturn("test-token");

        when(jwtTokenProvider.getExpirationSeconds())
                .thenReturn(86400L);

        LoginVO result = authService.login(request);

        assertEquals("test-token", result.getToken());
        assertEquals("Bearer", result.getTokenType());
        assertEquals(86400L, result.getExpiresInSeconds());
    }

    @Test
    void shouldThrowExceptionWhenPasswordIsWrong(){
        LoginRequest request = new LoginRequest();

        request.setUsername("backend_intern");
        request.setPassword("wrong-password");

        SysUser user = new SysUser();
        user.setUsername("backend_intern");
        user.setPassword("encoded-password");
        user.setStatus(1);

        when(sysUserMapper.selectOne(any()))
                .thenReturn(user);

        when(passwordEncoder.matches(
                "wrong-password",
                "encoded-password"
        )).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class, () -> authService.login(request));

        assertEquals(401, exception.getCode());
        assertEquals("用户名或密码错误", exception.getMessage());

        verify(jwtTokenProvider, never()).generateToken(any());
    }
    @Test
    void shouldThrowWhenUserDoesNotExist() {
        LoginRequest request = new LoginRequest();
        request.setUsername("not_exist");
        request.setPassword("Backend123");

        when(sysUserMapper.selectOne(any()))
                .thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> authService.login(request)
        );

        assertEquals(401, exception.getCode());
        assertEquals(
                "用户名或密码错误",
                exception.getMessage()
        );

        verifyNoInteractions(
                passwordEncoder,
                jwtTokenProvider
        );
    }
    @Test
    void shouldThrowWhenUserIsDisabled() {
        LoginRequest request = new LoginRequest();
        request.setUsername("disabled_user");
        request.setPassword("Backend123");

        SysUser user = new SysUser();
        user.setUsername("disabled_user");
        user.setPassword("encoded-password");
        user.setStatus(0);

        when(sysUserMapper.selectOne(any()))
                .thenReturn(user);

        when(passwordEncoder.matches("Backend123","encoded-password"))
                .thenReturn(true);
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> authService.login( request)
        );

        assertEquals(403, exception.getCode());
        assertEquals("用户已禁用", exception.getMessage());

        verify(jwtTokenProvider, never()).generateToken(any());
    }
    @Test
    void shouldConvertDuplicateUsernameToBusinessException() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("backend_intern");
        request.setPassword("Backend123");

        when(sysUserMapper.selectCount(any()))
                .thenReturn(0L);

        when(passwordEncoder.encode("Backend123"))
                .thenReturn("encoded-password");

        when(sysUserMapper.insert(any(SysUser.class)))
                .thenThrow(
                        new DuplicateKeyException(
                                "duplicate username"
                        )
                );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> authService.register(request)
        );

        assertEquals(400, exception.getCode());
        assertEquals(
                "用户名已存在",
                exception.getMessage()
        );
    }

}

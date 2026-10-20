package com.example.inventorypractice.config;

import com.example.inventorypractice.controller.AdminController;
import com.example.inventorypractice.entity.SysUser;
import com.example.inventorypractice.mapper.SysUserMapper;
import com.example.inventorypractice.security.JwtAuthenticationFilter;
import com.example.inventorypractice.security.JwtTokenProvider;
import com.example.inventorypractice.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    // @WebMvcTest 不会创建真正的 AuthService，使用 Mock 满足 AdminController 的构造方法依赖。
    @MockBean
    private AuthService authService;

    // JWT 过滤器现在需要查询用户，测试中使用 Mock Mapper 返回我们准备好的用户。
    @MockBean
    private SysUserMapper sysUserMapper;

    @Test
    void shouldReturnJsonWhenUserIsUnauthenticated() throws Exception {
        // 不携带 Token 访问管理员接口，应由认证入口返回统一 401 JSON。
        mockMvc.perform(get("/api/admin/ping"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("未认证"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void shouldReturnJsonWhenUserHasInsufficientPermission() throws Exception {
        String token = "user-token";

        SysUser user = new SysUser();
        user.setUsername("backend_intern");
        user.setStatus(1);
        user.setTokenVersion(0);

        // 模拟一个有效的 USER Token：身份有效，但没有 ADMIN 权限。
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getUsername(token)).thenReturn("backend_intern");
        when(jwtTokenProvider.getRole(token)).thenReturn("USER");
        when(jwtTokenProvider.getTokenVersion(token)).thenReturn(0);
        when(sysUserMapper.selectOne(any())).thenReturn(user);

        mockMvc.perform(
                        get("/api/admin/ping")
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("权限不足"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void shouldReturnUnauthorizedWhenUserIsDisabled() throws Exception {
        String token = "disabled-user-token";

        // JWT 本身仍然有效，并且版本相同，但数据库中的用户已经被禁用。
        SysUser user = new SysUser();
        user.setUsername("disabled_user");
        user.setStatus(0);
        user.setTokenVersion(0);

        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getUsername(token)).thenReturn("disabled_user");
        when(jwtTokenProvider.getRole(token)).thenReturn("USER");
        when(jwtTokenProvider.getTokenVersion(token)).thenReturn(0);
        when(sysUserMapper.selectOne(any())).thenReturn(user);

        mockMvc.perform(
                        get("/api/admin/ping")
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("未认证"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void shouldReturnUnauthorizedWhenTokenVersionIsOld() throws Exception {
        String token = "old-version-token";

        // 用户仍然启用，但数据库版本已经是 1，旧 JWT 中保存的还是 0。
        SysUser user = new SysUser();
        user.setUsername("backend_intern");
        user.setStatus(1);
        user.setTokenVersion(1);

        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getUsername(token)).thenReturn("backend_intern");
        when(jwtTokenProvider.getRole(token)).thenReturn("USER");
        when(jwtTokenProvider.getTokenVersion(token)).thenReturn(0);
        when(sysUserMapper.selectOne(any())).thenReturn(user);

        mockMvc.perform(
                        get("/api/admin/ping")
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("未认证"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }
}

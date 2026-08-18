package com.example.inventorypractice.config;

import com.example.inventorypractice.controller.AdminController;
import com.example.inventorypractice.security.JwtAuthenticationFilter;
import com.example.inventorypractice.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

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

    @Test
    void shouldReturnJsonWhenUserIsUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/admin/ping"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("未认证"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void shouldReturnJsonWhenUserHasInsufficientPermission() throws Exception {
        String token = "user-token";

        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getUsername(token)).thenReturn("backend_intern");
        when(jwtTokenProvider.getRole(token)).thenReturn("USER");

        mockMvc.perform(
                        get("/api/admin/ping")
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("权限不足"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }
}

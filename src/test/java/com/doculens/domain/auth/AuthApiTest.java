package com.doculens.domain.auth;

import com.doculens.support.IntegrationTestBase;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthApiTest extends IntegrationTestBase {

    @Test
    @DisplayName("회원가입 → 201 + accessToken 반환")
    void register() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"test@doculens.com","password":"password123","name":"테스터"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.email").value("test@doculens.com"));
    }

    @Test
    @DisplayName("이메일 중복 회원가입 → 409")
    void registerDuplicate() throws Exception {
        String body = """
                {"email":"dup@doculens.com","password":"password123","name":"테스터"}
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("로그인 → 200 + accessToken 반환")
    void login() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email":"login@doculens.com","password":"password123","name":"로그인테스터"}
                        """));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"login@doculens.com","password":"password123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists());
    }

    @Test
    @DisplayName("잘못된 비밀번호 로그인 → 400")
    void loginWrongPassword() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email":"wrong@doculens.com","password":"password123","name":"테스터"}
                        """));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"wrong@doculens.com","password":"wrongpassword"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("인증 없이 보호된 API 접근 → 401/403")
    void accessProtectedWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/collections"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("JWT 토큰으로 보호된 API 접근 → 200")
    void accessProtectedWithToken() throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"auth@doculens.com","password":"password123","name":"인증테스터"}
                                """))
                .andReturn().getResponse().getContentAsString();

        String token = JsonPath.read(response, "$.accessToken");

        mockMvc.perform(get("/api/v1/collections")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}

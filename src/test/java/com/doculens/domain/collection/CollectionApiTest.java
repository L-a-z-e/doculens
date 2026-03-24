package com.doculens.domain.collection;

import com.doculens.support.IntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CollectionApiTest extends IntegrationTestBase {

    @Test
    @DisplayName("컬렉션 생성 → 201")
    void createCollection() throws Exception {
        mockMvc.perform(post("/api/v1/collections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "테스트 컬렉션", "description": "통합 테스트"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("테스트 컬렉션"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DisplayName("중복 이름 생성 → 409")
    void createDuplicateName() throws Exception {
        String body = """
                {"name": "중복 테스트", "description": "desc"}
                """;

        mockMvc.perform(post("/api/v1/collections")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

        mockMvc.perform(post("/api/v1/collections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("이름 없이 생성 → 400")
    void createWithoutName() throws Exception {
        mockMvc.perform(post("/api/v1/collections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description": "이름 없음"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("존재하지 않는 ID 조회 → 404")
    void findByNonExistentId() throws Exception {
        mockMvc.perform(get("/api/v1/collections/00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("컬렉션 목록 조회 → 200 + 페이지네이션")
    void findAll() throws Exception {
        mockMvc.perform(post("/api/v1/collections")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "목록 테스트", "description": "desc"}
                        """));

        mockMvc.perform(get("/api/v1/collections"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").isNumber());
    }
}

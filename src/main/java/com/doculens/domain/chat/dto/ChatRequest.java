package com.doculens.domain.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ChatRequest(
        @NotBlank(message = "질문을 입력해주세요")
        @Size(max = 2000, message = "질문은 2000자 이내여야 합니다")
        String question,

        @NotNull(message = "컬렉션 ID는 필수입니다")
        UUID collectionId,

        String sessionId
) {
}

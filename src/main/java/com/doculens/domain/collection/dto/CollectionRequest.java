package com.doculens.domain.collection.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CollectionRequest(
        @NotBlank(message = "컬렉션 이름은 필수입니다")
        @Size(max = 200, message = "컬렉션 이름은 200자 이내여야 합니다")
        String name,

        @Size(max = 2000, message = "설명은 2000자 이내여야 합니다")
        String description
) {
}

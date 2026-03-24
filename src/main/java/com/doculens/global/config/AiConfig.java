package com.doculens.global.config;

import com.doculens.ai.tools.DocumentTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    private static final String SYSTEM_PROMPT = """
            You are DocuLens, an AI assistant specialized in analyzing technical documentation.

            Rules:
            - Answer questions based ONLY on the provided document context
            - Always cite sources: [Source: document_title]
            - If context doesn't contain the answer, say "업로드된 문서에서 해당 정보를 찾을 수 없습니다"
            - Never fabricate information not in the documents
            - Use the same language as the user's question
            - Keep technical terms in English
            """;

    @Bean
    ChatClient chatClient(@Qualifier("ollamaChatModel") ChatModel chatModel,
                          DocumentTools documentTools) {
        return ChatClient.builder(chatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultTools(documentTools)
                .build();
    }
}

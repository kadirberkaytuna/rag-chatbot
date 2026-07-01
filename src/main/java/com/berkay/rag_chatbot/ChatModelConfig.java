package com.berkay.rag_chatbot;

import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class ChatModelConfig {

    @Bean
    @ConditionalOnProperty(name = "app.llm.provider", havingValue = "ollama")
    public ChatModel ollamaChatModel(
            @Value("${app.llm.ollama.base-url}") String baseUrl,
            @Value("${app.llm.ollama.model-name}") String modelName) {
        return OllamaChatModel.builder()
                .baseUrl(baseUrl)
                .modelName(modelName)
                .temperature(0.7)
                .timeout(Duration.ofSeconds(120))
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "app.llm.provider", havingValue = "anthropic")
    public ChatModel anthropicChatModel(
            @Value("${app.llm.anthropic.api-key}") String apiKey,
            @Value("${app.llm.anthropic.model-name}") String modelName) {
        return AnthropicChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "app.llm.provider", havingValue = "openai")
    public ChatModel openAiChatModel(
            @Value("${app.llm.openai.api-key}") String apiKey,
            @Value("${app.llm.openai.model-name}") String modelName) {
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .build();
    }
}
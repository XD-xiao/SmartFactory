package com.example.smartfactoryservice.Service.AgentModule.impl;

import com.example.smartfactoryservice.Service.AgentModule.AIAgentService;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.response.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIAgentServiceImpl implements AIAgentService {

    private final OpenAiChatModel deepSeekChatModel;

    @Override
    public String chat(String userMessage) {
        log.info("DeepSeek chat request: {}", userMessage);
        String response = deepSeekChatModel.chat(userMessage);
        log.info("DeepSeek chat response: {}", response);
        return response;
    }

    @Override
    public String chatWithSystem(String systemPrompt, String userMessage) {
        log.info("DeepSeek chat with system request: system={}, user={}", systemPrompt, userMessage);

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new SystemMessage(systemPrompt));
        messages.add(new UserMessage(userMessage));

        ChatResponse response = deepSeekChatModel.chat(messages);
        String text = response.aiMessage().text();
        log.info("DeepSeek chat with system response: {}", text);
        return text;
    }
}

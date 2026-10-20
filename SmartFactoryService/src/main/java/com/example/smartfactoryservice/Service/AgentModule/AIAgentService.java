package com.example.smartfactoryservice.Service.AgentModule;

public interface AIAgentService {

    /**
     * 发送消息给DeepSeek模型并获取回复
     *
     * @param userMessage 用户输入的消息
     * @return 模型回复内容
     */
    String chat(String userMessage);

    /**
     * 发送带系统提示词的消息给DeepSeek模型
     *
     * @param systemPrompt 系统提示词
     * @param userMessage  用户输入的消息
     * @return 模型回复内容
     */
    String chatWithSystem(String systemPrompt, String userMessage);
}

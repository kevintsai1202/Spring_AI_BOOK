/**
 * Prompt Template Service
 * Provides basic prompt template functionality
 */
package com.example.service;

import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PromptTemplateService {

    /**
     * Create a basic prompt with template substitution
     * @param topic The topic to explain
     * @param level The difficulty level (Beginner/Intermediate/Advanced)
     * @return The created Prompt
     */
    public Prompt createBasicPrompt(String topic, String level) {
        // Define template string with {variable} placeholders
        String template = "As a senior technical expert, please provide a detailed explanation of {topic}. "
                + "The explanation should include: 1. Core concepts and definitions, "
                + "2. Main features and advantages, 3. Practical use cases, 4. Simple code examples. "
                + "Target audience: {level} developers. "
                + "Response language: English. "
                + "Response style: Professional and easy to understand.";

        // Create PromptTemplate instance
        PromptTemplate promptTemplate = new PromptTemplate(template);

        // Provide variable values
        Map<String, Object> variables = Map.of(
            "topic", topic,
            "level", level
        );

        // Generate final Prompt
        return promptTemplate.create(variables);
    }
}

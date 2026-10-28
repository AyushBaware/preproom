package com.preproom.engine;

import java.util.Map;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class LlmTestController {

    private final ChatClient chatClient;
    private final boolean enabled;

    public LlmTestController(ChatClient.Builder builder,
                             @Value("${preproom.llmtest.enabled:false}") boolean enabled) {
        this.chatClient = builder.build();
        this.enabled = enabled;
    }

    @GetMapping("/api/llm-test")
    public Map<String, String> test() {
        if (!enabled) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        String answer = chatClient.prompt()
                .user("Reply with exactly these two words: PrepRoom online")
                .call()
                .content();
        return Map.of("answer", answer == null ? "" : answer);
    }
}
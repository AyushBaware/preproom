package com.preproom.engine;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @org.springframework.beans.factory.annotation.Value("${server.port:8080}")
    private String port;

    @GetMapping("/api/hello")
    public Map<String, String> hello() {
        return Map.of("service", "engine", "status", "ok", "instance", port == null ? "unknown" : port);
    }
}
package com.miniecommerce.eureka.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/config")
@RefreshScope
public class ConfigTestController {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${server.port}")
    private String serverPort;


    @GetMapping("/info")
    public Map<String, String> getConfigInfo() {
        Map<String, String> config = new HashMap<>();
        config.put("applicationName", applicationName);
        config.put("serverPort", serverPort);
        config.put("message", "Configuration loaded from Config Server");
        return config;
    }
}
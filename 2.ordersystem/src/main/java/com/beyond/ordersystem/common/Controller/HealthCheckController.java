package com.beyond.ordersystem.common.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {
    @GetMapping("/health")
    public String healthCheck() {
        return "ok, k8s let's go 0905";
    }
}

package com.example.ordersystem.common.controller;

import com.example.ordersystem.common.service.SseEmitterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sse")
public class SseController {
    private final SseEmitterRegistry sseEmitterRegistry;

    @GetMapping("/connect")
    public SseEmitter subscribe(
            @RequestHeader("X-User-Email") String email
    ) {
        SseEmitter sseEmitter = new SseEmitter(14400 * 60 * 1000L);   // 10일 정도 emitter 유효기간 설정
        sseEmitterRegistry.addSseEmitter(email, sseEmitter);   // admin이메일과 emitter을 보내서 connect 요청하고 등록

        try {
            sseEmitter.send(SseEmitter.event().name("connect").data("연결완료"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sseEmitter;
    }

    @GetMapping("/disconnect")
    public void unSubscribe(
            @RequestHeader("X-User-Email") String email
    ) {
        sseEmitterRegistry.removeEmitter(email);
    }
}

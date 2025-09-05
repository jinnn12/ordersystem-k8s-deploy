package com.beyond.ordersystem.common.Controller;

import com.beyond.ordersystem.common.service.SseEmitterRegistry;
import com.beyond.ordersystem.member.service.SseAlarmService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sse")
public class SseController {
    private final SseAlarmService sseAlarmService;
    private final SseEmitterRegistry sseEmitterRegistry;

    @GetMapping("/connect")
    public SseEmitter subscribe() {
        SseEmitter sseEmitter = new SseEmitter(14400 * 60 * 1000L); // 10일 정도 Emitter 유효기간 설정, 14400분 : 10일, SseEmitter에서 get요청을 보내는 순간 사용자 연결정보가 알아서 만들어짐
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        sseEmitterRegistry.addSseEmitter(email, sseEmitter);
        try {
            sseEmitter.send(SseEmitter.event().name("connect").data("연결완료"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sseEmitter;
    }

    @GetMapping("/disconnect") // 로그아웃 버튼을 누르는 순간 사용자와의 연결 단절
    public void unSubscribe(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        sseEmitterRegistry.removeEmitter(email);
    }
}

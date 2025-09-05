package com.beyond.ordersystem.member.service;

import com.beyond.ordersystem.common.service.SseEmitterRegistry;
import com.beyond.ordersystem.common.dto.SseMessageDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Component

public class SseAlarmService implements MessageListener {

    private final SseEmitterRegistry sseEmitterRegistry;
    private final RedisTemplate<String, String> redisTemplate;
//    RedisTemplate를 사용하기 위해, @Qualifier를 사용하기 위해 생성자 주입방식으로 진행, @RequiredArgs X

    public SseAlarmService(SseEmitterRegistry sseEmitterRegistry, @Qualifier("ssePubSub") RedisTemplate<String, String> redisTemplate) {
        this.sseEmitterRegistry = sseEmitterRegistry;
        this.redisTemplate = redisTemplate;
    }

    //    특정 사용자에게 Message 발송,
// productId를 주겠다 인건데, 우리가 원하는대로 커스텀 가능, 알림메세지 부분임
    public void publishMessage(String sender, String reciever, Long orderingId) {
        SseMessageDto sseMessageDto = SseMessageDto.builder()
                .sender(sender)
                .receiver(reciever)
                .orderingId(orderingId)
                .build();
        ObjectMapper objectMapper = new ObjectMapper();
        String data = null;
        try {
            data = objectMapper.writeValueAsString(sseMessageDto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

//        emitter객체를 통해 메시지 전송
        SseEmitter sseEmitter = sseEmitterRegistry.getEmitter(reciever);
//        emitter 객체가 현재 서버에 있으면 직접 알림 발송, 그렇지 않으면 Redis에 publish
        if (sseEmitter != null) {
            try {
                sseEmitter.send(SseEmitter.event().name("ORDERED").data(data));
            } catch (IOException e) {
//            알림 줄 사용자 못 찾았다고 에러 터치는 건 프로그램이 말이 안됨, 로그만 남기고 넘어가도록 하기
                e.printStackTrace();
            }
        } else {
            redisTemplate.convertAndSend("order-channel", data); // (channel, data) channel은 redis의 가상의 공간 != redis의 16개 DB
        }
//    사용자가 로그아웃(또는 새로고침) 후 다시 화면에 들어왔을 때 알림메세지가 남아있으려면 DB에 추가적으로 저장 필요

    }


    @Override
    public void onMessage(Message message, byte[] pattern) {
//        Message : 실질적인 데이터가 담겨 있는 객체 (여기선 data(dto 조립))
//        pattern : 채널명
        String channelName = new String(pattern);
//        여러 개의 채널명을 구독하고 있을 경우, 채널명을 분기처리
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            SseMessageDto sseMessageDto = objectMapper.readValue(message.getBody(), SseMessageDto.class);
            SseEmitter sseEmitter = sseEmitterRegistry.getEmitter(sseMessageDto.getReceiver());
            if (sseEmitter != null) {
                try {
                    sseEmitter.send(SseEmitter.event().name("ORDERED").data(sseMessageDto));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}

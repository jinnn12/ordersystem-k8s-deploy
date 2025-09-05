package com.beyond.ordersystem.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class SseMessageDto {
    private String sender;
    private String receiver;
    private Long orderingId; // 몇개 들어왔는지 까지 알려주면 좋지 않을까?

}

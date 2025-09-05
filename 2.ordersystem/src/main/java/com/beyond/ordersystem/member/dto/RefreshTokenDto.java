package com.beyond.ordersystem.member.dto;

import jdk.jfr.Name;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
// param 방식으로 가는 게 편해보이나,, 보안이 중요하므로 body로, 헤더엔 at가 존재함
public class RefreshTokenDto {
    private String refreshToken;
}

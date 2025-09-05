package com.beyond.ordersystem.member.dto;

import com.beyond.ordersystem.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class MemberResponseDto {
    private Long id;
    private String name;
    private String email;

    public static MemberResponseDto fromEntity(Member member) {
        return MemberResponseDto.builder()
                .id(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .build();
    }
}

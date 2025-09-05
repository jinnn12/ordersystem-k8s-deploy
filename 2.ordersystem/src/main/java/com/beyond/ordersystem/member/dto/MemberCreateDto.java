package com.beyond.ordersystem.member.dto;

import com.beyond.ordersystem.member.domain.Member;
import com.beyond.ordersystem.member.domain.Role;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class MemberCreateDto {
    @NotEmpty
    private String name;
    @NotEmpty
    private String password;
    @NotEmpty
    private String email;

    public Member toEntity(String encodedPassword) {
        return Member.builder()
                .name(this.name)
                .email(this.email)
                .password(encodedPassword)
                .role(Role.USER)
                .build();
    }
}

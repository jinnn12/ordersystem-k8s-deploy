package com.beyond.ordersystem.member.domain;

import com.beyond.ordersystem.common.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
// jpql을 제외하고 모든 조회 쿼리에 where del_yn = "N" 붙이는 효과 발생
@Where(clause = "del_yn = 'N'")

public class Member extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private String password;
    @Builder.Default
    private String delYn = "N";
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    public void updateDelYn(String newDelYn) {
        this.delYn = newDelYn;
    }



}

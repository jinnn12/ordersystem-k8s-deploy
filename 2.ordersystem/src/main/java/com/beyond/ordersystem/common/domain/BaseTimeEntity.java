package com.beyond.ordersystem.common.domain;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

// 기본적으로 Entity는 상속이 불가능하다, 특정 어노테이션이 필요하다.
// @MappedSuperclass 사용 시 상속 가능.
// @Getter도 필요!
@MappedSuperclass
@Getter
public class BaseTimeEntity {
    @CreationTimestamp // 생성될 때 시간이 자동으로 찍힘
    private LocalDateTime createdTime;
    @UpdateTimestamp // 수정될 때 시간이 자동으로 찍힘
    private LocalDateTime updatedTime;

}

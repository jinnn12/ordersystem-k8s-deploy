package com.beyond.ordersystem.ordering.domain;

import com.beyond.ordersystem.common.domain.BaseTimeEntity;
import com.beyond.ordersystem.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter

public class Ordering extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OrderStatus orderStatus = OrderStatus.ORDERED;



    @OneToMany(mappedBy = "ordering", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST, orphanRemoval = true)
    @Builder.Default
    private List<OrderDetail> orderDetailList = new ArrayList<>();

    public void cancelStatus() {
        this.orderStatus = OrderStatus.CANCELLED;
    }
}

package com.example.ordersystem.ordering.domain;

import com.example.ordersystem.common.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Entity
@Builder
public class Ordering extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OrderStatus orderStatus = OrderStatus.ORDERED;

    /*
        - member와 ordering은 관계성을 가질 수 없다.
        방법은 2가지 있다.
        1. id값을 가져온다. 하지만 member에 다시 물어봐야 되기 때문에 패스.
        2. email을 가져온다. 토큰에 email을 가져오기 때문에 좀 더 간편하다.
     */
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "member_id")
//    private Member member;
    private String memberEmail;

    @OneToMany(mappedBy = "ordering", cascade = CascadeType.PERSIST)  // cascading을 위한 연결
    @Builder.Default
    List<OrderingDetail> orderingDetailList = new ArrayList<>();

    // 주문취소
    public void cancelStatus() {
        // 이미 취소된 주문인지 확인
        if (this.orderStatus == OrderStatus.CANCELED) {
            throw new IllegalStateException("이미 취소된 주문입니다.");
        }
        // 상태 변경
        this.orderStatus = OrderStatus.CANCELED;
    }

}

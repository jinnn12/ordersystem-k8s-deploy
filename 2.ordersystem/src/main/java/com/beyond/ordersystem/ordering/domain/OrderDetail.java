package com.beyond.ordersystem.ordering.domain;

import com.beyond.ordersystem.common.domain.BaseTimeEntity;
import com.beyond.ordersystem.product.domain.Product;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter

public class OrderDetail extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    어떤 주문에서
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordering_id")
    private Ordering ordering;

//    어떤 상품을
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

//    몇개
    private Integer quantity;




}

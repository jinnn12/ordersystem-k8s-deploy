package com.beyond.ordersystem.product.domain;

import com.beyond.ordersystem.common.domain.BaseTimeEntity;
import com.beyond.ordersystem.member.domain.Member;
import com.beyond.ordersystem.product.dto.ProductUpdateDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor

public class Product extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String category;
    private Integer price;
    private Integer stockQuantity;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;
    private String imagePath;

    public void updateImageUrl(String url) {
        this.imagePath = url;
    }

    public void updateStockQuantity(int orderQuantity) {
        this.stockQuantity = this.stockQuantity - orderQuantity;
    }
    public void cancelOrder(int orderQuantity) {
        this.stockQuantity = this.stockQuantity + orderQuantity;
    }

}

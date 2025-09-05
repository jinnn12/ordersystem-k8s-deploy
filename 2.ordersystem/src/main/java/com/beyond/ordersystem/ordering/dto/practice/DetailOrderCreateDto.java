package com.beyond.ordersystem.ordering.dto.practice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class DetailOrderCreateDto {
    private Long storeId;
    private String payment;
    private List<ProductDetailDto> details;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class ProductDetailDto{
        private Long productId;
        private Integer productCount;
    }
}

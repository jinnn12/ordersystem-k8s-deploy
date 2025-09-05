package com.beyond.ordersystem.ordering.dto;

import com.beyond.ordersystem.ordering.domain.OrderDetail;
import com.beyond.ordersystem.ordering.domain.Ordering;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class OrderCreateDto {
    private Long productId;
    private Integer productCount;

    }




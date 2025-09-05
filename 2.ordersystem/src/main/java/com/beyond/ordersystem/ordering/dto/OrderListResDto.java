package com.beyond.ordersystem.ordering.dto;

import com.beyond.ordersystem.member.domain.Member;
import com.beyond.ordersystem.ordering.domain.OrderDetail;
import com.beyond.ordersystem.ordering.domain.OrderStatus;
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

public class OrderListResDto {
    private Long id;
    private String memberEmail;
    private OrderStatus orderStatus;
    private List<OrderDetailResDto> orderDetails;

    public static OrderListResDto fromEntity(Ordering ordering) {
        List<OrderDetailResDto> orderDetailResDtoList = new ArrayList<>();
        for (OrderDetail orderDetail : ordering.getOrderDetailList()) {
            orderDetailResDtoList.add(OrderDetailResDto.fromEntity(orderDetail));
        }
        OrderListResDto dto = OrderListResDto.builder()
                .id(ordering.getId())
                .memberEmail(ordering.getMember().getEmail())
                .orderStatus(ordering.getOrderStatus())
                .orderDetails(orderDetailResDtoList)
                .build();
        return dto;

//    public static OrderListResDto fromEntity(Ordering ordering) {
//        List<OrderListResDto> orderListResDtoList = new ArrayList<>();
//        for (OrderDetail orderDetail : ordering.getOrderDetailList()) {
//            OrderDetailResDto orderDetailResDto =
//            orderDetailResDtoList.add(OrderDetailResDto.fromEntity(orderDetail));
//        }
//        OrderListResDto dto = OrderListResDto.builder()
//                .id(ordering.getId())
//                .memberEmail(ordering.getMember().getEmail())
//                .orderStatus(ordering.getOrderStatus())
//                .orderDetails(orderDetailResDtoList)
//                .build();
//        return dto;
//    }
    }
}

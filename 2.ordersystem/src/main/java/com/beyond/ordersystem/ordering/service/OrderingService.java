package com.beyond.ordersystem.ordering.service;

import com.beyond.ordersystem.member.domain.Member;
import com.beyond.ordersystem.member.repository.MemberRepository;
import com.beyond.ordersystem.member.service.SseAlarmService;
import com.beyond.ordersystem.ordering.domain.OrderDetail;
import com.beyond.ordersystem.ordering.domain.OrderStatus;
import com.beyond.ordersystem.ordering.domain.Ordering;
import com.beyond.ordersystem.ordering.dto.OrderCreateDto;
import com.beyond.ordersystem.ordering.dto.OrderDetailResDto;
import com.beyond.ordersystem.ordering.dto.OrderListResDto;
import com.beyond.ordersystem.ordering.repository.OrderDetailRepository;
import com.beyond.ordersystem.ordering.repository.OrderingRepository;
import com.beyond.ordersystem.product.domain.Product;
import com.beyond.ordersystem.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderingService {
    private final OrderingRepository orderingRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final SseAlarmService sseAlarmService;

    public Long create(List<OrderCreateDto> orderCreateDtoList) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("사용자 없음"));

//        부모객체를 먼저 만드는 것이 좋다 (왜 멤버만 builder 세팅하는거지??, 멤버 빼고 다 초기화 됐음)
        Ordering ordering = Ordering.builder()
                .member(member)
                .build();

        for (OrderCreateDto orderCreateDto : orderCreateDtoList) { // {{"productId":"1", "productCount":"3"}, {...}, ...}
            Product product = productRepository.findById(orderCreateDto.getProductId()).orElseThrow(() -> new EntityNotFoundException("product is not found"));
            if (product.getStockQuantity() < orderCreateDto.getProductCount()) {
//                예외를 강제 발생시킴으로써 임시저장된 사항들 전체 rollback 처리
                throw new IllegalArgumentException("재고 부족");
            }
/*          1. 동시에 접근하는 상황에서 update값의 정합성이 깨지고 갱신이상(lost update)이 발생
            2. Spring 버전, mySql 버전에 따라 Jpa에서 강제에러(Deadlock, 교착상태)를 유발시켜 대부분의 요청실패 발생
        - 우리 버전에서는 10~20개 정도만 주문이 들어가고, 나머지는 스프링에서 Deadlock 발생 / redis에서 재고관리를 하는 것이 어떨까*/
            product.updateStockQuantity(orderCreateDto.getProductCount()); /* 동시성이슈발생 */

            OrderDetail orderDetail = OrderDetail.builder()
                    .ordering(ordering)
                    .product(product)
                    .quantity(orderCreateDto.getProductCount())
                    .build();
//            1. orderDetailRepository.save(orderDetail); 원론적으로 repository에 저장하는 방법
            ordering.getOrderDetailList().add(orderDetail); // 부모 저장할때 자식까지 저장하는 cascading 코드
        }
        orderingRepository.save(ordering);
//        주문 성공시 admin 유저에게 알림메시지 전송
        sseAlarmService.publishMessage(email, "admin@naver.com", ordering.getId()); // ordering.getMember().getEmail() == email / sender, receiver 동적설계 해야함

//        큐에 메세지를 담는다(RabbitMQ 안에 update Rdb에 하라고)
        return ordering.getId();
    }

    public List<OrderListResDto> findAll() {
        List<Ordering> orderingList = orderingRepository.findAll();
        List<OrderListResDto> orderListResDtoList = new ArrayList<>();
        for (Ordering ordering : orderingList) {
            List<OrderDetail> orderDetailList = ordering.getOrderDetailList();
            List<OrderDetailResDto> orderDetailResDtoList = new ArrayList<>();
            for (OrderDetail orderDetail : orderDetailList) {
//                OrderDetailResDto orderDetailResDto = OrderDetailResDto.builder()
//                        .detailId(orderDetail.getId())
//                        .productName(orderDetail.getProduct().getName())
//                        .productCount(orderDetail.getQuantity())
//                        .build();
//                orderDetailResDtoList.add(orderDetailResDto);
                OrderDetailResDto orderDetailResDto = OrderDetailResDto.fromEntity(orderDetail);
                orderDetailResDtoList.add(orderDetailResDto);
            }

            OrderListResDto dto = OrderListResDto.builder()
                    .id(ordering.getId())
                    .memberEmail(ordering.getMember().getEmail())
                    .orderStatus(ordering.getOrderStatus())
                    .orderDetails(orderDetailResDtoList)
                    .build();
            OrderListResDto dto1 = OrderListResDto.fromEntity(ordering);

            orderListResDtoList.add(dto);
        }
        return orderListResDtoList;
//        return orderingRepository.findAll().stream().map(ordering -> OrderListResDto.fromEntity(ordering)).collect(Collectors.toList());

    }

    public List<OrderListResDto> myOrders() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("없음"));
        return orderingRepository.findAllByMember(member).stream().map(ordering -> OrderListResDto.fromEntity(ordering)).collect(Collectors.toList());
    }



    public Ordering cancel(Long id) {
//        Ordering의 상태값 변경 -> CANCELLED
        Ordering ordering = orderingRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("삭제하고자 하는 상품이 없음"));
        ordering.cancelStatus();
        return ordering;
    }
}

package com.beyond.ordersystem.ordering.controller;

import com.beyond.ordersystem.common.dto.CommonDto;
import com.beyond.ordersystem.ordering.domain.Ordering;
import com.beyond.ordersystem.ordering.dto.OrderListResDto;
import com.beyond.ordersystem.ordering.dto.OrderCreateDto;
import com.beyond.ordersystem.ordering.service.OrderingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/ordering")
@RequiredArgsConstructor

public class OrderingController {
    //    @PostMapping("/detailCreate")
//    public ResponseEntity<?> create(@RequestBody DetailOrderCreateDto detailOrderCreateDtos) {
//        System.out.println(detailOrderCreateDtos);
//        return null;
//    }
    /*-------------------------------------------------------------------------------------------------------------------------*/
    private final OrderingService orderingService;

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody List<OrderCreateDto> orderCreateDtos) { // dto가 단건이 아니라 여러개이므로 List로 컨트롤러에서 받아주기, dto에서 처리하기보단,,,
        Long id = orderingService.create(orderCreateDtos);
        return ResponseEntity.status(HttpStatus.OK)
                .body(CommonDto.builder()
                        .result(id)
                        .statusCode(HttpStatus.OK.value())
                        .statusMessage("주문 완료")
                        .build());
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')") // ?? 일단 막 박음
    public ResponseEntity<?> findAll() {
        List<OrderListResDto> orderListResDtos = orderingService.findAll();
        return ResponseEntity.status(HttpStatus.OK)
                .body(CommonDto.builder()
                        .result(orderListResDtos)
                        .statusCode(HttpStatus.OK.value())
                        .statusMessage("Ordering List is found")
                        .build());
    }

    @GetMapping("/myorders")
    public ResponseEntity<?> myOrders() {
        List<OrderListResDto> orderListResDtos = orderingService.myOrders();
        return ResponseEntity.status(HttpStatus.OK)
                .body(CommonDto.builder()
                        .result(orderListResDtos)
                        .statusCode(HttpStatus.OK.value())
                        .statusMessage("내 주문 목록 조회 성공")
                        .build());
    }

    @DeleteMapping("/cancel/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> orderCancel(@PathVariable Long id) {
        Ordering ordering = orderingService.cancel(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(CommonDto.builder()
                        .result(ordering.getId())
                        .statusCode(HttpStatus.OK.value())
                        .statusMessage("주문 취소 성공")
                        .build());
    }
}

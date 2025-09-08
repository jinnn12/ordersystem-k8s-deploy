package com.example.ordersystem.ordering.feignclient;

import com.example.ordersystem.common.dto.CommonDto;
import com.example.ordersystem.ordering.dto.OrderCreateDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

// name = eureka에 등록된 application.name을 의미
// url = k8s의 service명이다.
@FeignClient(name = "product-service", url="http://product-service")  
public interface ProductFeignClient {
    // product-service로 보낼 요청들 생성

    @GetMapping("/product/detail/{productId}")
    CommonDto getProductById(@PathVariable Long productId);

    @PutMapping("/product/updatestock")
    void updateProductStockQuantity(@RequestBody OrderCreateDto dto);

}

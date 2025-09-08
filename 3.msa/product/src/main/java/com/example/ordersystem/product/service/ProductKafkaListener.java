package com.example.ordersystem.product.service;

import com.example.ordersystem.product.dto.ProductUpdateStockDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductKafkaListener {

    private final ProductService productService;

    @KafkaListener(topics = "stock-update-topic", containerFactory = "kafkaListener")
    public void stockConsumer(String message) throws JsonProcessingException {
        System.out.println("컨슈머 메시지 수신 : " + message);
        ObjectMapper objectMapper = new ObjectMapper();
        ProductUpdateStockDTO dto = objectMapper.readValue(message, ProductUpdateStockDTO.class);
        productService.updateStock(dto);
    }

    @KafkaListener(topics = "member-update-topic", containerFactory = "kafkaListener")
    public void memberConsumer(String message) {

    }


}

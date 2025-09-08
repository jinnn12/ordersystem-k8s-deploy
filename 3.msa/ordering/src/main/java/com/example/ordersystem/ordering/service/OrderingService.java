package com.example.ordersystem.ordering.service;

import com.example.ordersystem.common.dto.CommonDto;
import com.example.ordersystem.common.service.SseAlarmService;
import com.example.ordersystem.ordering.domain.Ordering;
import com.example.ordersystem.ordering.domain.OrderingDetail;
import com.example.ordersystem.ordering.dto.OrderCreateDto;
import com.example.ordersystem.ordering.dto.OrderListResDto;
import com.example.ordersystem.ordering.dto.ProductDto;
import com.example.ordersystem.ordering.feignclient.ProductFeignClient;
import com.example.ordersystem.ordering.repository.OrderDetailRepository;
import com.example.ordersystem.ordering.repository.OrderingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderingService {
    private final OrderingRepository orderingRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final SseAlarmService sseAlarmService;
    private final RestTemplate restTemplate;
    private final ProductFeignClient productFeignClient;
    private final KafkaTemplate<String, Object> kafkaTemplate; // String은 topic이 된다.

    // 주문
    public Long create(List<OrderCreateDto> orderCreateDtoList, String memberEmail) {
        Ordering ordering = Ordering.builder()
                .memberEmail(memberEmail)
                .build();

        for (OrderCreateDto dto : orderCreateDtoList) {
            /// == 1. 상품 조회 ==
            // url 만들기
            String productDetailUrl = "http://product-service/product/detail/" + dto.getProductId();
            // 서버간 요청 헤더 만들기
            HttpHeaders headers = new HttpHeaders();
//            headers.set("X-User-Email", memberEmail);
            HttpEntity<String> httpEntity = new HttpEntity<>(headers);
            ResponseEntity<CommonDto> resDTO = restTemplate.exchange(productDetailUrl, HttpMethod.GET, httpEntity, CommonDto.class);
            // 응답 받아오기 -> 아직은 commonDto의 알맹이들은 json형태. 왜? result의 형태가 Object이기 때문에.
            CommonDto commonDto = resDTO.getBody();
            ObjectMapper objectMapper = new ObjectMapper();
            // readValue: String -> 클래스 변환.
            // convertValue: Object 클래스 -> 클래스 변환.
            ProductDto product = objectMapper.convertValue(commonDto.getResult(), ProductDto.class);

            /// == 2. 주문 발생 ==
            // 재고 부족할 경우
            if (product.getStockQuantity() < dto.getProductCount()) {
                // 예외를 강제 발생시킴으로서, 모두 임시저장사항들은 rollback 처리
                throw new IllegalArgumentException("재고가 부족합니다.");
            }

            OrderingDetail orderingDetail = OrderingDetail.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .quantity(dto.getProductCount())
                    .ordering(ordering)
                    .build();
//            orderDetailRepository.save(orderingDetail);
            ordering.getOrderingDetailList().add(orderingDetail);

            /// == 3. 재고 감소 ==
            // (동기성) 재고 감소
            String productUpdateStockUrl = "http://product-service/product/updatestock";
            HttpHeaders stockHeaders = new HttpHeaders();
            // 바디가 어떤 타입인지 명시해줘야 된다.
            stockHeaders.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<OrderCreateDto> updateStockEntity = new HttpEntity<>(dto, stockHeaders);
            restTemplate.exchange(productUpdateStockUrl, HttpMethod.PUT, updateStockEntity, Void.class);

        }
        orderingRepository.save(ordering);

        // 주문성공 시 admin에게 알림메시지 전송
        // email체계로 던져줌(admin, 주문자, 주문id)
        sseAlarmService.publishMessage("admin@naver.com", memberEmail, ordering.getId());

        return ordering.getId();
    }

    // fallback 메서드는 원본 메서드의 매개변수와 정확히 일치해야함.
    public void fallbackProductServiceCircuit(
            List<OrderCreateDto> orderCreateDtoList, String email, // createFeignKafka메서드의 인자값과 일치.
            Throwable throwable // 에러메시지가 담겨있음.
    ) {
        throw new RuntimeException("상품서버 응답없음. 나중에 다시 시도해주세요.");
    }


    /** kafka 사용하여 주문 */
    /**
     * CircuitBreaker 테스트 : 4~5번의 정상요청 -> 5번 중에 2번의 지연발생 -> circuit open -> 그 다음 요청은 바로 fallback
     */
    @CircuitBreaker(name = "productServiceCircuit", fallbackMethod = "fallbackProductServiceCircuit") // fallbackMethod은 발동될 예외를 지정.
    public Long createFeignKafka(List<OrderCreateDto> orderCreateDtoList, String email) {

        Ordering ordering = Ordering.builder()
                .memberEmail(email)
                .build();

        for (OrderCreateDto dto : orderCreateDtoList) {
            /* 상품 조회 */
            // feign 클라이언트를 사용한 동기적 상품 조회
            CommonDto commonDto = productFeignClient.getProductById(dto.getProductId());    // productId값만 꺼내주면 CommonDto 클래스로 받을 수 있음
            ObjectMapper objectMapper = new ObjectMapper();

            ProductDto product = objectMapper.convertValue(commonDto.getResult(), ProductDto.class);

            // 재고 부족할 경우
            if (product.getStockQuantity() < dto.getProductCount()) {
                // 예외를 강제 발생시킴으로서, 모두 임시저장사항들은 rollback 처리
                throw new IllegalArgumentException("재고가 부족합니다.");
            }

            /* 주문 생성 */
            OrderingDetail orderingDetail = OrderingDetail.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .quantity(dto.getProductCount())
                    .ordering(ordering)
                    .build();
            ordering.getOrderingDetailList().add(orderingDetail);

            /* 재고감소 */
//            // feign을 통한 동기적 재고감소 요청
//            productFeignClient.updateProductStockQuantity(dto);

            // kafka를 사활용한 비동기적 재고감소 요청
            kafkaTemplate.send("stock-update-topic", dto);


        }
        orderingRepository.save(ordering);

        // 주문성공 시 admin에게 알림메시지 전송
        sseAlarmService.publishMessage("admin@naver.com", email, ordering.getId());  // email체계로 던져줌(admin, 주문자, 주문id)

        return ordering.getId();

    }



    // 주문목록조회
    public List<OrderListResDto> findAll() {
        return orderingRepository.findAll().stream()
                .map(OrderListResDto::fromEntity).collect(Collectors.toList());
    }

    // 내주문목록조회
    public List<OrderListResDto> findAllByMember(String email) {
        return orderingRepository.findAllByMemberEmail(email).stream()
                .map(OrderListResDto::fromEntity).collect(Collectors.toList());
    }


}

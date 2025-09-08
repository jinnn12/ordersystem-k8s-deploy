package com.example.ordersystem.product.controller;

import com.example.ordersystem.common.dto.CommonDto;
import com.example.ordersystem.product.dto.*;
import com.example.ordersystem.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@SpringBootApplication
@RestController
@RequiredArgsConstructor
@RequestMapping("/product")
public class ProductController {
    private final ProductService productService;

    // 상품등록
    @PostMapping("/create")
    public ResponseEntity<?> create(
            @ModelAttribute ProductCreateDto productCreateDto,
            @RequestHeader("X-User-Email") String email

    ){
        Long id = productService.save(productCreateDto, email);
        return  new ResponseEntity<>(
                CommonDto.builder()
                        .result(id)
                        .status_code(HttpStatus.CREATED.value())
                        .status_message("상품등록완료")
                        .build(),
                HttpStatus.CREATED
        );
    }

    // 상품목록조회
    @GetMapping("/list")
    public ResponseEntity<?> findAll(Pageable pageable, ProductSearchDto productSearchDto){
        Page<ProductResDto> productResDtoList = productService.findAll(pageable, productSearchDto);
        return  new ResponseEntity<>(
                CommonDto.builder()
                        .result(productResDtoList)
                        .status_code(HttpStatus.OK.value())
                        .status_message("상품목록조회성공")
                        .build(),
                HttpStatus.OK
        );
    }

    // 상품상세조회
    @GetMapping("/detail/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) throws InterruptedException {
        Thread.sleep(3000L); // 의도적 3초 예외발생.
        ProductResDto productResDto = productService.findById(id);
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(productResDto)
                        .status_code(HttpStatus.OK.value())
                        .status_message("상품상세조회성공")
                        .build(),
                HttpStatus.OK
        );
    }

    // 상품수정
    @PutMapping("/update/{productId}")
    public ResponseEntity<?> update(@ModelAttribute ProductUpdateDto productUpdateDto, @PathVariable Long productId) {
        Long id = productService.update(productUpdateDto, productId);
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(id)
                        .status_code(HttpStatus.CREATED.value())
                        .status_message("상품변경완료")
                        .build(),
                HttpStatus.OK
        );
    }

    // 상품수정
    @PutMapping("/updatestock")
    public ResponseEntity<?> updateStock(
            @RequestBody ProductUpdateStockDTO reqDTO
            ) {
        Long id = productService.updateStock(reqDTO);
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(id)
                        .status_code(HttpStatus.OK.value())
                        .status_message("재고수량 변경 완료")
                        .build(),
                HttpStatus.OK
        );
    }
}


package com.beyond.ordersystem.product.controller;

import com.beyond.ordersystem.common.dto.CommonDto;
import com.beyond.ordersystem.product.dto.ProductCreateDto;
import com.beyond.ordersystem.product.dto.ProductResDto;
import com.beyond.ordersystem.product.dto.ProductSearchDto;
import com.beyond.ordersystem.product.dto.ProductUpdateDto;
import com.beyond.ordersystem.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;
import java.util.*;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> save(@ModelAttribute ProductCreateDto productCreateDto) {
        Long id = productService.save(productCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonDto.builder()
                        .result(id)
                        .statusCode(HttpStatus.CREATED.value())
                        .statusMessage("product save is completed")
                        .build());
    }

    @GetMapping("/list")
    public ResponseEntity<?> findAll(Pageable pageable, ProductSearchDto productSearchDto) {
        Page<ProductResDto> productResDtoList = productService.findAll(pageable, productSearchDto);
        return ResponseEntity.status(HttpStatus.OK)
                .body(CommonDto.builder()
                        .result(productResDtoList)
                        .statusCode(HttpStatus.OK.value())
                        .statusMessage("list is found")
                        .build());
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        ProductResDto productResDto = productService.findById(id);
        return ResponseEntity.status(HttpStatus.FOUND)
                .body(CommonDto.builder()
                        .result(productResDto)
                        .statusCode(HttpStatus.FOUND.value())
                        .statusMessage("product is found")
                        .build());
    }

    @PutMapping("/update/{productId}")
    public ResponseEntity<?> updateProduct(@PathVariable Long productId, @ModelAttribute ProductUpdateDto productUpdateDto) {
        Long id = productService.updateProduct(productUpdateDto, productId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(CommonDto.builder()
                        .result(id)
                        .statusCode(HttpStatus.OK.value())
                        .statusMessage("수정이 완료되었습니다.")
                        .build());
    }
}

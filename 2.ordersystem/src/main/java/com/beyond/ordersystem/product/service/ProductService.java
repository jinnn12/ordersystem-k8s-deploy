package com.beyond.ordersystem.product.service;

import com.beyond.ordersystem.member.domain.Member;
import com.beyond.ordersystem.member.repository.MemberRepository;
import com.beyond.ordersystem.product.domain.Product;
import com.beyond.ordersystem.product.dto.ProductCreateDto;
import com.beyond.ordersystem.product.dto.ProductResDto;
import com.beyond.ordersystem.product.dto.ProductSearchDto;
import com.beyond.ordersystem.product.dto.ProductUpdateDto;
import com.beyond.ordersystem.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor

public class ProductService {
    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final S3Client s3Client;


    public Long save(ProductCreateDto productCreateDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("유효하지 않은 사용자입니다."));
        Product product = productCreateDto.toEntity(member);
        productRepository.save(product);
        // s3
        // image명 설정
        System.out.println(productCreateDto);
        if (productCreateDto.getProductImage() != null) {
            String fileName = "product-" + product.getId() + "-profileImage-" + productCreateDto.getProductImage().getOriginalFilename();

            // 저장 객체 구성
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(productCreateDto.getProductImage().getContentType()) //image/jpeg, video/mp4...
                    .build();

            // 이미지를 업로드(byte형태로)
            try {
                s3Client.putObject(putObjectRequest, RequestBody.fromBytes(productCreateDto.getProductImage().getBytes()));
            } catch (IOException e) {
                // checked -> unchecked로 바꿔 전체 rollback되도록 예외처리
                throw new IllegalArgumentException("이미지 업로드 실패");
            }

//            이미지 삭제시
//            s3Client.deleteObject(a -> a.bucket(bucketName).key(fileName));

            // 이미지 url 추출
            String imgUrl = s3Client.utilities().getUrl(a -> a.bucket(bucketName).key(fileName)).toExternalForm();
            product.updateImageUrl(imgUrl);
        }
//        상품 등록 시 redis에 재고 세팅
        return product.getId();
    }

    public Long updateProduct(ProductUpdateDto productUpdateDto, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("찾는 상품이 없습니다."));        // s3
        // image명 설정
        if (productUpdateDto.getProductImage() != null && !productUpdateDto.getProductImage().isEmpty()) {
//            기존 이미지 삭제 : 파일명으로 삭제
            String imgUrl = product.getImagePath();
            String fileName = imgUrl.substring(imgUrl.lastIndexOf("/") + 1); // '/' 이후의 값들만 잘라내겠다
            s3Client.deleteObject(a -> a.bucket(bucketName).key(fileName));

//            신규 이미지 등록
            String newFileName = "product-" + product.getId() + "-profileImage-" + productUpdateDto.getProductImage().getOriginalFilename();

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(newFileName)
                    .contentType(productUpdateDto.getProductImage().getContentType()) //image/jpeg, video/mp4...
                    .build();

//            이미지를 업로드(byte형태로)
            try {
                s3Client.putObject(putObjectRequest, RequestBody.fromBytes(productUpdateDto.getProductImage().getBytes()));
            } catch (IOException e) {
                // checked -> unchecked로 바꿔 전체 rollback되도록 예외처리
                throw new IllegalArgumentException("이미지 업로드 실패");
            }

            // 신규 이미지 url 업데이트
            String newImgUrl = s3Client.utilities().getUrl(a -> a.bucket(bucketName).key(newFileName)).toExternalForm();
            product.updateImageUrl(newImgUrl);
        } else {
//            s3에서 이미지 삭제 후 url 갱신
            product.updateImageUrl(null); // 사용자가 이미지를 안 넣으면 삭제처리를 한 것으로 취급하기 위함
        }
        return product.getId();
    }


    public String updateProduct1(ProductUpdateDto productUpdateDto, Long id) {
        Product product = productUpdateDto.toEntity();
        Product findProduct = productRepository.findById(product.getId()).orElseThrow(() -> new EntityNotFoundException("찾는 상품이 없습니다."));
        s3Client.deleteObject(a -> a.bucket(bucketName).key(findProduct.getName()));
        String imgUrl = s3Client.utilities().getUrl(a -> a.bucket(bucketName).key(findProduct.getName())).toExternalForm();
        findProduct.updateImageUrl(imgUrl);
        return imgUrl;
    }

    public Page<ProductResDto> findAll(Pageable pageable, ProductSearchDto productSearchDto) {
        Specification<Product> specification = new Specification<Product>() {
            @Override // 검색할 때 Dto를 어떻게 설계할지, query를 어떻게 짤지 고민하는 것이 더좋음
            public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
//                Root 객체 : 엔티티의 속성을 접근하기 위한 객체, CriteriaBuilder 객체 : 쿼리를 생성하기 위한 객체, Predicate : & 조건 & 조건 .. 이런거
                List<Predicate> predicateList = new ArrayList<>(); // 검색조건을 List에 하나씩 담아보자

                if (productSearchDto.getCategory() != null) {
                    predicateList.add(criteriaBuilder.equal(root.get("category"), productSearchDto.getCategory()));
                }
                if (productSearchDto.getProductName() != null) {
                    predicateList.add(criteriaBuilder.like(root.get("name"), "%" + productSearchDto.getProductName() + "%"));
                }
                Predicate[] predicateArr = new Predicate[predicateList.size()];
                for (int i = 0; i < predicateList.size(); i++) {
                    predicateArr[i] = predicateList.get(i);
                }
//                위의 검색 조건들을 하나(한줄)의 Predicate객체로 만들어서 return
                Predicate predicate = criteriaBuilder.and(predicateArr);
                return predicate;
            }
        };
        Page<Product> productList = productRepository.findAll(specification, pageable);
        return productList.map(a -> ProductResDto.fromEntity(a));

//        List<Product> productList = productRepository.findAll();
//        return productList.stream().map(p -> ProductResDto.fromEntity(p)).collect(Collectors.toList());
    }

    public ProductResDto findById(Long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("찾는 상품이 없음"));
        return ProductResDto.fromEntity(product);
    }


}

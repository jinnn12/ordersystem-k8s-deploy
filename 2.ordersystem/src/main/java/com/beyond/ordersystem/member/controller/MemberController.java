package com.beyond.ordersystem.member.controller;

import com.beyond.ordersystem.common.auth.JwtTokenProvider;
import com.beyond.ordersystem.common.dto.CommonDto;
import com.beyond.ordersystem.member.dto.*;
import com.beyond.ordersystem.member.domain.Member;
import com.beyond.ordersystem.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;

    //    회원가입
    @PostMapping("/create")
    public ResponseEntity<?> save(@Valid @RequestBody MemberCreateDto memberCreateDto) {
        Long id = memberService.save(memberCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonDto.builder()
                        .result(id)
                        .statusCode(HttpStatus.CREATED.value())
                        .statusMessage("회원가입 완료!")
                        .build());
    }

    @PostMapping("/doLogin")
    public ResponseEntity<?> doLogin(@Valid @RequestBody LoginRequestDto loginRequestDto) { // 성공하면 토큰을 줘야 함, 토큰 설계가 핵심
        Member member = memberService.doLogin(loginRequestDto);
//        at토큰 생성
        String accessToken = jwtTokenProvider.createAtToken(member); // member 넘기는 이유는 payload를 조립하기 위해
//        rt토큰 생성
        String refreshToken = jwtTokenProvider.createRtToken(member);

        LoginResponseDto loginResponseDto = LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        return ResponseEntity.status(HttpStatus.OK)
                .body(CommonDto.builder()
                        .result(loginResponseDto)
                        .statusCode(HttpStatus.OK.value())
                        .statusMessage("로그인 성공!")
                        .build());
    }

    //    rt를 통한 at 갱신 요청 api
    @PostMapping("/refresh-at")
    public ResponseEntity<?> generateNewAt(@RequestBody RefreshTokenDto refreshTokenDto) { // 사용자가 Rt를 들고 요청하는 것이므로 매개변수로 rt 사용
//        rt 검증 로직
        Member member = jwtTokenProvider.validateRt(refreshTokenDto.getRefreshToken());

//        at 신규 생성 로직
        String accessToken = jwtTokenProvider.createAtToken(member); // member 객체를 넣어야 하는데, at 토큰의 payload의 이메일을 차자서 넣기
        LoginResponseDto loginResponseDto = LoginResponseDto.builder()
                .accessToken(accessToken) // 갱신할 땐 at만 발급하니 rt는 필요없음
                .build();

        return ResponseEntity.status(HttpStatus.OK)
                .body(CommonDto.builder()
                        .result(loginResponseDto)
                        .statusCode(HttpStatus.OK.value())
                        .statusMessage("At 재발급 성공!")
                        .build());
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> findAll() {
        System.out.println("heeer");
        List<MemberResponseDto> memberResponseDtoList = memberService.findAll();
        return ResponseEntity.status(HttpStatus.OK)
                .body(CommonDto.builder()
                        .result(memberResponseDtoList)
                        .statusCode(HttpStatus.OK.value())
                        .statusMessage("find is successful")
                        .build());
    }

    @GetMapping("/myinfo")
    public ResponseEntity<?> myinfo() {
        MemberResponseDto memberResponseDto = memberService.getMyInfo();
        return ResponseEntity.status(HttpStatus.OK)
                .body(CommonDto.builder()
                        .result(memberResponseDto)
                        .statusCode(HttpStatus.OK.value())
                        .statusMessage("my info is found")
                        .build());
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> delete() {
        memberService.delete();
        return ResponseEntity.status(HttpStatus.OK)
                .body(CommonDto.builder()
                        .result("OK")
                        .statusCode(HttpStatus.OK.value())
                        .statusMessage("delete is complete")
                        .build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/detail/{id}")
    public ResponseEntity<?> memberDetail(@PathVariable Long id){
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(memberService.findById(id))
                        .statusCode(HttpStatus.OK.value())
                        .statusMessage("회원상세조회완료")
                        .build(),
                HttpStatus.OK);
    }


}

package com.beyond.ordersystem.member.service;

import com.beyond.ordersystem.member.domain.Member;
import com.beyond.ordersystem.member.dto.LoginRequestDto;
import com.beyond.ordersystem.member.dto.MemberCreateDto;
import com.beyond.ordersystem.member.dto.MemberResponseDto;
import com.beyond.ordersystem.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public Long save(MemberCreateDto memberCreateDto) {
        if (memberRepository.findByEmail(memberCreateDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 가입된 이메일 입니다.");
        }
        String encodedPassword = passwordEncoder.encode(memberCreateDto.getPassword());
        Member member = memberRepository.save(memberCreateDto.toEntity(encodedPassword));
        return member.getId();
    }

    public Member doLogin(LoginRequestDto loginRequestDto) {
        Optional<Member> optionalMember = memberRepository.findByEmail(loginRequestDto.getEmail());
        boolean check = true;
        if (optionalMember.isEmpty()) {
            check = false;
        } else {
            if (!passwordEncoder.matches(loginRequestDto.getPassword(), optionalMember.get().getPassword())) {
                check = false;
            }
            if (check = false) {
                throw new IllegalArgumentException("이메일 혹은 비밀번호가 같지 않습니다.");
            }
        }
        return optionalMember.get();
    }

    public List<MemberResponseDto> findAll() {
        List<Member> memberList = memberRepository.findAll();
        System.out.println(memberList);
        return memberList.stream().map(m -> MemberResponseDto.fromEntity(m)).collect(Collectors.toList());
    }

    public MemberResponseDto getMyInfo() {
//        로그인했다라는 가정(토큰이 있다라는 가정)하에 이 사람의 토큰의 authentication 이메일을 꺼내겠다
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("해당 유저가 존재하지 않습니다."));

        return MemberResponseDto.fromEntity(member);
    }

    public void delete() {
//        상단에 transactional이 있으므로 영속성 컨텍스트가 활용된다.
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("해당 유저가 존재하지 않습니다."));
        member.updateDelYn("Y");
    }

    public MemberResponseDto findById(Long id){
        Member member = memberRepository.findById(id).orElseThrow(()->new EntityNotFoundException("member is not found"));
        return MemberResponseDto.fromEntity(member);
    }
}

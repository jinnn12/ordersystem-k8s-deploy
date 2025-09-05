package com.beyond.ordersystem.common.auth;

import com.beyond.ordersystem.member.domain.Member;
import com.beyond.ordersystem.member.repository.MemberRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component


public class JwtTokenProvider {
    private final MemberRepository memberRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.expirationAt}") // "${}" : yml에 있는 파일 전체를 지칭
    private int expirationAt;
    @Value("${jwt.secretKeyAt}")
    private String secretKeyAt;

    @Value("${jwt.expirationRt}") // "${}" : yml에 있는 파일 전체를 지칭
    private int expirationRt;
    @Value("${jwt.secretKeyRt}")
    private String secretKeyRt;

    private Key secret_at_key;
    private Key secret_rt_key;

//    @Qualifier는 기본적으로 메서드를 통한 주입이 가능하다, 그래서 이 경우 생성자 주입 방식을 해야만 @Qualifier 사용가능
//    @Qualifier를 사용하게 되면 생성자 주입을 사용, @RequiredArgs 사용 불가능
    public JwtTokenProvider(MemberRepository memberRepository, @Qualifier("rtInventory") RedisTemplate<String, String> redisTemplate) {
        this.memberRepository = memberRepository;
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    public void init() { // 메서드로 한 번 만들어서 JwtTokenProvider를 싱글톤 객체로 주입을 해서 사용
        secret_at_key = new SecretKeySpec
                (java.util.Base64.getDecoder().decode(secretKeyAt), // 인코딩 된 걸 디코딩 하고
                SignatureAlgorithm.HS512.getJcaName()); // decode + algorithm 세팅
        secret_rt_key = new SecretKeySpec
                (java.util.Base64.getDecoder().decode(secretKeyRt),
                        SignatureAlgorithm.HS512.getJcaName());
    }

    public String createAtToken(Member member) {
        String email = member.getEmail();
        String role = member.getRole().toString();

        Claims claims = Jwts.claims().setSubject(email); // filter에서 claims.getSubject와 싱크가 맞음
        claims.put("role", role);
        Date now = new Date();
        String accessToken = Jwts.builder() // 라이브러리의 도움, 토큰을 제작
                .setClaims(claims)
                .setIssuedAt(now) // 발행시간
                .setExpiration(new Date(now.getTime() + expirationAt*60*1000L)) // 만료시간, 유효기간 지난 토큰은 의미없음, 30분을 밀리초 단위로 세팅(30*60*1000L), 시간은 중요값, 만료 되었을 때 검증하는 것이 필요(라이브러리 존재)
                .signWith(secret_at_key)
                .compact();

        return accessToken;
}

    public String createRtToken(Member member) {
//        유효기간이 긴 rt 토큰 생성
        String email = member.getEmail();
        String role = member.getRole().toString();

        Claims claims = Jwts.claims().setSubject(email); // filter에서 claims.getSubject와 싱크가 맞음
        claims.put("role", role);
        Date now = new Date();
        String refreshToken = Jwts.builder() // 라이브러리의 도움, 토큰을 제작
                .setClaims(claims)
                .setIssuedAt(now) // 발행시간
                .setExpiration(new Date(now.getTime() + expirationRt*60*1000L)) // 만료시간, Rt는 다르게 설정
                .signWith(secret_rt_key)
                .compact();

//        rt토큰을 만듦과 동시에 redis에 저장 (RedisTemplet 객체를 받아서)
//        값을 저장하는 메서드 opsForValue().set(Key, Value) 형식
        redisTemplate.opsForValue().set(member.getEmail(), refreshToken);
//        redisTemplate.opsForValue().set(member.getEmail(), refreshToken, 200, TimeUnit.DAYS);
//        특정 시간에 종료되도록 설정 (200일), ttl 세분화 설정 가능, 우리는 필요성이 떨어지는 게 이미 .yml에서 expirationRt를 설정했기 때문
        return refreshToken;
    }

    public Member validateRt(String refreshToken) {
//        rt 그 자체를 검증
        Claims claims = Jwts.parserBuilder() // 이것부터 아래 코드 전부가 검증하는 코드이다, 토큰이 잘못됐을 땐 여기서 에러가 남?
                .setSigningKey(secretKeyRt)
                .build()
                .parseClaimsJws(refreshToken) // 토큰의 payload는 이 token 안에 있음
                .getBody();
//        토큰 자체가 잘못됐다면 위 로직에 걸려서 500 에러가 뜬다, 토큰이 잘못되었다, token must contain 어쩌고...

        String email = claims.getSubject();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("찾고자 하는 이메일이 없음"));

//        redis의 값과 비교하는 검증
        String redisRt = redisTemplate.opsForValue().get(member.getEmail());
        if (!redisRt.equals(refreshToken)) {
            throw new IllegalArgumentException("잘못된 토큰값입니다.");
        }
        return member;
    }

}

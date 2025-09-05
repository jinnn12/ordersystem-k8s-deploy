package com.beyond.ordersystem.common.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Component // (service 와 유사한 성격)
@Slf4j
public class JwtTokenFilter extends GenericFilter {

    @Value("${jwt.secretKeyAt}")
    private String secretKey;
    //    token이 없는 경우 그냥 진행 시켜
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        try {
            HttpServletRequest req = (HttpServletRequest) servletRequest;
            String bearerToken = req.getHeader("Authorization");
            if (bearerToken == null) {
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            }
            String token = bearerToken.substring(7);
            Claims claims = Jwts.parserBuilder() // 이것부터 아래 코드 전부가 검증하는 코드이다, 토큰이 잘못됐을 땐 여기서 에러가 남?
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token) // 토큰의 payload는 이 token 안에 있음
                    .getBody();

            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + claims.get("role")));
            Authentication authentication = new UsernamePasswordAuthenticationToken(claims.getSubject(), "", authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }
}

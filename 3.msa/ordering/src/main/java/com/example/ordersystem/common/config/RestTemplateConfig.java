package com.example.ordersystem.common.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    // eureka에 등록된 서비스명을 사용해서 내부 서비스 호출(내부통신)하는 어노테이션
    // 이것을 사용하지 않으면 http에 DNS서버에게 요청하기에 무조건 붙여주자.
    @LoadBalanced
    public RestTemplate makeRestTemplate() {
        return new RestTemplate();
    }
}

package com.beyond.ordersystem.common.config;

import com.beyond.ordersystem.member.service.SseAlarmService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Value("${spring.redis.host}")
    private String host;
    @Value("${spring.redis.port}")
    private int port;


    @Bean
//    @Qualifier : 같은 Bean 객체가 여러 개 있을 경우, Bean 객체를 구분하기 위한 어노테이션
    @Qualifier("rtInventory")
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        configuration.setDatabase(0);

        return new LettuceConnectionFactory(configuration);
    }

    @Bean
    @Qualifier("rtInventory")
//    Bean들끼리 서로 의존성을 주입 받을 때 메서드 파라미터(매개변수)로도 주입 가능
//    RedistTemplet<키 type, 밸류 type>
    public RedisTemplate<String, String> redisTemplate(@Qualifier("rtInventory") RedisConnectionFactory redisConnectionFactory) { // 0번 팩토리 (Bean)싱글톤 객체로 주입 받겠다
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory); // 0번 팩토리 싱글톤 객체로 연결, 어떤 DB로 연결할 것인가에 대한
//        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer()); //-> redisTemplate.set("key", 객체(Object) -> 알아서 json형변환, objectMapper사용 안해도 됨)
        return redisTemplate;
    }

    //    redis pub/sub을 위한 연결객체 생성
    @Bean
    @Qualifier("ssePubSub")
    public RedisConnectionFactory sseFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);

//        redis pub/sub 기능은 DB에 값을 저장하는 기능이 아니므로, 특정 DB에 의존적이지 않음
        return new LettuceConnectionFactory(configuration);
    }

    @Bean
    @Qualifier("ssePubSub")
    public RedisTemplate<String, String> sseRedisTemplet(@Qualifier("ssePubSub") RedisConnectionFactory stockConnectionFactory) { // 0번 팩토리 (Bean)싱글톤 객체로 주입 받겠다
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(stockConnectionFactory); // 1번 팩토리 싱글톤 객체로 연결, 어떤 DB로 연결할 것인가에 대한
        return redisTemplate;
    }

//    Redis 리스너 객체를 만들어보자 (래빗엠큐의 리스너객체와 같음) , "order-channel"객체를 바라보고 있음
    @Bean
    @Qualifier("ssePubSub")
    public RedisMessageListenerContainer redisMessageListenerContainer(@Qualifier("ssePubSub") RedisConnectionFactory redisConnectionFactory,
                                                                       MessageListenerAdapter messageListenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.addMessageListener(messageListenerAdapter, new PatternTopic("order-channel")); // 이게 구독 채널 이름
//        만약에 여러 채널을 구독해야 하는 경우에는 여러 개의 PatternTopic을 add하거나, 별도의 Bean객체 생성
        return container;
    }

//    Redis의 채널에서 수신된 메세지를 처리하는 Bean객체
// sseAlarmService가 메세지가 되어 SseAlarmService에 override 메서드의 매개변수 Message message로 들어감
//    채널로부터 수신되는 message 처리를 SseAlarmService의 onMessage 메서드로 설정 -> SseAlarmService에 @Override 되어 있는 onMessage 메서드로 가라
//    즉, 메시지가 수신되면 onMessage 메서드가 호출된다.
    @Bean
    public MessageListenerAdapter messageListenerAdapter(SseAlarmService sseAlarmService) {
        return new MessageListenerAdapter(sseAlarmService, "onMessage");
    }
}

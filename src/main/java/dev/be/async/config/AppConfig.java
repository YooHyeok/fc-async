package dev.be.async.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Thread Pool 정의 설정클래스
 * 2개의 Thread Pool을 정의한다.
 * 특정 메소드가 특정 Thread Pool을 사용할 수 있도록 명시 지정한다.
 */
@Configuration
public class AppConfig {

    @Bean(name = "defaultTaskExecutor", destroyMethod = "shutdown"/* 의도치 않게 정의되지 않는 현상 방지 */)
    public ThreadPoolTaskExecutor defaultTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(200);
        executor.setMaxPoolSize(300);
        executor.setKeepAliveSeconds(10);
        return executor;
    }

    @Bean(name = "messageingTaskExecutor", destroyMethod = "shutdown"/* 의도치 않게 정의되지 않는 현상 방지 */)
    public ThreadPoolTaskExecutor messageingTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(200);
        executor.setMaxPoolSize(300);
        executor.setKeepAliveSeconds(10);
        return executor;
    }
}

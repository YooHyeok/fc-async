package dev.be.async.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Async(비동기)하게 동작할 수 있도록 Annotation을 선언한다.
 */
@Configuration
@EnableAsync // Async하게 동작된다.
public class AsyncConfig {
}

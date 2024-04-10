package dev.be.async.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    @Async("defaultTaskExecutor") // 어떤 스레드 풀을 사용할 지 정의
    public void sendMail() {// @Async어노테이션 선언한 메소드 구현시 반드시 public으로 선언해야한다. private선언시 에러발생
        System.out.println("[sendMail] :: " + Thread.currentThread().getName());
    }
    @Async("messageingTaskExecutor")
    public void sendMailWithCustomThreadPool() {
        System.out.println("[messageTaskExecutor] :: " + Thread.currentThread().getName());
    }
}

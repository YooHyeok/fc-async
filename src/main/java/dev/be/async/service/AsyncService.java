package dev.be.async.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Spring에서 비동기 작업을 하기 위해서는 SpringFramework의 도움이 필요하다.<br/>
 * SpringFramework가 비동기로 처리하고자 하는 메소드 <br/>
 * 예를들어, 비동기 메소드를 구현한 EmailService는 빈으로 등록되어 있을것이다. <br/>
 * 해당 Bean을 반환받았을 때 순수한 Bean을 AsyncService에게 반환하는것이 아니라 <br/>
 * EmailService Bean의 경우 Async하게 동작해야 하기 때문에 한번 더 Wrapping 해준다 <br/>
 * Proxy객체로 Wrapping을 해서 반환해준다. <br/>
 * AsyncService 위와같이 Wrapping된 Proxy객체를 주입받게되고, <br/>
 * 해당 Proxy객체로부터 비동기 메소드를 호출하게 되면 <br/>
 * 비동기로 동작할 수 있게 SubThread에게 위임하게 된다. <br/>
 * 중요한 key point는 Spring Container에 등록된 Bean을 사용해야 Proxy객체와 SubThread에의해 비동기로 작동한다는 것이다. <br/>
 */
@Service
@RequiredArgsConstructor
public class AsyncService {

    private final EmailService emailService;

    /**
     * 빈주입을 받아 비동기 메소드를 호출했을 때
     * 비동기로 호출되는지 확인
     * 모두 다른 Thread 이름이 확인된다.
     */
    public void asyncCall_1() {
        System.out.println("[asyncCall_1] :: " + Thread.currentThread().getName());
        emailService.sendMail();
        emailService.sendMailWithCustomThreadPool();
    }

    /**
     * 단순한 인스턴스의 비동기 메소드를 호출했을 때
     * 비동기로 호출되는지 확인
     * 모두 동일한 Thread 이름이 확인된다.
     */
    public void asyncCall_2() {
        System.out.println("[asyncCall_2] :: " + Thread.currentThread().getName());
        EmailService emailService = new EmailService();
        emailService.sendMail();
        emailService.sendMailWithCustomThreadPool();
    }

    /**
     * 현재 클래스 내부 메소드의 비동기 메소드를 호출했을 때 <br/>
     * 비동기로 호출되는지 확인 <br/>
     * 모두 동일한 Thread 이름이 확인된다. <br/>
     * <br/>
     * 이미 AsyncService는 Bean을 가지고 왔고, 해당 Bean안에 있는 메소드와 같은 메소드가 아닌 <br/>
     * 클래스 내부에 선언한 멤버 메소드에 다이렉트로 접근하게 되면 SpringFramework의 도움을 받을 수 없다. <br/>
     * 즉,메서드를 호출하는 콜러입장에서 Proxy객체로 Wrapping된 빈을 사용해야 하지만 <br/>
     * @Async 어노테이션을 붙이지 않은 메소드처럼 동작하게 된다.
     */
    public void asyncCall_3() {
        System.out.println("[asyncCall_3] :: " + Thread.currentThread().getName());
        sendMail();
    }

    @Async
    public void sendMail() {
        System.out.println("[sendMail] :: " + Thread.currentThread().getName());
    }
}

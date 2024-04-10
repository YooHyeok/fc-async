# Async

## 비동기 프로그래밍 이란?
Async한 통신을 구현하기 위해 사용하는 방법 으로  
실시간성 응답을 필요로 하지 않는 상황에서 사용하는것이 가장 적절한 방법이다.  
예를들어 Notification이라던지, 회원가입을 하였을 때 가입 축하 메일 전송, 혹은 푸시 알람 등은  
즉각적인 실시간성을 요구하지 않기 때문에 Async하게 작업하는게 일반적으로 통용되고 있다.  

Java Spring 환경에서 Main 요청이 들어오면 일반적으로 Main Thread에서 Task를 처리하게 된다.
그러나 비동기 프로그래밍을 하기 위해서는 Main Thread에서 처리하는 것이 아니라 Sub Thread에게 비동기로 처리해아하는 Task를
위임하는 행위라고 말할 수 있다.

Spring에서 비동기 프로그래밍을 하기 위해서는 Thread Pool 정의를 할 필요가 있다.    

## Thread Pool의 옵션
1. `CorePoolSize`  
    Thread Pool에 최소한의 스레드를 몇개 가지고 있을것이냐 지정하는 옵션
    만약 size를 5개로 설정한다면 해당 ThreadPool은 일이 없더라도 최소한 5개의 스레드는 무조건 리소스를 점유하고 있게 된다. 
2. `MaxPoolSize`  
    최대 몇개까지의 스레드를 할당할것인지를 설정하는 옵션
3. `WorkQueue`  
    특정 요청이 들어왔을 때 모든 스레드들이 바로 처리할 수 없기 때문에 먼저 들어온 요청을 먼저 처리할 수 있는 자료구조인 Queue를
    사용하여 WorkQueue라는 곳에 여러 요청을 담은 뒤 현재 작업하고 있는 스레드들이 현재 Task가 마무리되면 다음 작업할 Task를 가져온다.
4. `KeepAliveTime`  
   Core/Max PoolSize 두 옵션을 기준으로 스레드의 사이즈가 늘렸다 줄였다 하는 행위가 발생할것이고,  
   CorePoolSize보다 많은 스레드가 할당이 되었을 때 이 스레드들을 언젠간 반환해야하 할 것이다.
   반환하는 조건으로 지정한 시간만큼 스레드들이 일을 하지 않는다면 자원을 반납하는 옵션이다.

Thread Pool을 정의하면 처음에는 CorePoolSize 만큼 Thread를 생성하게 된다.  
그 다음 만약 CorePoolSize를 3으로 설정했는데 Request가 4개가 들어오면 4번째 Thread를 생성하지 않고 WorkQueue에 담게 된다.   
WorkQueue Size만큼 새로운 요청들을 계속해서 담고 WorkQueue에 모든 요청, 처음 지정한 Size만큼 요청이 다 쌓였이게 되면  
MaxPoolSize만큼 Thread를 생성하게 된다.

### 예제) 아래 코드를 보고 스레드 풀 설정을 서술하라.
```java
public ThreadPoolExecutor(
        int corePoolSize, 
        int maximumPoolSize, 
        long keepAliveTime, 
        TimeUnit unit, 
        BlockingQueue<Runnable> workQueue
        ) {/*생략*/}
```

```java
new ThreadPoolExecutor(5, 10, 3, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable> (50));
```
최소 5개의 스레드를 무조건 보유하고 있다.  
Queue에 Task가 다 쌓였으면 10개까지 스레드를 생성할 것이다.  
5개 초과인 6번째 부터 10번째 까지의 스레드가 3초동안 (3개의 Seconds이므로) 일을 하지 않으면 자원을 반납한다.
Queue에는 50개까지의 Task(Request)를 담고있다.

## Thread Pool 생성시 주의점

1) ### CorePoolSize값을 너무 크게 설정할 경우 Side Effect 고려해보기` 
   CorePoolSize의 경우 설정한 수 만큼은 무조건 자원을 점유하고 있기 때문에 너무 큰 값을 설정하게 되면  
    많은 스레드를 점유하고 있을 수 있기 때문에 적절하게 조절해서 설정할 필요가 있다.  
    실제 실무에서는 기본적으로 정의가 되어 있는 스레드 풀 들이 있다.   
   (완전 신규프로젝트라면 기존 프로젝트를 참조해서 설정하는 게 일반적인 설정 방법이다.)
2) ### Exception
   1. `IllegalArgumentException`
      - corePoolSize < 0  
        Main Thread가 ThreadPool에 특정 Task를 위임하지만 ThreadPool에 Thread가 없으면 작업이 불가능하다.
      - keepAliveTime < 0
        corePoolSize에서 스레드가 더 필요해 새로운 스레드를 생성하려고 할 때 생성하자마자 바로 죽게된다.
      - maximumPoolSize <= 0
        일반적으로 corePoolSize는 maxPoolSize보다 작으므로 corePoolSize도 무조건 0보다 작게되어 단 하나의 스레드도 없기때문.
      - maximumPoolSize < corePoolSize  
        minimum이 maximum보다 큰값을 갖는다는 것이므로 말이 안된다. 
        위 네가지 케이스중 하나라라도 해당되면 발생한다.
   2. `NullPointerException`  
    코어풀사지으 만큼 스레드를 생성하고 Queue에 다음 요청들을 담게된다.
    그러나 WorkQueue가 Null일 경우엔 WorkQueue.push/put() 과 같은 함수를 호출하게 되면 정의되어있지 않은 null상태의  
    WorkQueue에서는 당연히 nullPointException이 발생하게 된다.

## CorePoolSize 정리

```text
if (Thread수 < CorePoolSize) new Thread 생성
if (Thread수 > CorePoolSize) Queue에 요청 추가
```
스레드 수가 코어풀 사이즈보다 적으면 새로운 스레드를 생성하게 되고, 그 반대의 상황에는 Queue에 요청을 추가한다.

## MaxPoolSize 정리
```text
if (Queue Full && Thread수 < MaxPoolSize) new Thread 생성
if (Queue Full && Thread수 > MaxPoolSize) 요청 거절
```
Queue가 가득 차있고, 스레드수가 맥스풀 사이즈보다 적으면 새로운 스레드를 생성하게 되고,  
KeepAliveTime으로 지정한 시간만큼 특정 작업을 하지 않으면 리소스를 반납하게 된다.

그 반대의 상황은 요청을 거절하게 된다.  
(요청 거절 혹은 Exception 등 핸들링이 가능하다)

# Spring과 @Async
Spring에서 비동기 작업을 하기 위해서는 SpringFramework의 도움이 필요하다.  
SpringFramework가 비동기로 처리하고자 하는 메소드  
예를들어, 비동기 메소드를 구현한 EmailService는 빈으로 등록되어 있을것이다.  
해당 Bean을 반환받았을 때 순수한 Bean을 AsyncService에게 반환하는것이 아니라  
EmailService Bean의 경우 Async하게 동작해야 하기 때문에 한번 더 Wrapping 해준다  
Proxy객체로 Wrapping을 해서 반환해준다.  
AsyncService 위와같이 Wrapping된 Proxy객체를 주입받게되고,  
해당 Proxy객체로부터 비동기 메소드를 호출하게 되면  
비동기로 동작할 수 있게 SubThread에게 위임하게 된다.  
중요한 key point는 Spring Container에 등록된 Bean을 사용해야 Proxy객체와 SubThread에의해 비동기로 작동한다는 것이다.  



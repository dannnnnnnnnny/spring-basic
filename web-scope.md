## 웹 스코프
- 웹 환경에서만 동작
- 프로토타입과 다르게 스프링이 해당 스코프 종료시점까지 관리. 따라서 종료 메서드 호출됨

### 웹 스코프 종류
- request: HTTP 요청 하나가 들어오고 나갈 때까지 유지되는 스코프. 각각 HTTP 요청마다 별도의 빈 인스턴스가 생성되고 관리됨
- session: HTTP Session과 동일한 생명주기를 가지는 스코프
- application: 서블릿 컨텍스트와 동일한 생명주기를 가지는 스코프
- websocket: 웹소켓과 동일한 생명주기를 가지는 스코프

#### 웹 환경 추가
```groovy
implementation 'org.springframework.boot:spring-boot-starter-web'
```
CoreApplication 실행시 Tomcat이 같이 실행됨.
(spring-boot-starter-web 라이브러리를 추가하면 스프링부트는 내장 톰캣 서버를 활용하여 웹 서버와 스프링을 함께 실행시킴)
(스프링 부트는 웹 라이브러리가 없으면 AnnotationConfigApplicationContext)을 기반으로 애플리케이션을 구동함. 웹 라이브러리가 추가되면 웹 관련된 추가 설정과 환경들이 필요하므로 AnnotationConfigServletWebServerApplicationContext를 기반으로 애플리케이션을 구동함.

- 만약 기본 포트 8080를 다른 곳에서 사용중이라면 변경해줘야 함
'main/resources/application.properties'
```
server.port = 9090
```
---
- 동시에 여러 http 요청이 오면 정확히 어떤 요청이 남긴 로그인지 구분하기 어려움. 이럴 때 request 스코프를 사용하면 좋음
  - (UUID를 통해 구분)
  - request URL 정보도 넣어서 어떤 URL 요청 로그인지 구분

```java
@Component
@Scope(value = "request")
public class MyLogger {

    private String uuid;
    private String requestURL;

    public void setRequestURL(String requestURL) {
        this.requestURL = requestURL;
    }

    public void log(String message) {
        System.out.println("[" + uuid + "]" + "[" + requestURL + "] " + message);
    }

    @PostConstruct
    public void init() {
        uuid = UUID.randomUUID().toString();
        System.out.println("[" + uuid + "] request scope bean create: " + this);
    }

    @PreDestroy
    public void close() {
        System.out.println("[" + uuid + "] request scope bean close: " + this);
    }
}
```
- 로그 출력 위한 클래스
- request 스코프
- 빈이 생성되는 시점에 uuid 생성 후 저장
- requestURL은 생성되는 시점을 알 수 없으므로 setter로 입력 받음

```java
package hello.core.web;

import hello.core.common.MyLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequiredArgsConstructor
public class LogDemoController {

    private final LogDemoService logDemoService;
    private final ObjectProvider<MyLogger> myLoggerProvider;

    @RequestMapping("log-demo")
    @ResponseBody // 문자 그대로 응답받음
    public String logDemo(HttpServletRequest request) throws InterruptedException { // 고객 요청 정보를 받음
        MyLogger myLogger = myLoggerProvider.getObject();
        String requestURL = request.getRequestURL().toString();
        myLogger.setRequestURL(requestURL);

        myLogger.log("controller test");
        Thread.sleep(1000);
        logDemoService.logic("testId");
        return "OK!";
    }
}

```

```java
package hello.core.web;

import hello.core.common.MyLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogDemoService {

    private final ObjectProvider<MyLogger> myLoggerProvider;

    public void logic(String id) {
        MyLogger myLogger = myLoggerProvider.getObject();
        myLogger.log("service id = " + id);
    }
}

```
##### => error 발생
- request scope 은 http 요청시에 빈이 생성되기 때문에 생성자 주입시 에러가 발생함
- ObjectProvider.getObject()를 호출하는 시점에는 http 요청이 진행중이므로 빈 생성을 지연 생성할 수 있음

---

## 스코프와 프록시
```java
package hello.core.common;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.UUID;

@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class MyLogger {

    private String uuid;
    private String requestURL;

    public void setRequestURL(String requestURL) {
        this.requestURL = requestURL;
    }

    public void log(String message) {
        System.out.println("[" + uuid + "]" + "[" + requestURL + "] " + message);
    }

    @PostConstruct
    public void init() {
        uuid = UUID.randomUUID().toString();
        System.out.println("[" + uuid + "] request scope bean create: " + this);
    }

    @PreDestroy
    public void close() {
        System.out.println("[" + uuid + "] request scope bean close: " + this);
    }
}

```
- proxyMode 설정
- provider가 없어도 문제없이 동작함


```java
@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class MyLogger {
```
- 핵심 부분임
  - 적용 대상이 인터페이스가 아닌 클래스면 TARGET_CLASS
  - 적용 대상이 인터페이스면 INTERFACES
- 이렇게 하면 MyLogger의 가짜 프록시 클래스를 만들어두고 HTTP request 와 상관없이 가짜 프록시 클래스를 다른 빈에 미리 주입해둘 수 있음

```java
System.out.println("myLogger = " + myLogger.getClass());
```
```
myLogger = class hello.core.common.MyLogger$$EnhancerBySpringCGLIB$$ffc72b9d
```
- 스프링이 조작해서 만들어줌 (CGLIB)

#### CGLIB 라이브러리로 클래스를 상속 받은 가짜 프록시 객체를 만들어서 주입해줌
- @Scope의 proxyMode = ScopeProxyMode.TARGET_CLASS를 설정하면 스프링 컨테이너는 CGLIB 이라는 바이트 코드를 조작하는 라이브러리를 사용해서 MyLogger 를 상속받은 가짜 프록시 객체를 생성함
- 출력해보면 순수한 MyLogger가 아니라 MyLogger$$EnhanceBySpringCGLIB 이라는 클래스로 만들어진 객체가 등록된 것을 확인가능
- 스프링 컨테이너에서 myLogger 라는 이름으로 진짜 대신 가짜 프록시 객체를 등록
- ac.getBean("myLogger", MyLogger.class) 로 조회해도 프록시 객체가 조회됨
  - 의존관계 주입도 가짜 프록시 객체가 주입

#### 가짜 프록시 객체는 요청이 오면 그때 내부에서 진짜 빈을 요청하는 위임 로직이 들어있음
- 클라이언트가 myLogger.logic() 을 호출하면 사실은 가짜 프록시 객체의 메서드를 호출한 것임
- 가짜 프록시 객체는 내부에 진짜 myLogger를 찾는 방법을 알고 있음
- 가짜 프록시 객체는 request scope의 진짜 myLogger.logic()을 호출함
- 가짜 프록시 객체는 원본 클래스를 상속받아 만들어졌기 때문에 이 객체를 사용하는 클라이언트 입장에서는 사실 원본인지 아닌지도 모르게 동일하게 사용 가능 (다형성)

**동작 정리**
- CGLIB 라이브러리로 클래스를 상속받은 가짜 프록시 객체를 만들어서 주입함
- 가짜 프록시 객체는  실제 요청이 오면 그때 내부에서 실제 빈을 요청하는 위임 로직이 들어있음
- 가짜 프록시 객체는 실제 request scope과 관계 없음. 그냥 가짜이고 내부에 단순한 위임 로직만 있으며 싱글톤처럼 동작함

**특징 정리**
- 프록시 객체 덕분에 클라이언트는 마치 싱글톤을 사용하듯이 편리하게 request scope을 사용 가능
- 사실 Provider를 사용하든 프록시를 사용하든 **핵심 아이디어는 진짜 객체를 꼭 필요한 시점에서 지연처리** 한다는 점
- 단지 애노테이션 설정 변경만으로 원본 객체를 프록시 객체로 대체할 수 있음. 이것이 다형성과 DI 컨테이너가 가진 큰 장점
- 웹 스코프가 아니여도 프록시는 사용 가능

**주의점**
- 마치 싱글톤을 사용하는 것 같지만 다르게 동작하기 때문에 주의해야 함
- 이런 특별한 proxy는 꼭 필요한 경우에만 최소화해서 사용해야 함. 무분별하게 사용시 유지보수하기 어려움
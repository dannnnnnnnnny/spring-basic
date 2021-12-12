# 빈 스코프
- 지금까지 스프링 빈이 스프링 컨테이너의 시작과 함께 생성되어서 스프링 컨테이너가 종료될 때까지 유지된다고 학습했음.
- 이것은 스프링 빈이 기본적으로 싱글톤 스코프로 생성되기 때문임
- 스코프는 번역 그대로 빈이 존재할 수 있는 범위를 뜻함

**스프링은 다양한 스코프를 지원한다.**
1. 싱글톤: default, 스프링 컨테이너의 시작과 종료까지 유지되는 가장 넓은 범위 스코프
2. 프로토타입: 스프링 컨테이너는 프로토타입 빈의 생성과 의존관계 주입까지만 관여하고 더는 관리하지 않는 매우 짧은 범위 스코프
3. 웹 관련 스코프
   1. request: 웹 요청이 들어오고 나갈 때 까지 유지되는 스코프
   2. session: 웹 세션이 생성되고 종료될 때 까지 유지되는 스코프
   3. application: 웹 서블릿 켄텍스트와 같은 범위로 유지되는 스코프
---
   
### 컴포넌트 스캔 자동 등록
```java
@Scope("prototype")
@Component
public class HelloBean {}
```

### 수동 등록
```java
@Scope("prototype")
@Bean
PrototypeBean HelloBean() {
    return new HelloBean();
}
```

### 프로토타입 스코프
- 싱글톤 스코프의 빈을 조회하면 스프링 컨테이너는 항상 같은 인스턴스의 스프링 빈을 반환한다. 반면에 프로토타입 스코프를 스프링 컨테이너에 조회하면 스프링 컨테이너는 항상 새로운 인스턴스를 생성해서 반환한다.

##### 싱글톤 빈 요청
1. 싱글톤 스코프의 빈을 스프링 컨테이너에 요청한다
2. 스프링 컨테이너는 본인이 관리하는 스프링 빈을 반환한다
3. 이후에 스프링 컨테이너에 같은 요청이 와도 같은 객체 인스턴스 스프링 빈을 반환한다

##### 프로토타입 빈 요청
1. 프로토타입 스코프 빈을 스프링 컨테이너에 요청한다
2. 스프링 컨테이너는 이 시점에 프로토타입 빈을 생성하고 필요한 의존관계를 주입한다
3. 스프링 컨테이너는 생성한 프로토타입 빈을 클라이언트에 반환한다
4. 이후에 스프링 컨테이너에 같은 요청이 오면 항상 새로운 프로토타입 빈을 생성해서 반환한다. (클라이언트에 반환하면, 관리하지 않고 바로 버림)

**=> 핵심은 스프링 컨테이너는 프로토타입 빈을 생성하고 의존관계 주입, 초기화까지만 처리한다는 것**
- 이후 프로토타입 빈 관리 X
- 관리할 책임은 프로토타입 빈을 받은 클라이언트에 있음
- 그렇기 때문에 **@PreDestroy 같은 종료 메서드가 호출되지 않음** 

---
- 싱글톤 빈은 스프링 컨테이너 생성 시점에 초기화 메서드가 실행되지만, 프로토타입 스코프 빈은 스프링 컨테이너에서 빈을 조회할 때 생성되고, 초기화 메서드도 실행됨
- 프로토 타입 빈을 2번 조회했으므로 와넞ㄴ히 다른 스프링 빈이 생성되고, 초기화도 2번 실행된 것을 확인할 수 있음
- 싱글톤 빈은 스프링 컨테이너가 관리하기 때문에 스프링 컨테이너가 종료될 때 빈의 종료 메서드가 실행되지만, 프로토타입 빈은 스프링 컨테이너가 생성과 의존관계 주입 그리고 초기화까지만 관여하고 더는 관리하지 않음.
  - 따라서 프로토타입 빈은 스프링 컨테이너가 종료될 때 @PreDestroy 같은 종료 메서드가 전혀 실행되지 않음

**프로토타입 빈 정리**
- 스프링 컨테이너에 요청할 때 마다 새로 생성
- 스프링 컨테이너는 프로토타입 빈의 생성과 의존관계 주입 그리고 초기화까지만 관여함
- 종료 메서드 호출 X
- 프로토타입 빈은 프로토타입 빈을 조회한 클라이언트가 관리해야 함. 종료 메서드에 대한 호출도 클라이언트가 직접해야 함
   - prototypeBean1.destroy()

---
## 프로토타입 스코프를 싱글톤 빈과 함께 사용시 문제점
- 스프링 컨테이너에 프로토타입 스코프의 빈을 요청하면 항상 새로운 객체 인스턴스를 생성해서 반환함.
- 하지만 싱글톤빈과 함께 사용할 때는 의도한대로 잘 동작하지 않으므로 주의해야 함

### 싱글톤("clientBean") 내부에서 프로토타입 빈 사용하는 경우
- 싱글톤은 보통 스프링 컨테이너 생성 시점에 함께 생성되고, 의존관계 주입도 발생함
- clientBean 은 의존관계 자동 주입시점에 프로토타입 빈을 요청함
- 스프링 컨테이너는 프로토타입 빈을 생성해서 clientBean에 반환함. 프로토타입 빈의 count 필드는 0
- clientBean은 프로토타입 빈을 내부 필드에 보관함 (참조값 보관)
- 클라이언트A는 clientBean을 스프링 컨테이너에 요청해서 받음. 싱글톤이므로 항상 같은 clientBean이 반환됨
- 클라이언트A는 clientBean.logic()을 호출함
- clientBean은 prototypeBean의 addCount()를 호출해서 프로토타입 빈의 count를 증가시킴. (count == 1)
- 클라이언트B가 똑같이 clientBean을 스프링 컨테이너에 요청해서 받음 (싱글톤이므로 같은 clientBean)
  - clientBean 내부에 가지고 있는 프로토타입 빈은 이미 과거에 주입이 끝난 빈임. 주입 시점에 스프링 컨테이너에 요청해서 프로토타입 빈이 새로 생성된 것이지, 사용할 때마다 새로 생성되는 것이 아님.
- 클라이언트B가 clientBean.logic() 호출
- clientBean은 prototypeBean의 addCount를 호출해서 프로토타입 빈의 count를 증가시킴 (count == 2)

---
스프링 일반적으로 싱글톤 빈을 사용하므로 싱글톤 빈이 프로토타입 빈을 사용하게 됨
그런데 싱글톤 빈은 생성 시점에만 의존관계를 주입받기 때문에, 프로토타입 빈이 새로 생성되기는 하지만 싱글톤 빈과 함께 계속 유지되는 것이 문제임.
(프로토타입 빈을 사용하는 이유는 주입 시점에서만 새로 생성하는게 아니라, 사용할 때마다 새로 생성해서 사용하는 것을 원할 것임.)

### Provider로 문제 해결
```java
public class SingletonWithPrototypeTest {

    @Test
    void prototypeFind() {
        AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(PrototypeBean.class);
        PrototypeBean prototypeBean1 = ac.getBean(PrototypeBean.class);

        prototypeBean1.addCount();

        assertThat(prototypeBean1.getCount()).isEqualTo(1);

        PrototypeBean prototypeBean2 = ac.getBean(PrototypeBean.class);

        prototypeBean2.addCount();

        assertThat(prototypeBean2.getCount()).isEqualTo(1);
    }

    @Test
    void singletonClientUsePrototype() {
        AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(ClientBean.class, PrototypeBean.class);

        ClientBean clientBean1 = ac.getBean(ClientBean.class);
        int count1 = clientBean1.logic();
        assertThat(count1).isEqualTo(1);

        ClientBean clientBean2 = ac.getBean(ClientBean.class);
        int count2 = clientBean2.logic();
        assertThat(count2).isEqualTo(1);

    }

    @Scope("singleton")
    static class ClientBean {

        @Autowired
        private ObjectProvider<PrototypeBean> prototypeBeanProvider; 

        public int logic() {
            PrototypeBean prototypeBean = prototypeBeanProvider.getObject(); // getObject 호출시에, 스프링 컨테이너에서 찾아서 전달해서 반환해줌. 그렇기 때문에 매번 새로운 prototypeBean이 반환됨
            prototypeBean.addCount();
            return prototypeBean.getCount();
        }
    }

    @Scope("prototype")
    static class PrototypeBean {
        private int count = 0;

        public void addCount() {
            count++;
        }

        public int getCount() {
            return count;
        }

        @PostConstruct
        public void init() {
            System.out.println("PrototypeBean.init" + this);
        }

        @PreDestroy
        public void destroy() {
            System.out.println("PrototypeBean.destroy");
        }
    }
}
```
#### ObjectFactory, ObjectProvider
- 지정한 빈을 컨테이너에서 대신 찾아주는 DL (Dependency Lookup) 서비스를 제공하는 것이 바로 ObjectProvider임.
- 과거에는 ObjectFactory가 있었는데 여기서 편의기능을 추가해서 상속받은 ObjectProvider가 만들어짐
- getObject()를 통해서 항상 새로운 프로토타입 빈이 생성됨
  - 호출시 내부에서 스프링 컨테이너를 통해 해당 빈을 찾아서 반환함
- 스프링이 제공하는 기능을 사용하지만 기능이 단순하므로 단위테스트를 만들거나 mock 코드를 만들기는 훨씬 쉬움
- ObjectProvider는 지금 딱 필요한 DL 정도의 기능만 제공함

- ObjectProvider는 ObjectFactory를 상속받았고, 옵션, 스트림 처리 등 편의 기능이 추가됨. 별도의 라이브러리는 필요 없으며 스프링에 의존적임

#### JSR-330 Provider
- 'javax.inject.Provider' 자바 표준
- 이 방법을 사용하려면 'javax.inject:javax.inject:1' 라이브러리를 gradle에 추가해야 함
```java
@Scope("singleton")
static class ClientBean {

    @Autowired
    private Provider<PrototypeBean> prototypeBeanProvider;

    public int logic() {
        PrototypeBean prototypeBean = prototypeBeanProvider.get(); // getObject 호출시에, 스프링 컨테이너에서 찾아서 전달해서 반환해줌. 그렇기 때문에 매번 새로운 prototypeBean이 반환됨
        prototypeBean.addCount();
        return prototypeBean.getCount();
    }
}
```
- provder.get() 을 통해 항상 새로운 프로토타입 빈이 생성되는 것을 확인할 수 있음
- provider의 get()을 호출하면 내부에서는 스프링 컨테이너를 통해 해당 빈을 찾아서 반환(DL 기능)
- 자바 표준이고 기능이 단순하므로 단위테스트를 만들거나 mock 코드를 만들기 훨씬 쉬움

**특징**
- get() 메서드 하나로, 기능이 매우 단순
- 별도의 라이브러리 필요
- 자바 표준이므로 스프링이 아닌 컨테이너에서도 사용 가능

**정리**
- 프로토타입 빈을 언제 사용할까? 매번 사용할 때 마다 의존관계 주입이 완료된 새로운 객체가 필요하면 사용하면 됨. 실무에서 웹 애플리케이션을 개발해보면 싱글톤 빈으로 대부분 문제를 해결할 수 있기 때문에 프로토타입 빈을 직접적으로 사용하는 일은 드뭄
(스프링이 제공하는 메서드에 @Lookup 애노테이션을 사용하는 방법도 있지만, 이전 방법들로 충분함)
- 스프링이 아닌 다른 컨테이너를 쓴다면 JSR-330 Provider / 그게아니라면 스프링이 제공하는 ObjectProvider를 쓰면 됨.


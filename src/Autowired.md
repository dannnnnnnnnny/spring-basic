## @Autowired 필드명, @Qualifier, @Primary
#### 조회 대상 빈이 2개 이상일 때 해결 방법


- @Autowired 필드명 매칭
- @Qualifier -> @Qualifier 끼리 매칭 -> 빈 이름 매칭
- @Primary 사용

1. @Autowired 필드명 매칭
- @Autowired는 타입 매칭을 시도, 이때 여러 빈이 있으면 필드 이름(파라미터 이름)으로 빈이름을 추가 매칭함


##### 기존 코드
```java
@Autowired()
private DiscountPolicy discountPolicy;
```

##### 필드명을 빈 이름으로 변경
```java
@Autowired()
private DiscountPolicy rateDiscountPolicy;
```
- 필드명이 rateDiscountPolicy 이므로 정상 주입됨
- **필드명 매칭은 먼저 타입 매칭을 시도하고 그 결과에 따라 여러 빈이 있을 때, 추가로 동작하는 기능**

###### @Autowired 매칭 정리
1) 타입 매칭
2) 타입 매칭의 결과가 2개 이상일 때 필드명으로 빈 이름 매칭

2. @Qualifier 사용
- 추가 구분자를 붙여주는 방법
- 주입시 추갖거인 방법을 제공하는 것이지 빈 이름을 변경하는 것은 아님

```java
@Component
@Qualifier("mainDiscountPolicy")
public class RateDiscountPolicy implements DiscountPolicy {}
```

```java
@Component
@Qualifier("fixDiscountPolicy")
public class FixDiscountPolicy implements DiscountPolicy {}
```
- 주입시 @Qualifier 붙여주고 등록한 이름 적어줌

**수정자에서도 가능**
```java
@Autowired
public DiscountPolicy setDiscountPolicy(@Qualifier("mainDiscountPolicy") DiscountPolicy discountPolicy) {
    return discountPolicy;
        }
```

- @Qualifier 로 주입할 때, @Qualifier("mainDiscountPolicy") 를 못찾는다면 mainDiscountPolicy라는 이름의 스프링 빈을 추가로 찾음. @Qualifier는 @Qualifier를 찾는 용도로만 사용하는 것이 좋음.

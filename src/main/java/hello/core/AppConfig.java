package hello.core;

import hello.core.discount.DiscountPolicy;
import hello.core.discount.FixDiscountPolicy;
import hello.core.discount.RateDiscountPolicy;
import hello.core.member.MemberRepository;
import hello.core.member.MemberService;
import hello.core.member.MemberServiceImpl;
import hello.core.member.MemoryMemberRepository;
import hello.core.order.OrderService;
import hello.core.order.OrderServiceImpl;

public class AppConfig {

    public MemberService memberService() {
        return new MemberServiceImpl(memberRepository());
    }

    private MemberRepository memberRepository() {
        return new MemoryMemberRepository();
    }

    public OrderService orderService() {
        return new OrderServiceImpl(memberRepository(), discountPolicy());
    }

    private DiscountPolicy discountPolicy() {
//        return new FixDiscountPolicy();
//        할인 정책 변경이 구성역할 담당인 AppConfig만 변경하면 됨 (사용영역인 OrderServiceImpl 코드 변경 X)
        return new RateDiscountPolicy();
    }
}

/*
    구성 영역은 당연히 변경됨
    구성 역할을 담당하는 AppConfig는 애플리케이션이라는 공연의 기획자
    공연 기획자는 참여자의 구현 객체들을 모두 알아야 함!
* */
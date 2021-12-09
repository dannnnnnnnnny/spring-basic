package hello.core.order;

import hello.core.discount.DiscountPolicy;
import hello.core.member.Member;
import hello.core.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final MemberRepository memberRepository;
    private final DiscountPolicy rateDiscountPolicy;

    @Override
    public Order createOrder(Long memberId, String itemName, int itemPrice) {
        Member member = memberRepository.findById(memberId);
        int discountPrice = rateDiscountPolicy.discount(member, itemPrice);

        return new Order(memberId, itemName, itemPrice, discountPrice);
    }

    public MemberRepository getMemberRepository() {
        return memberRepository;
    }
}

/*
        private final DiscountPolicy discountPolicy = new FixDiscountPolicy();
        private final DiscountPolicy discountPolicy = new RateDiscountPolicy();
        새로운 할인 정책 적용시 클라이언트인 OrderServiceImpl 코드를 고쳐야함 (OCP 위반)
        Fix -> Rate로 변경해주어야 하는 문제 발생
        DiscountPolicy 인터페이스에 의존하면서 DIP를 지킨 것 같지만, 인터페이스뿐만 아니라 구현클래스에도 의존하고 있음 (DIP 위반)

        private DiscountPolicy discountPolicy;
        => DIP를 위반하지 않고 인터페이스에만 의존하도록 의존관계를 변경하면 됨 (인터페이스에만 의존하도록 설계 변경 필요)
        구현체가 없는데 실행할 수 있는지? (NullPointException 발생)
        클라이언트인 OrderServiceImpl에 DiscountPolicy의 구현객체를 대신 생성하고 주입해줘야 함.

        AppConfig
        - 애플리케이션의 전체 동작 방식을 구성하기 위해 구현 객체를 생성하고 연결하는 책임을 가지는 별도의 설정 클래스
        - 실제 동작에 필요한 구현 객체 생성
        - 생성한 객체 인스턴스의 레퍼런스를 생성자를 통해서 주입해줌

        => OrderServiceImpl은 MemoryMemberRepository, Fix/RateDiscountPolicy를 의존하지 않게 됨 (인터페이스에만 의존)
        => 생성자를 통해 어떤 구현 객체가 들어올지 알 수 없음. 어떤 구현객체를 주입할지는 외부에서 결정됨
        => 이제 의존관계에 대한 고민은 외부에 맡기고 실행에만 집중하면 됨

        [DIP 완성] - OrderServiceImpl은 MemberRepository, DiscountPolicy인 추상에만 의존하면 됨. 구체 클래스를 몰라도 됨
        [관심사의 분리] - 객체를 생성하고 연결하는 역할과 실행하는 역할이 명확히 분리됨

        클라이언트인 OrderServiceImpl입장에서 보면 의존관계를 마치 외부에서 주입해주는 것 같다고 해서 DI (Dependency Injection) 의존관계 주입이라고 함


        AppConfig는 공연 기획자다!
        AppConfig는
            구체 클래스를 선택한다.
            배역에 맞는 담당 배우를 선택한다.
            애플리케이션이 어떻게 동작해야 할지 전체 구성을 책임진다.
        이제 각 배우들(OrderServiceImpl)은 담당 기능을 실행하는 책임만 지면 된다.


        AppConfig의 등장으로 사용 영역(OrderServiceImpl, DiscountPolicy..)과 구성 영역(AppConfig)으로 분리됨
    */
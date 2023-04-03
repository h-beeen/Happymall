package jpabook.jpashop.service;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.exception.NotEnoughStockException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static jpabook.jpashop.domain.OrderStatus.CANCEL;
import static jpabook.jpashop.domain.OrderStatus.ORDER;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired
    EntityManager em;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    OrderService orderService;

    private Book createBook(String name, int price, int quantity) {
        Book book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(quantity);
        em.persist(book);
        return book;
    }

    private Member createMember(String name, String city, String street, String zipcode) {
        Member member = new Member();
        Address address = new Address(city, street, zipcode);
        member.setName(name);
        member.setAddress(address);
        em.persist(member);
        return member;
    }

    @Test
    void productOrder() throws Exception {
        //given
        Member member = createMember("회원1", "강가", "강남대로", "123-123");
        Item book = createBook("시골 JPA", 12000, 15);

        int orderCount = 10;
        //when
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        //then
        Order getOrder = orderRepository.findOne(orderId);

        assertEquals(ORDER, getOrder.getStatus(),"주문 상품의 Status -> ORDER");
        assertEquals(1, getOrder.getOrderItems().size(), "주문 상품의 종류 = 1");
        assertEquals(book.getPrice() * orderCount, getOrder.getTotatlPrice(), "가격 = 가격*수량");
        assertEquals(5, book.getStockQuantity());
    }

    @Test
    void overstockedOrder() throws Exception {
        //given
        Member member = createMember("회원2", "경기", "내천", "123-153");
        Book book = createBook("도시의 JPA", 10000, 10);

        //when
        int orderCount = 15;
        //then
        assertThatThrownBy(() -> orderService.order(member.getId(), book.getId(), orderCount))
                .isInstanceOf(NotEnoughStockException.class);
    }

    @Test
    void cancelOrder() throws Exception {
        //given
        Member member = createMember("회원3", "취소군", "취소시", "567-1281");
        Book book = createBook("해빈의 JPA", 30000, 50);

        //when
        int orderCount = 15;
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);
        assertEquals(35, book.getStockQuantity(), "주문 취소 전 재고량 감소 검증");
        assertEquals(ORDER, orderRepository.findOne(orderId).getStatus(), "주문 취소 전 상태 -> ORDER");
        orderService.cancelOrder(orderId);
        //then
        Order getOrder = orderRepository.findOne(orderId);
        assertEquals(CANCEL, getOrder.getStatus(), "주문 취소시, 상태 -> CANCEL");
        assertEquals(50, book.getStockQuantity(), "주문 취소시 재고량 원복");
    }


}

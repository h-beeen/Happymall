package jpabook.jpashop.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
public class Member {

    @Id
    @GeneratedValue
    @Getter @Setter
    @Column(name = "member_id")
    private Long id;

    private String name;

    @Embedded
    private Address address;

    @Getter
    @OneToMany(mappedBy = "member")
    private List<Order> orders = new ArrayList<>();
}
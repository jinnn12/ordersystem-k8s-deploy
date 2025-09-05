package com.beyond.ordersystem.member.repository;

import com.beyond.ordersystem.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);

    List<Member> findByDelYn(String delYn);
}

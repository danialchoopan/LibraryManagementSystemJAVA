package com.library.repository;

import com.library.entity.Member;
import java.util.List;
import java.util.Optional;

public interface MemberRepository {
    Member save(Member member);
    Optional<Member> findById(Long id);
    Optional<Member> findByNationalCode(String nationalCode);
    List<Member> findAll();
    List<Member> findByName(String name);
    List<Member> findByNationalCodeContaining(String nationalCode);
    boolean existsByNationalCode(String nationalCode);
    boolean deleteById(Long id);
    long count();
}
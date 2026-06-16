package com.library.service;

import com.library.entity.Member;
import java.util.List;

public interface MemberService {
    Member addMember(Member member);
    Member updateMember(Member member);
    void deleteMember(Long id);
    Member getMemberById(Long id);
    Member getMemberByNationalCode(String nationalCode);
    List<Member> getAllMembers();
    List<Member> searchMembers(String keyword);
    List<Member> getMembersByName(String name);
    boolean isNationalCodeAvailable(String nationalCode);
}
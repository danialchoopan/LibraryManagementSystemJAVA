package com.library.service;

import com.library.entity.Member;
import com.library.exception.MemberNotFoundException;
import com.library.repository.MemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class MemberServiceImpl implements MemberService {
    private static final Logger logger = LoggerFactory.getLogger(MemberServiceImpl.class);
    private final MemberRepository memberRepository;

    public MemberServiceImpl(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public Member addMember(Member member) {
        if (memberRepository.existsByNationalCode(member.getNationalCode())) {
            throw new IllegalArgumentException("Member with national code " + member.getNationalCode() + " already exists");
        }
        logger.info("Adding new member: {}", member.getName());
        return memberRepository.save(member);
    }

    @Override
    public Member updateMember(Member member) {
        if (member.getId() == null) {
            throw new IllegalArgumentException("Member ID cannot be null for update");
        }
        if (!memberRepository.findById(member.getId()).isPresent()) {
            throw new MemberNotFoundException(member.getId());
        }
        logger.info("Updating member with ID: {}", member.getId());
        return memberRepository.save(member);
    }

    @Override
    public void deleteMember(Long id) {
        if (!memberRepository.findById(id).isPresent()) {
            throw new MemberNotFoundException(id);
        }
        logger.info("Deleting member with ID: {}", id);
        memberRepository.deleteById(id);
    }

    @Override
    public Member getMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new MemberNotFoundException(id));
    }

    @Override
    public Member getMemberByNationalCode(String nationalCode) {
        return memberRepository.findByNationalCode(nationalCode)
                .orElseThrow(() -> new MemberNotFoundException("Member not found with national code: " + nationalCode));
    }

    @Override
    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    @Override
    public List<Member> searchMembers(String keyword) {
        return memberRepository.findByName(keyword);
    }

    @Override
    public List<Member> getMembersByName(String name) {
        return memberRepository.findByName(name);
    }

    @Override
    public boolean isNationalCodeAvailable(String nationalCode) {
        return !memberRepository.existsByNationalCode(nationalCode);
    }
}
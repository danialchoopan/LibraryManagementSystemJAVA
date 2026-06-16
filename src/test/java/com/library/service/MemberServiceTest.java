package com.library.service;

import com.library.entity.Member;
import com.library.exception.MemberNotFoundException;
import com.library.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberServiceImpl memberService;

    private Member testMember;

    @BeforeEach
    void setUp() {
        testMember = new Member("John Doe", "1234567890", "+1-555-0101", LocalDate.of(2023, 1, 15));
        testMember.setId(1L);
    }

    @Test
    void addMember_Success() {
        when(memberRepository.existsByNationalCode("1234567890")).thenReturn(false);
        when(memberRepository.save(any(Member.class))).thenReturn(testMember);

        Member result = memberService.addMember(testMember);

        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        verify(memberRepository).existsByNationalCode("1234567890");
        verify(memberRepository).save(testMember);
    }

    @Test
    void addMember_DuplicateNationalCode_ThrowsException() {
        when(memberRepository.existsByNationalCode("1234567890")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> memberService.addMember(testMember));
        verify(memberRepository).existsByNationalCode("1234567890");
        verify(memberRepository, never()).save(any());
    }

    @Test
    void getMemberById_Found() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));

        Member result = memberService.getMemberById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(memberRepository).findById(1L);
    }

    @Test
    void getMemberById_NotFound_ThrowsException() {
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(MemberNotFoundException.class, () -> memberService.getMemberById(1L));
        verify(memberRepository).findById(1L);
    }

    @Test
    void getAllMembers() {
        List<Member> members = Arrays.asList(testMember, new Member("Jane Doe", "0987654321", "+1-555-0102", LocalDate.of(2023, 2, 20)));
        when(memberRepository.findAll()).thenReturn(members);

        List<Member> result = memberService.getAllMembers();

        assertEquals(2, result.size());
        verify(memberRepository).findAll();
    }

    @Test
    void searchMembers() {
        List<Member> members = Arrays.asList(testMember);
        when(memberRepository.findByName("John")).thenReturn(members);

        List<Member> result = memberService.searchMembers("John");

        assertEquals(1, result.size());
        verify(memberRepository).findByName("John");
    }

    @Test
    void isNationalCodeAvailable_True() {
        when(memberRepository.existsByNationalCode("1234567890")).thenReturn(false);

        assertTrue(memberService.isNationalCodeAvailable("1234567890"));
        verify(memberRepository).existsByNationalCode("1234567890");
    }

    @Test
    void isNationalCodeAvailable_False() {
        when(memberRepository.existsByNationalCode("1234567890")).thenReturn(true);

        assertFalse(memberService.isNationalCodeAvailable("1234567890"));
        verify(memberRepository).existsByNationalCode("1234567890");
    }
}
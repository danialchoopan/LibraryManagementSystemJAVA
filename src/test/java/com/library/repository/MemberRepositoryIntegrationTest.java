package com.library.repository;

import com.library.entity.Member;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MemberRepositoryIntegrationTest {

    private static MemberRepositoryImpl repo;

    @BeforeAll
    static void setUp() {
        repo = new MemberRepositoryImpl();
    }

    @Test
    @Order(1)
    void insertMember() {
        Member member = new Member("Ali Rezaei", "1000000001", "+989121111111", LocalDate.of(2024, 1, 1));
        Member saved = repo.save(member);

        assertNotNull(saved.getId());
        assertEquals("Ali Rezaei", saved.getName());
        assertEquals("1000000001", saved.getNationalCode());
        assertEquals(LocalDate.of(2024, 1, 1), saved.getJoinDate());
    }

    @Test
    @Order(2)
    void findById() {
        Member member = new Member("Find Member", "1000000002", "+989122222222", LocalDate.of(2024, 2, 1));
        Member saved = repo.save(member);

        Optional<Member> found = repo.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Find Member", found.get().getName());
    }

    @Test
    @Order(3)
    void findByNationalCode() {
        Member member = new Member("Code Member", "1000000003", "+989123333333", LocalDate.of(2024, 3, 1));
        repo.save(member);

        Optional<Member> found = repo.findByNationalCode("1000000003");
        assertTrue(found.isPresent());
        assertEquals("Code Member", found.get().getName());
    }

    @Test
    @Order(4)
    void updateMember() {
        Member member = new Member("Original Name", "1000000004", "+989124444444", LocalDate.of(2024, 4, 1));
        Member saved = repo.save(member);

        saved.setName("Updated Name");
        saved.setPhoneNumber("+989129999999");
        repo.save(saved);

        Optional<Member> found = repo.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Updated Name", found.get().getName());
        assertEquals("+989129999999", found.get().getPhoneNumber());
    }

    @Test
    @Order(5)
    void deleteMember() {
        Member member = new Member("Delete Me", "1000000005", "+989125555555", LocalDate.of(2024, 5, 1));
        Member saved = repo.save(member);

        boolean deleted = repo.deleteById(saved.getId());
        assertTrue(deleted);

        Optional<Member> found = repo.findById(saved.getId());
        assertFalse(found.isPresent());
    }

    @Test
    @Order(6)
    void findAll() {
        int before = repo.findAll().size();
        repo.save(new Member("List1", "1000000006", "+989126000001", LocalDate.of(2024, 6, 1)));
        repo.save(new Member("List2", "1000000007", "+989126000002", LocalDate.of(2024, 6, 2)));

        List<Member> all = repo.findAll();
        assertTrue(all.size() >= before + 2);
    }

    @Test
    @Order(7)
    void searchByName() {
        repo.save(new Member("UniqueSearchPerson", "1000000008", "+989127000001", LocalDate.of(2024, 7, 1)));

        List<Member> results = repo.findByName("UniqueSearchPerson");
        assertFalse(results.isEmpty());
        assertEquals("UniqueSearchPerson", results.get(0).getName());
    }

    @Test
    @Order(8)
    void existsByNationalCode() {
        repo.save(new Member("Exists Member", "1000000009", "+989128000001", LocalDate.of(2024, 8, 1)));

        assertTrue(repo.existsByNationalCode("1000000009"));
        assertFalse(repo.existsByNationalCode("9999999999"));
    }

    @Test
    @Order(9)
    void count() {
        long count = repo.count();
        assertTrue(count > 0);
    }

    @Test
    @Order(10)
    void insertDuplicateNationalCode_ThrowsException() {
        repo.save(new Member("Dup1", "1000000010", "+989129000001", LocalDate.of(2024, 9, 1)));
        assertThrows(RuntimeException.class, () ->
                repo.save(new Member("Dup2", "1000000010", "+989129000002", LocalDate.of(2024, 9, 2))));
    }
}

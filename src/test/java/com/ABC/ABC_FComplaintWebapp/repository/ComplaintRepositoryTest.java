package com.ABC.ABC_FComplaintWebapp.repository;

import com.ABC.ABC_FComplaintWebapp.model.Complaint;
import com.ABC.ABC_FComplaintWebapp.repositories.ComplaintRepo;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)  // Use H2
@DisplayName("Complaint Repository Tests")
class ComplaintRepositoryTest {

    @Autowired
    private ComplaintRepo complaintRepository;

    @Autowired
    private EntityManager entityManager;

    private Complaint complaint;
    private Complaint complaint2;
    private Complaint complaint3;

    @BeforeEach
    void setUp() {
        complaint = new Complaint();
        complaint.setUserId(101);
        complaint.setUserName("Sarah Johnson");
        complaint.setTitle("ATM retained my card");
        complaint.setDescription("ATM issue description");
        complaint.setCategory("ATM Issue");
        complaint.setStatus("Pending");
        complaint.setCreatedAt(LocalDateTime.now());

        complaint2 = new Complaint();
        complaint2.setUserId(102);
        complaint2.setUserName("Daniel Parker");
        complaint2.setTitle("Suspicious online transaction");
        complaint2.setDescription("Fraud description");
        complaint2.setCategory("Fraud");
        complaint2.setStatus("In Process");
        complaint2.setCreatedAt(LocalDateTime.now());

        complaint3 = new Complaint();
        complaint3.setUserId(101);
        complaint3.setUserName("Sarah Johnson");
        complaint3.setTitle("Delayed card delivery");
        complaint3.setDescription("Card delivery description");
        complaint3.setCategory("Card Delivery");
        complaint3.setStatus("Closed");
        complaint3.setAdminResponse("A new card has been dispatched.");
        complaint3.setCreatedAt(LocalDateTime.now());
    }

    // SAVE AND FIND TESTS

    @Test
    @DisplayName("Should save complaint to database")
    void testSaveComplaint() {
        Complaint saved = complaintRepository.save(complaint);
        entityManager.flush();

        assertNotNull(saved.getId());
        assertEquals("Sarah Johnson", saved.getUserName());
    }

    @Test
    @DisplayName("Should auto-generate ID when saving")
    void testSaveGeneratesId() {
        Complaint saved = complaintRepository.save(complaint);

        assertNotNull(saved.getId());
        assertTrue(saved.getId() > 0);
    }

    @Test
    @DisplayName("Should find complaint by ID")
    void testFindById() {
        complaintRepository.save(complaint);
        entityManager.flush();

        Optional<Complaint> found = complaintRepository.findById(complaint.getId());

        assertTrue(found.isPresent());
        assertEquals("Sarah Johnson", found.get().getUserName());
    }

    @Test
    @DisplayName("Should return empty optional for non-existent ID")
    void testFindByIdNotFound() {
        Optional<Complaint> found = complaintRepository.findById(999L);
        assertFalse(found.isPresent());
    }

    // SAVE MULTIPLE TESTS

    @Test
    @DisplayName("Should save multiple complaints")
    void testSaveMultipleComplaints() {
        complaintRepository.save(complaint);
        complaintRepository.save(complaint2);
        complaintRepository.save(complaint3);
        entityManager.flush();

        assertEquals(3, complaintRepository.count());
    }

    @Test
    @DisplayName("Should auto-increment ID for each new complaint")
    void testIdIncrementOnMultipleSave() {
        Complaint saved1 = complaintRepository.save(complaint);
        Complaint saved2 = complaintRepository.save(complaint2);
        entityManager.flush();

        assertTrue(saved2.getId() > saved1.getId());
    }

    // FIND BY STATUS TESTS

    @Test
    @DisplayName("Should find complaints by status")
    void testFindByStatus() {
        complaintRepository.save(complaint);
        complaintRepository.save(complaint2);
        complaintRepository.save(complaint3);
        entityManager.flush();

        List<Complaint> pendingComplaints = complaintRepository.findByStatus("Pending");

        assertEquals(1, pendingComplaints.size());
        assertEquals("Pending", pendingComplaints.get(0).getStatus());
    }

    @Test
    @DisplayName("Should find multiple complaints with same status")
    void testFindMultipleByStatus() {
        Complaint complaint4 = new Complaint();
        complaint4.setUserId(103);
        complaint4.setUserName("Test User");
        complaint4.setTitle("Test complaint");
        complaint4.setDescription("Test description");
        complaint4.setCategory("Technical Issue");
        complaint4.setStatus("Pending");
        complaint4.setCreatedAt(LocalDateTime.now());

        complaintRepository.save(complaint);
        complaintRepository.save(complaint4);
        entityManager.flush();

        List<Complaint> pendingComplaints = complaintRepository.findByStatus("Pending");

        assertEquals(2, pendingComplaints.size());
        assertTrue(pendingComplaints.stream()
            .allMatch(c -> "Pending".equals(c.getStatus())));
    }

    // FIND BY USER ID TESTS

    @Test
    @DisplayName("Should find complaints by user ID")
    void testFindByUserId() {
        complaintRepository.save(complaint);
        complaintRepository.save(complaint2);
        entityManager.flush();

        List<Complaint> userComplaints = complaintRepository.findByUserId(101);

        assertEquals(1, userComplaints.size());
        assertEquals(101, userComplaints.get(0).getUserId());
    }

    @Test
    @DisplayName("Should return empty list when user has no complaints")
    void testFindByUserIdEmpty() {
        complaintRepository.save(complaint);
        entityManager.flush();

        List<Complaint> userComplaints = complaintRepository.findByUserId(999);

        assertEquals(0, userComplaints.size());
    }

    // FIND ALL TESTS

    @Test
    @DisplayName("Should find all complaints")
    void testFindAll() {
        complaintRepository.save(complaint);
        complaintRepository.save(complaint2);
        complaintRepository.save(complaint3);
        entityManager.flush();

        List<Complaint> allComplaints = complaintRepository.findAllByOrderByCreatedAtDesc();

        assertEquals(3, allComplaints.size());
    }

    @Test
    @DisplayName("Should return empty list when no complaints exist")
    void testFindAllEmpty() {
        List<Complaint> all = complaintRepository.findAllByOrderByCreatedAtDesc();
        assertEquals(0, all.size());
    }

    // UPDATE TESTS

    @Test
    @DisplayName("Should update complaint details")
    void testUpdateComplaint() {
        complaintRepository.save(complaint);
        entityManager.flush();
        Long id = complaint.getId();

        complaint.setStatus("Closed");
        complaint.setAdminResponse("Resolved");
        complaintRepository.save(complaint);
        entityManager.flush();

        Complaint updated = complaintRepository.findById(id).get();
        assertEquals("Closed", updated.getStatus());
        assertEquals("Resolved", updated.getAdminResponse());
    }

    // DELETE TESTS

    @Test
    @DisplayName("Should delete complaint")
    void testDeleteComplaint() {
        complaintRepository.save(complaint);
        entityManager.flush();
        Long id = complaint.getId();

        complaintRepository.deleteById(id);
        entityManager.flush();

        assertFalse(complaintRepository.findById(id).isPresent());
    }

    @Test
    @DisplayName("Should delete all complaints")
    void testDeleteAll() {
        complaintRepository.save(complaint);
        complaintRepository.save(complaint2);
        entityManager.flush();

        complaintRepository.deleteAll();
        entityManager.flush();

        assertEquals(0, complaintRepository.count());
    }
}
package com.ABC.ABC_FComplaintWebapp.integration;

import com.ABC.ABC_FComplaintWebapp.model.Complaint;
import com.ABC.ABC_FComplaintWebapp.repositories.ComplaintRepo;
import com.ABC.ABC_FComplaintWebapp.service.ComplaintService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")  // Uses application-test.properties (complaint_db)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)  // Use real MySQL
@Transactional  // Rollback after each test
@DisplayName("Complaint Integration Tests with MySQL (complaint_db)")
class ComplaintIntegrationTest {

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private ComplaintRepo complaintRepository;

    private Complaint complaint;
    private Complaint complaint2;

    @BeforeEach
    void setUp() {
        complaintRepository.deleteAll();

        complaint = new Complaint();
        complaint.setUserId(101);
        complaint.setUserName("Sarah Johnson");
        complaint.setTitle("ATM retained my card");
        complaint.setDescription("ATM froze and retained my debit card.");
        complaint.setCategory("ATM Issue");

        complaint2 = new Complaint();
        complaint2.setUserId(102);
        complaint2.setUserName("Daniel Parker");
        complaint2.setTitle("Suspicious online transaction");
        complaint2.setDescription("Unauthorised £245 transaction.");
        complaint2.setCategory("Fraud");
    }

    // CREATE TESTS

    @Test
    @DisplayName("Should create and retrieve complaint from MySQL")
    void testCreateAndRetrieveComplaint() {
        Complaint saved = complaintService.createComplaint(complaint);

        assertNotNull(saved.getId());
        assertEquals("Sarah Johnson", saved.getUserName());
        assertEquals("Pending", saved.getStatus());

        Complaint retrieved = complaintService.getComplaintById(saved.getId());
        assertNotNull(retrieved);
    }

    @Test
    @DisplayName("Should persist complaint fully")
    void testComplaintPersistenceWithAllFields() {
        complaint.setUserId(105);
        complaint.setUserName("Test User");
        complaint.setTitle("Test Title");
        complaint.setDescription("Test Description");
        complaint.setCategory("Technical Issue");

        Complaint saved = complaintService.createComplaint(complaint);
        Complaint retrieved = complaintService.getComplaintById(saved.getId());

        assertEquals("Test User", retrieved.getUserName());
        assertEquals("Test Description", retrieved.getDescription());
    }

    @Test
    @DisplayName("Should auto-increment MySQL ID")
    void testIdIncrement() {
        Complaint c1 = complaintService.createComplaint(complaint);
        Complaint c2 = complaintService.createComplaint(complaint2);

        assertTrue(c2.getId() > c1.getId());
    }

    // UPDATE TESTS

    @Test
    @DisplayName("Should update admin response")
    void testUpdateAdminResponse() {
        Complaint saved = complaintService.createComplaint(complaint);

        Complaint updated = complaintService.updateAdminResponse(
                saved.getId(),
                "A replacement card has been issued.",
                "Closed"
        );

        assertEquals("Closed", updated.getStatus());
        assertEquals("A replacement card has been issued.", updated.getAdminResponse());
    }

    @Test
    @DisplayName("Should preserve original fields during admin update")
    void testUpdatePreservesFields() {
        Complaint saved = complaintService.createComplaint(complaint);

        String originalTitle = saved.getTitle();
        String originalDesc = saved.getDescription();

        complaintService.updateAdminResponse(saved.getId(), "Response", "In Process");

        Complaint updated = complaintService.getComplaintById(saved.getId());

        assertEquals(originalTitle, updated.getTitle());
        assertEquals(originalDesc, updated.getDescription());
    }

    @Test
    @DisplayName("Should return null if updating missing complaint")
    void testUpdateNonExisting() {
        Complaint updated = complaintService.updateAdminResponse(999L, "Response", "Closed");
        assertNull(updated);
    }

    // GET TESTS

    @Test
    @DisplayName("Should retrieve complaint by ID")
    void testGetComplaintById() {
        Complaint saved = complaintService.createComplaint(complaint);
        Complaint retrieved = complaintService.getComplaintById(saved.getId());

        assertNotNull(retrieved);
        assertEquals(saved.getId(), retrieved.getId());
    }

    @Test
    @DisplayName("Should return null for missing ID")
    void testGetNonExisting() {
        assertNull(complaintService.getComplaintById(999L));
    }

    // LIST TESTS

    @Test
    @DisplayName("Should list all complaints")
    void testGetAll() {
        complaintService.createComplaint(complaint);
        complaintService.createComplaint(complaint2);

        List<Complaint> list = complaintService.getAllComplaints();
        assertEquals(2, list.size());
    }

    @Test
    @DisplayName("Should return empty list when DB is empty")
    void testGetAllEmpty() {
        List<Complaint> list = complaintService.getAllComplaints();
        assertTrue(list.isEmpty());
    }

    @Test
    @DisplayName("Should return complaints ordered by createdAt DESC")
    void testGetAllOrdered() throws InterruptedException {
        Complaint c1 = complaintService.createComplaint(complaint);

        Thread.sleep(50);

        Complaint c2 = complaintService.createComplaint(complaint2);

        List<Complaint> list = complaintService.getAllComplaints();

        assertTrue(list.get(0).getCreatedAt().isAfter(list.get(1).getCreatedAt()));
    }

    // STATUS TESTS

    @Test
    @DisplayName("Should get complaints by status")
    void testGetByStatus() {
        Complaint saved = complaintService.createComplaint(complaint);
        complaintService.updateAdminResponse(saved.getId(), "response", "Closed");

        List<Complaint> closed = complaintService.getComplaintsByStatus("Closed");
        List<Complaint> pending = complaintService.getComplaintsByStatus("Pending");

        assertEquals(1, closed.size());
        assertEquals(1, pending.size());
    }

    @Test
    @DisplayName("Should return empty list by status")
    void testGetByStatusEmpty() {
        complaintService.createComplaint(complaint);

        List<Complaint> list = complaintService.getComplaintsByStatus("Closed");
        assertTrue(list.isEmpty());
    }

    // USER ID TESTS

    @Test
    @DisplayName("Should get complaints by user ID")
    void testGetByUserId() {
        complaintService.createComplaint(complaint);
        complaintService.createComplaint(complaint2);

        List<Complaint> list = complaintService.getComplaintsByUserId(101);

        assertEquals(1, list.size());
    }

    @Test
    @DisplayName("Should return empty list for user with no complaints")
    void testGetByUserIdEmpty() {
        List<Complaint> list = complaintService.getComplaintsByUserId(999);
        assertTrue(list.isEmpty());
    }
}
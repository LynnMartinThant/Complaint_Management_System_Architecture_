package com.ABC.ABC_FComplaintWebapp.service;

import com.ABC.ABC_FComplaintWebapp.model.Complaint;
import com.ABC.ABC_FComplaintWebapp.repositories.ComplaintRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Complaint Service Unit Tests")
class ComplaintServiceTest {

    @Mock
    private ComplaintRepo complaintRepository;

    @InjectMocks
    private ComplaintService complaintService;

    private Complaint complaint;
    private Complaint complaint2;

    @BeforeEach
    void setUp() {
        complaint = new Complaint();
        complaint.setId(1L);
        complaint.setUserId(101);
        complaint.setUserName("Sarah Johnson");
        complaint.setTitle("ATM retained my card");
        complaint.setDescription("While withdrawing cash at the ATM on Oxford Street, the machine froze and retained my debit card.");
        complaint.setCategory("ATM Issue");
        complaint.setStatus("Pending");
        complaint.setCreatedAt(LocalDateTime.now());

        complaint2 = new Complaint();
        complaint2.setId(2L);
        complaint2.setUserId(102);
        complaint2.setUserName("Daniel Parker");
        complaint2.setTitle("Suspicious online transaction");
        complaint2.setDescription("I found an unauthorised £245 transaction on my account labelled as an online purchase I did not make.");
        complaint2.setCategory("Fraud");
        complaint2.setStatus("In Process");
        complaint2.setCreatedAt(LocalDateTime.now());
    }

    // ==================== CREATE COMPLAINT TESTS ====================

    @Test
    @DisplayName("Should create complaint successfully")
    void testCreateComplaintSuccess() {
        when(complaintRepository.save(any(Complaint.class))).thenReturn(complaint);

        Complaint result = complaintService.createComplaint(complaint);

        assertNotNull(result);
        assertEquals("Sarah Johnson", result.getUserName());
        assertEquals("ATM Issue", result.getCategory());
        assertEquals("Pending", result.getStatus());
        verify(complaintRepository, times(1)).save(any(Complaint.class));
    }

    @Test
    @DisplayName("Should set status to Pending when creating complaint")
    void testCreateComplaintSetsPendingStatus() {
        complaint.setStatus(null);
        when(complaintRepository.save(any(Complaint.class))).thenReturn(complaint);

        Complaint result = complaintService.createComplaint(complaint);

        assertEquals("Pending", result.getStatus());
    }

    @Test
    @DisplayName("Should set createdAt timestamp when creating complaint")
    void testCreateComplaintSetsTimestamp() {
        when(complaintRepository.save(any(Complaint.class))).thenReturn(complaint);

        Complaint result = complaintService.createComplaint(complaint);

        assertNotNull(result.getCreatedAt());
        assertTrue(result.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    // ==================== UPDATE ADMIN RESPONSE TESTS ====================

    @Test
    @DisplayName("Should update admin response successfully")
    void testUpdateAdminResponseSuccess() {
        String adminResponse = "A replacement card has been issued and will arrive within 3–5 working days.";
        complaint.setAdminResponse(adminResponse);
        complaint.setStatus("Closed");
        complaint.setUpdatedAt(LocalDateTime.now());

        when(complaintRepository.findById(1L)).thenReturn(Optional.of(complaint));
        when(complaintRepository.save(any(Complaint.class))).thenReturn(complaint);

        Complaint result = complaintService.updateAdminResponse(1L, adminResponse, "Closed");

        assertNotNull(result);
        assertEquals(adminResponse, result.getAdminResponse());
        assertEquals("Closed", result.getStatus());
        assertNotNull(result.getUpdatedAt());
        verify(complaintRepository, times(1)).findById(1L);
        verify(complaintRepository, times(1)).save(any(Complaint.class));
    }

    @Test
    @DisplayName("Should return null when complaint not found for update")
    void testUpdateAdminResponseComplaintNotFound() {
        when(complaintRepository.findById(999L)).thenReturn(Optional.empty());

        Complaint result = complaintService.updateAdminResponse(999L, "Test response", "Closed");

        assertNull(result);
        verify(complaintRepository, times(1)).findById(999L);
        verify(complaintRepository, never()).save(any(Complaint.class));
    }

    @Test
    @DisplayName("Should only update admin response and status, not other fields")
    void testUpdateAdminResponseDoesNotModifyOtherFields() {
        String originalTitle = complaint.getTitle();
        String originalDescription = complaint.getDescription();
        String newAdminResponse = "Updated response";

        complaint.setAdminResponse(newAdminResponse);
        complaint.setStatus("In Process");

        when(complaintRepository.findById(1L)).thenReturn(Optional.of(complaint));
        when(complaintRepository.save(any(Complaint.class))).thenReturn(complaint);

        Complaint result = complaintService.updateAdminResponse(1L, newAdminResponse, "In Process");

        assertEquals(originalTitle, result.getTitle());
        assertEquals(originalDescription, result.getDescription());
        assertEquals(newAdminResponse, result.getAdminResponse());
    }

    // ==================== GET COMPLAINT TESTS ====================

    @Test
    @DisplayName("Should get complaint by ID successfully")
    void testGetComplaintByIdSuccess() {
        when(complaintRepository.findById(1L)).thenReturn(Optional.of(complaint));

        Complaint result = complaintService.getComplaintById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Sarah Johnson", result.getUserName());
        verify(complaintRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should return null when complaint not found by ID")
    void testGetComplaintByIdNotFound() {
        when(complaintRepository.findById(999L)).thenReturn(Optional.empty());

        Complaint result = complaintService.getComplaintById(999L);

        assertNull(result);
        verify(complaintRepository, times(1)).findById(999L);
    }

    // ==================== GET ALL COMPLAINTS TESTS ====================

    @Test
    @DisplayName("Should get all complaints successfully")
    void testGetAllComplaintsSuccess() {
        List<Complaint> complaints = Arrays.asList(complaint, complaint2);
        when(complaintRepository.findAllByOrderByCreatedAtDesc()).thenReturn(complaints);

        List<Complaint> result = complaintService.getAllComplaints();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Sarah Johnson", result.get(0).getUserName());
        verify(complaintRepository, times(1)).findAllByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("Should return empty list when no complaints exist")
    void testGetAllComplaintsEmpty() {
        when(complaintRepository.findAllByOrderByCreatedAtDesc()).thenReturn(Arrays.asList());

        List<Complaint> result = complaintService.getAllComplaints();

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(complaintRepository, times(1)).findAllByOrderByCreatedAtDesc();
    }

    // ==================== GET COMPLAINTS BY STATUS TESTS ====================

    @Test
    @DisplayName("Should get complaints by status successfully")
    void testGetComplaintsByStatusSuccess() {
        List<Complaint> pendingComplaints = Arrays.asList(complaint);
        when(complaintRepository.findByStatus("Pending")).thenReturn(pendingComplaints);

        List<Complaint> result = complaintService.getComplaintsByStatus("Pending");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Pending", result.get(0).getStatus());
        verify(complaintRepository, times(1)).findByStatus("Pending");
    }

    @Test
    @DisplayName("Should return empty list when no complaints with status found")
    void testGetComplaintsByStatusNotFound() {
        when(complaintRepository.findByStatus("NonExistent")).thenReturn(Arrays.asList());

        List<Complaint> result = complaintService.getComplaintsByStatus("NonExistent");

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    // ==================== GET COMPLAINTS BY USER ID TESTS ====================

    @Test
    @DisplayName("Should get complaints by user ID successfully")
    void testGetComplaintsByUserIdSuccess() {
        List<Complaint> userComplaints = Arrays.asList(complaint);
        when(complaintRepository.findByUserIdOrderByCreatedAtDesc(101)).thenReturn(userComplaints);

        List<Complaint> result = complaintService.getComplaintsByUserId(101);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(101, result.get(0).getUserId());
        verify(complaintRepository, times(1)).findByUserIdOrderByCreatedAtDesc(101);
    }

    @Test
    @DisplayName("Should return empty list when user has no complaints")
    void testGetComplaintsByUserIdNoComplaints() {
        when(complaintRepository.findByUserIdOrderByCreatedAtDesc(999)).thenReturn(Arrays.asList());

        List<Complaint> result = complaintService.getComplaintsByUserId(999);

        assertNotNull(result);
        assertEquals(0, result.size());
    }
}
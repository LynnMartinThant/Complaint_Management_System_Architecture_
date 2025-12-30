package com.ABC.ABC_FComplaintWebapp.controller;

import com.ABC.ABC_FComplaintWebapp.model.Complaint;
import com.ABC.ABC_FComplaintWebapp.service.ComplaintService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ComplaintController.class)  // FIX: Check your actual controller class name
@DisplayName("Complaint Controller Tests with MockMvc")
class ComplaintControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
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
        complaint.setDescription("ATM issue description");
        complaint.setCategory("ATM Issue");
        complaint.setStatus("Pending");
        complaint.setCreatedAt(LocalDateTime.now());

        complaint2 = new Complaint();
        complaint2.setId(2L);
        complaint2.setUserId(102);
        complaint2.setUserName("Daniel Parker");
        complaint2.setTitle("Suspicious online transaction");
        complaint2.setDescription("Fraud issue description");
        complaint2.setCategory("Fraud");
        complaint2.setStatus("In Process");
        complaint2.setCreatedAt(LocalDateTime.now());
    }

    // CREATE FORM TESTS

    @Test
    @DisplayName("Should display create complaint form")
    void testShowCreateForm() throws Exception {
        mockMvc.perform(get("/complaints/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("create-complaint"))
                .andExpect(model().attributeExists("complaint"))
                .andExpect(model().attributeExists("categories"));
    }

    @Test
    @DisplayName("Should create complaint successfully")
    void testCreateComplaintSuccess() throws Exception {
        when(complaintService.createComplaint(any())).thenReturn(complaint);

        mockMvc.perform(post("/complaints/create")
                .param("userId", "101")
                .param("userName", "Sarah Johnson")
                .param("title", "ATM retained my card")
                .param("description", "ATM issue description")
                .param("category", "ATM Issue"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/complaints/my-complaints/*"));

        verify(complaintService).createComplaint(any());
    }

    // MY COMPLAINTS TESTS

    @Test
    @DisplayName("Should display user's complaints")
    void testMyComplaints() throws Exception {
        when(complaintService.getComplaintsByUserId(101))
                .thenReturn(Arrays.asList(complaint));

        mockMvc.perform(get("/complaints/my-complaints/101"))
                .andExpect(status().isOk())
                .andExpect(view().name("my-complaints"))
                .andExpect(model().attribute("complaints", hasSize(1)));
    }

    // ADMIN VIEW TESTS

    @Test
    @DisplayName("Should display admin all complaints")
    void testAdminViewAll() throws Exception {
        when(complaintService.getAllComplaints())
                .thenReturn(Arrays.asList(complaint, complaint2));

        mockMvc.perform(get("/complaints/admin/all"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-complaints"))
                .andExpect(model().attribute("complaints", hasSize(2)));
    }
}
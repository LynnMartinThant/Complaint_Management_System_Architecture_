package com.ABC.ABC_FComplaintWebapp.service;



import com.ABC.ABC_FComplaintWebapp.repositories.ComplaintRepo;
import com.ABC.ABC_FComplaintWebapp.model.Complaint;
import com.ABC.ABC_FComplaintWebapp.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ComplaintService {
    
    @Autowired
    private ComplaintRepo complaintRepository;
    
    /**
     * Create a new complaint as a user
     */
    public Complaint createComplaint(Complaint complaint) {
        complaint.setCreatedAt(LocalDateTime.now());
        complaint.setStatus("Pending");
        return complaintRepository.save(complaint);
    }
    
    /**
     * Admin can ONLY update admin response and status
     * No other fields can be modified
     */
    public Complaint updateAdminResponse(Long complaintId, String adminResponse, String status) {
        Optional<Complaint> complaintOpt = complaintRepository.findById(complaintId);
        if (complaintOpt.isPresent()) {
            Complaint complaint = complaintOpt.get();
            complaint.setAdminResponse(adminResponse);
            complaint.setStatus(status);
            complaint.setUpdatedAt(LocalDateTime.now());
            return complaintRepository.save(complaint);
        }
        return null;
    }
    
    /**
     * Get a specific complaint by ID
     */
    public Complaint getComplaintById(Long id) {
        return complaintRepository.findById(id).orElse(null);
    }
    
    /**
     * Get all complaints
     */
    public List<Complaint> getAllComplaints() {
        return complaintRepository.findAllByOrderByCreatedAtDesc();
    }
    
    /**
     * Get complaints filtered by status
     */
    public List<Complaint> getComplaintsByStatus(String status) {
        return complaintRepository.findByStatus(status);
    }
    
    /**
     * Get complaints for a specific user
     */
    public List<Complaint> getComplaintsByUserId(Integer userId) {
        return complaintRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}

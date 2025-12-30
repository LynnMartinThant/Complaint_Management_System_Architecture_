package com.ABC.ABC_FComplaintWebapp.repositories;






import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ABC.ABC_FComplaintWebapp.model.Complaint;

import java.util.List;

@Repository
public interface ComplaintRepo extends JpaRepository<Complaint, Long> {
    List<Complaint> findByStatus(String status);
    List<Complaint> findByUserId(Integer userId);
    List<Complaint> findByUserIdOrderByCreatedAtDesc(Integer userId);
    List<Complaint> findAllByOrderByCreatedAtDesc();
}

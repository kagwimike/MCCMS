package com.example.mccms.repository;

import com.example.mccms.model.Comment;
import com.example.mccms.model.Deliverable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByDeliverableOrderByCreatedAtDesc(Deliverable deliverable);
}

package com.project2.repository;

import com.project2.model.Bid;
import com.project2.model.BidStatus;
import com.project2.model.Project;
import com.project2.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {
    List<Bid> findByProject(Project project);

    List<Bid> findByFreelancer(User freelancer);

    List<Bid> findByProjectAndStatus(Project project, BidStatus status);

    Optional<Bid> findByProjectAndFreelancer(Project project, User freelancer);

    boolean existsByProjectAndFreelancer(Project project, User freelancer);

    long countByProject(Project project);

    long countByFreelancerAndCreatedAtAfter(User freelancer, java.time.LocalDateTime date);

    @org.springframework.data.jpa.repository.Query("SELECT b FROM Bid b WHERE b.freelancer = :freelancer " +
            "AND (:status IS NULL OR b.status = :status) " +
            "AND (:search IS NULL OR LOWER(b.project.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(b.project.client.fullName) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Bid> searchBids(User freelancer, BidStatus status, String search);
}

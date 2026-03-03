package com.project2.service;

import com.project2.model.Bid;
import com.project2.model.BidStatus;
import com.project2.model.Project;
import com.project2.model.User;
import com.project2.repository.BidRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BidService {

    private final BidRepository bidRepository;

    public BidService(BidRepository bidRepository) {
        this.bidRepository = bidRepository;
    }

    @Transactional
    public Bid submitBid(Bid bid) {
        if (bidRepository.existsByProjectAndFreelancer(bid.getProject(), bid.getFreelancer())) {
            throw new RuntimeException("You have already submitted a bid for this project");
        }
        return bidRepository.save(bid);
    }

    public List<Bid> findByProject(Project project) {
        return bidRepository.findByProject(project);
    }

    public List<Bid> findByFreelancer(User freelancer) {
        return bidRepository.findByFreelancer(freelancer);
    }

    public List<Bid> searchBids(User freelancer, BidStatus status, String search) {
        return bidRepository.searchBids(freelancer, status, search);
    }

    public Optional<Bid> findById(Long id) {
        return bidRepository.findById(id);
    }

    @Transactional
    public void withdrawBid(Long bidId) {
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new RuntimeException("Bid not found"));
        bid.setStatus(BidStatus.WITHDRAWN);
        bidRepository.save(bid);
    }

    public long countBidsForProject(Project project) {
        return bidRepository.countByProject(project);
    }
}

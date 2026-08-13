package com.nurseadda.project.repository;

import com.nurseadda.project.entity.StaffProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StaffProfileRepository extends JpaRepository<StaffProfile, Long> {

    Optional<StaffProfile> findByUserId(Long userId);

    /**
     * Paginated list with the owner {@code User} fetched eagerly via an entity
     * graph (single query instead of N+1 secondary selects).
     */
    @Override
    @EntityGraph(attributePaths = "user")
    Page<StaffProfile> findAll(Pageable pageable);
}

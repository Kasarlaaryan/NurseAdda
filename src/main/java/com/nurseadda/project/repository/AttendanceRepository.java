package com.nurseadda.project.repository;

import com.nurseadda.project.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findByStaffProfileId(Long staffProfileId);

    List<Attendance> findByAssignmentId(Long assignmentId);

    Optional<Attendance> findByStaffProfileIdAndDate(Long staffProfileId, LocalDate date);

    @Query("SELECT a FROM Attendance a WHERE a.staffProfile.id = :staffProfileId " +
            "AND a.date BETWEEN :startDate AND :endDate")
    List<Attendance> findByStaffAndDateRange(
            @Param("staffProfileId") Long staffProfileId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT a FROM Attendance a WHERE a.assignment.id = :assignmentId " +
            "AND a.date BETWEEN :startDate AND :endDate")
    List<Attendance> findByAssignmentAndDateRange(
            @Param("assignmentId") Long assignmentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT SUM(a.workingHours) FROM Attendance a WHERE a.staffProfile.id = :staffProfileId " +
            "AND a.date BETWEEN :startDate AND :endDate AND a.status = 'CHECKED_OUT'")
    Double getTotalWorkingHours(
            @Param("staffProfileId") Long staffProfileId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}

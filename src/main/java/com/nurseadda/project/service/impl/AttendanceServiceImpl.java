package com.nurseadda.project.service.impl;

import com.nurseadda.project.common.exception.ResourceNotFoundException;
import com.nurseadda.project.dto.request.CheckInRequest;
import com.nurseadda.project.dto.request.CheckOutRequest;
import com.nurseadda.project.dto.response.AttendanceResponse;
import com.nurseadda.project.entity.Assignment;
import com.nurseadda.project.entity.Attendance;
import com.nurseadda.project.entity.StaffProfile;
import com.nurseadda.project.entity.User;
import com.nurseadda.project.enums.AssignmentStatus;
import com.nurseadda.project.repository.*;
import com.nurseadda.project.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final UserRepository userRepository;
    private final StaffProfileRepository staffProfileRepository;
    private final AssignmentRepository assignmentRepository;
    private final AttendanceRepository attendanceRepository;

    @Override
    @Transactional
    public AttendanceResponse checkIn(String staffEmail, CheckInRequest request) {
        User user = userRepository.findByEmail(staffEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + staffEmail));

        StaffProfile staffProfile = staffProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Staff profile not found"));

        Assignment assignment = assignmentRepository.findById(request.getAssignmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + request.getAssignmentId()));

        // Verify this assignment belongs to this staff
        if (!assignment.getStaffProfile().getId().equals(staffProfile.getId())) {
            throw new IllegalArgumentException("This assignment is not assigned to you");
        }

        // Verify assignment is ACTIVE
        if (assignment.getStatus() != AssignmentStatus.ACTIVE) {
            throw new IllegalArgumentException("Can only check in for ACTIVE assignments");
        }

        // Check if already checked in today
        LocalDate today = LocalDate.now();
        Optional<Attendance> existingToday = attendanceRepository.findByStaffProfileIdAndDate(staffProfile.getId(), today);
        if (existingToday.isPresent()) {
            throw new IllegalArgumentException("You have already checked in today");
        }

        Attendance attendance = new Attendance();
        attendance.setAssignment(assignment);
        attendance.setStaffProfile(staffProfile);
        attendance.setDate(today);
        attendance.setCheckInTime(LocalDateTime.now());
        attendance.setStatus("CHECKED_IN");
        attendance.setNotes(request.getNotes());

        attendance = attendanceRepository.save(attendance);
        return mapToResponse(attendance);
    }

    @Override
    @Transactional
    public AttendanceResponse checkOut(String staffEmail, Long attendanceId, CheckOutRequest request) {
        User user = userRepository.findByEmail(staffEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + staffEmail));

        StaffProfile staffProfile = staffProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Staff profile not found"));

        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found with id: " + attendanceId));

        // Verify this attendance belongs to this staff
        if (!attendance.getStaffProfile().getId().equals(staffProfile.getId())) {
            throw new IllegalArgumentException("This attendance record does not belong to you");
        }

        // Verify not already checked out
        if ("CHECKED_OUT".equals(attendance.getStatus())) {
            throw new IllegalArgumentException("Already checked out for today");
        }

        LocalDateTime checkOutTime = LocalDateTime.now();
        attendance.setCheckOutTime(checkOutTime);
        attendance.setStatus("CHECKED_OUT");

        // Calculate working hours
        Duration duration = Duration.between(attendance.getCheckInTime(), checkOutTime);
        double hours = Math.round(duration.toMinutes() / 60.0 * 100.0) / 100.0;
        attendance.setWorkingHours(hours);

        if (request.getNotes() != null) {
            attendance.setNotes(request.getNotes());
        }

        attendance = attendanceRepository.save(attendance);
        return mapToResponse(attendance);
    }

    @Override
    public List<AttendanceResponse> getStaffAttendance(String staffEmail) {
        User user = userRepository.findByEmail(staffEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + staffEmail));

        StaffProfile staffProfile = staffProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Staff profile not found"));

        return attendanceRepository.findByStaffProfileId(staffProfile.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AttendanceResponse> getStaffAttendanceByDateRange(String staffEmail, LocalDate startDate, LocalDate endDate) {
        User user = userRepository.findByEmail(staffEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + staffEmail));

        StaffProfile staffProfile = staffProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Staff profile not found"));

        return attendanceRepository.findByStaffAndDateRange(staffProfile.getId(), startDate, endDate)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AttendanceResponse> getAttendanceByAssignment(Long assignmentId) {
        return attendanceRepository.findByAssignmentId(assignmentId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Double getTotalWorkingHours(String staffEmail, LocalDate startDate, LocalDate endDate) {
        User user = userRepository.findByEmail(staffEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + staffEmail));

        StaffProfile staffProfile = staffProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Staff profile not found"));

        Double total = attendanceRepository.getTotalWorkingHours(staffProfile.getId(), startDate, endDate);
        return total != null ? total : 0.0;
    }

    @Override
    public AttendanceResponse getTodayAttendance(String staffEmail) {
        User user = userRepository.findByEmail(staffEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + staffEmail));

        StaffProfile staffProfile = staffProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Staff profile not found"));

        Optional<Attendance> today = attendanceRepository.findByStaffProfileIdAndDate(staffProfile.getId(), LocalDate.now());
        return today.map(this::mapToResponse).orElse(null);
    }

    private AttendanceResponse mapToResponse(Attendance attendance) {
        AttendanceResponse response = new AttendanceResponse();
        response.setId(attendance.getId());
        response.setAssignmentId(attendance.getAssignment().getId());
        response.setStaffName(attendance.getStaffProfile().getUser().getFirstName() + " " + attendance.getStaffProfile().getUser().getLastName());
        response.setStaffEmail(attendance.getStaffProfile().getUser().getEmail());
        response.setDesignation(attendance.getAssignment().getStaffingRequest().getDesignation());
        response.setLocation(attendance.getAssignment().getStaffingRequest().getLocation());
        response.setDate(attendance.getDate());
        response.setCheckInTime(attendance.getCheckInTime());
        response.setCheckOutTime(attendance.getCheckOutTime());
        response.setWorkingHours(attendance.getWorkingHours());
        response.setStatus(attendance.getStatus());
        response.setNotes(attendance.getNotes());
        response.setCreatedAt(attendance.getCreatedAt());
        return response;
    }
}

package com.nurseadda.project.controller;

import com.nurseadda.project.dto.request.CheckInRequest;
import com.nurseadda.project.dto.request.CheckOutRequest;
import com.nurseadda.project.dto.response.AttendanceResponse;
import com.nurseadda.project.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/checkin")
    public ResponseEntity<AttendanceResponse> checkIn(
            Authentication authentication,
            @Valid @RequestBody CheckInRequest request
    ) {
        String email = (String) authentication.getPrincipal();
        AttendanceResponse response = attendanceService.checkIn(email, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/checkout/{attendanceId}")
    public ResponseEntity<AttendanceResponse> checkOut(
            Authentication authentication,
            @PathVariable Long attendanceId,
            @RequestBody(required = false) CheckOutRequest request
    ) {
        String email = (String) authentication.getPrincipal();
        if (request == null) {
            request = new CheckOutRequest();
        }
        AttendanceResponse response = attendanceService.checkOut(email, attendanceId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/today")
    public ResponseEntity<AttendanceResponse> getTodayAttendance(Authentication authentication) {
        String email = (String) authentication.getPrincipal();
        AttendanceResponse response = attendanceService.getTodayAttendance(email);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<AttendanceResponse>> getMyAttendance(Authentication authentication) {
        String email = (String) authentication.getPrincipal();
        List<AttendanceResponse> responses = attendanceService.getStaffAttendance(email);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/my/range")
    public ResponseEntity<List<AttendanceResponse>> getMyAttendanceByDateRange(
            Authentication authentication,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        String email = (String) authentication.getPrincipal();
        List<AttendanceResponse> responses = attendanceService.getStaffAttendanceByDateRange(email, startDate, endDate);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/my/hours")
    public ResponseEntity<Double> getTotalWorkingHours(
            Authentication authentication,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        String email = (String) authentication.getPrincipal();
        Double totalHours = attendanceService.getTotalWorkingHours(email, startDate, endDate);
        return ResponseEntity.ok(totalHours);
    }

    @GetMapping("/assignment/{assignmentId}")
    public ResponseEntity<List<AttendanceResponse>> getAttendanceByAssignment(
            @PathVariable Long assignmentId
    ) {
        List<AttendanceResponse> responses = attendanceService.getAttendanceByAssignment(assignmentId);
        return ResponseEntity.ok(responses);
    }
}

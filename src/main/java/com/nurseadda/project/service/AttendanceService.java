package com.nurseadda.project.service;

import com.nurseadda.project.dto.request.CheckInRequest;
import com.nurseadda.project.dto.request.CheckOutRequest;
import com.nurseadda.project.dto.response.AttendanceResponse;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {

    AttendanceResponse checkIn(String staffEmail, CheckInRequest request);
    AttendanceResponse checkOut(String staffEmail, Long attendanceId, CheckOutRequest request);
    List<AttendanceResponse> getStaffAttendance(String staffEmail);
    List<AttendanceResponse> getStaffAttendanceByDateRange(String staffEmail, LocalDate startDate, LocalDate endDate);
    List<AttendanceResponse> getAttendanceByAssignment(Long assignmentId);
    Double getTotalWorkingHours(String staffEmail, LocalDate startDate, LocalDate endDate);
    AttendanceResponse getTodayAttendance(String staffEmail);
}

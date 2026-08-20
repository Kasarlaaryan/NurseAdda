package com.nurseadda.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nurseadda.project.common.exception.GlobalExceptionHandler;
import com.nurseadda.project.common.exception.ResourceNotFoundException;
import com.nurseadda.project.dto.response.AssignmentResponse;
import com.nurseadda.project.dto.response.StaffDetailsResponse;
import com.nurseadda.project.dto.response.StaffingRequestResponse;
import com.nurseadda.project.enums.AssignmentStatus;
import com.nurseadda.project.enums.StaffingRequestStatus;
import com.nurseadda.project.service.AssignmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AssignmentControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Mock
    private AssignmentService assignmentService;

    @InjectMocks
    private AssignmentController assignmentController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(assignmentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // =====================================================================
    //  GET /staffing-requests/pending — Staff sees matching requests
    // =====================================================================

    @Test
    @DisplayName("GET /staffing-requests/pending: staff sees pending requests")
    void getPendingRequests_staff_returns200() throws Exception {
        StaffingRequestResponse response = new StaffingRequestResponse();
        response.setId(12L);
        response.setDesignation("ICU Nurse");
        response.setLocation("Mumbai");
        response.setStatus("PENDING");
        response.setAdvancePaid(true);
        response.setEstimatedTotal(new BigDecimal("4000.00"));
        response.setAdvanceAmount(new BigDecimal("1600.00"));

        when(assignmentService.getPendingRequestsForStaff(eq("priya@nurse.com"), nullable(com.nurseadda.project.enums.RequestType.class)))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/staffing-requests/pending")
                        .principal(new UsernamePasswordAuthenticationToken("priya@nurse.com", null,
                                List.of(new SimpleGrantedAuthority("ROLE_STAFF")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(12))
                .andExpect(jsonPath("$[0].designation").value("ICU Nurse"))
                .andExpect(jsonPath("$[0].advancePaid").value(true));

        verify(assignmentService).getPendingRequestsForStaff(eq("priya@nurse.com"), nullable(com.nurseadda.project.enums.RequestType.class));
    }

    @Test
    @DisplayName("GET /staffing-requests/pending: non-staff gets 403")
    void getPendingRequests_nonStaff_returns403() throws Exception {
        mockMvc.perform(get("/api/staffing-requests/pending")
                        .principal(new UsernamePasswordAuthenticationToken("rahul@hospital.com", null,
                                List.of(new SimpleGrantedAuthority("ROLE_USER")))))
                .andExpect(status().isForbidden());

        verify(assignmentService, never()).getPendingRequestsForStaff(any());
    }

    // =====================================================================
    //  POST /staffing-requests/{id}/accept — Staff accepts request
    // =====================================================================

    @Test
    @DisplayName("POST /staffing-requests/{id}/accept: staff accepts returns 201")
    void acceptStaffingRequest_valid_returns201() throws Exception {
        AssignmentResponse response = new AssignmentResponse();
        response.setId(15L);
        response.setStaffName("Priya Sharma");
        response.setStatus(AssignmentStatus.ACTIVE);
        response.setAcceptedAt(LocalDateTime.now());

        when(assignmentService.acceptStaffingRequest(eq(12L), eq("priya@nurse.com")))
                .thenReturn(response);

        mockMvc.perform(post("/api/staffing-requests/12/accept")
                        .principal(new UsernamePasswordAuthenticationToken("priya@nurse.com", null,
                                List.of(new SimpleGrantedAuthority("ROLE_STAFF")))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(15))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(assignmentService).acceptStaffingRequest(12L, "priya@nurse.com");
    }

    @Test
    @DisplayName("POST /staffing-requests/{id}/accept: non-staff gets 403")
    void acceptStaffingRequest_nonStaff_returns403() throws Exception {
        mockMvc.perform(post("/api/staffing-requests/12/accept")
                        .principal(new UsernamePasswordAuthenticationToken("rahul@hospital.com", null,
                                List.of(new SimpleGrantedAuthority("ROLE_USER")))))
                .andExpect(status().isForbidden());

        verify(assignmentService, never()).acceptStaffingRequest(anyLong(), any());
    }

    @Test
    @DisplayName("POST /staffing-requests/{id}/accept: expired request returns 400")
    void acceptStaffingRequest_expired_returns400() throws Exception {
        when(assignmentService.acceptStaffingRequest(eq(12L), eq("priya@nurse.com")))
                .thenThrow(new IllegalArgumentException("Request has expired"));

        mockMvc.perform(post("/api/staffing-requests/12/accept")
                        .principal(new UsernamePasswordAuthenticationToken("priya@nurse.com", null,
                                List.of(new SimpleGrantedAuthority("ROLE_STAFF")))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request has expired"));
    }

    // =====================================================================
    //  POST /staffing-requests/{id}/pay-advance — Client pays advance
    // =====================================================================

    @Test
    @DisplayName("POST /staffing-requests/{id}/pay-advance: client pays returns 200")
    void payAdvance_valid_returns200() throws Exception {
        StaffingRequestResponse response = new StaffingRequestResponse();
        response.setId(12L);
        response.setAdvancePaid(true);
        response.setAdvanceAmount(new BigDecimal("1600.00"));

        when(assignmentService.payAdvance(eq(12L), eq("rahul@hospital.com"),
                eq("order_abc"), eq("pay_xyz"), eq("sig_123")))
                .thenReturn(response);

        mockMvc.perform(post("/api/staffing-requests/12/pay-advance")
                        .principal(new UsernamePasswordAuthenticationToken("rahul@hospital.com", null,
                                List.of(new SimpleGrantedAuthority("ROLE_USER"))))
                        .param("razorpayOrderId", "order_abc")
                        .param("razorpayPaymentId", "pay_xyz")
                        .param("razorpaySignature", "sig_123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.advancePaid").value(true));

        verify(assignmentService).payAdvance(12L, "rahul@hospital.com", "order_abc", "pay_xyz", "sig_123");
    }

    // =====================================================================
    //  PATCH /assignments/{id}/approve-to-client — Admin approves
    // =====================================================================

    @Test
    @DisplayName("PATCH /assignments/{id}/approve-to-client: admin approves returns 200")
    void approveAssignmentToClient_admin_returns200() throws Exception {
        AssignmentResponse response = new AssignmentResponse();
        response.setId(15L);
        response.setSentToClient(true);
        response.setSentToClientAt(LocalDateTime.now());

        when(assignmentService.approveAssignmentToClient(eq(15L), eq("admin@nurse.com"), eq("ROLE_ADMIN")))
                .thenReturn(response);

        mockMvc.perform(patch("/api/assignments/15/approve-to-client")
                        .principal(new UsernamePasswordAuthenticationToken("admin@nurse.com", null,
                                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sentToClient").value(true));

        verify(assignmentService).approveAssignmentToClient(15L, "admin@nurse.com", "ROLE_ADMIN");
    }

    // =====================================================================
    //  GET /assignments/{id}/staff-details — View staff details
    // =====================================================================

    @Test
    @DisplayName("GET /assignments/{id}/staff-details: admin sees full details")
    void getStaffDetails_admin_returns200() throws Exception {
        StaffDetailsResponse response = new StaffDetailsResponse();
        response.setAssignmentId(15L);
        response.setStaffName("Priya Sharma");
        response.setStaffCategory("ICU Nurse");
        response.setVerified(true);
        response.setDocuments(List.of());

        when(assignmentService.getStaffDetailsForAssignment(eq(15L), eq("admin@nurse.com"), eq("ROLE_ADMIN")))
                .thenReturn(response);

        mockMvc.perform(get("/api/assignments/15/staff-details")
                        .principal(new UsernamePasswordAuthenticationToken("admin@nurse.com", null,
                                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.staffName").value("Priya Sharma"))
                .andExpect(jsonPath("$.verified").value(true));

        verify(assignmentService).getStaffDetailsForAssignment(15L, "admin@nurse.com", "ROLE_ADMIN");
    }

    @Test
    @DisplayName("GET /assignments/{id}/staff-details: unauthorized client gets 400")
    void getStaffDetails_unauthorizedClient_returns400() throws Exception {
        when(assignmentService.getStaffDetailsForAssignment(eq(15L), eq("other@hospital.com"), eq("ROLE_USER")))
                .thenThrow(new IllegalArgumentException("You do not have access to this assignment"));

        mockMvc.perform(get("/api/assignments/15/staff-details")
                        .principal(new UsernamePasswordAuthenticationToken("other@hospital.com", null,
                                List.of(new SimpleGrantedAuthority("ROLE_USER")))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("You do not have access to this assignment"));
    }

    // =====================================================================
    //  GET /assignments/my-clients — Client sees their assignments
    // =====================================================================

    @Test
    @DisplayName("GET /assignments/my-clients: client sees their assignments")
    void getMyClientAssignments_client_returns200() throws Exception {
        AssignmentResponse response = new AssignmentResponse();
        response.setId(15L);
        response.setStaffName("Priya Sharma");
        response.setStatus(AssignmentStatus.ACTIVE);

        var pageResponse = new com.nurseadda.project.dto.response.PageResponse<>(
                List.of(response), 0, 20, 1, 1, true, true);

        when(assignmentService.getClientAssignments(eq("rahul@hospital.com"), any()))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/assignments/my-clients")
                        .principal(new UsernamePasswordAuthenticationToken("rahul@hospital.com", null,
                                List.of(new SimpleGrantedAuthority("ROLE_USER")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(15))
                .andExpect(jsonPath("$.content[0].staffName").value("Priya Sharma"));

        verify(assignmentService).getClientAssignments(eq("rahul@hospital.com"), any());
    }
}

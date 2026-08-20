package com.nurseadda.project.service.impl;

import com.nurseadda.project.common.exception.ResourceNotFoundException;
import com.nurseadda.project.dto.request.AssignmentRequest;
import com.nurseadda.project.dto.request.StaffingRequestDto;
import com.nurseadda.project.dto.response.AssignmentResponse;
import com.nurseadda.project.dto.response.StaffingRequestResponse;
import com.nurseadda.project.entity.*;
import com.nurseadda.project.enums.AssignmentStatus;
import com.nurseadda.project.enums.Role;
import com.nurseadda.project.enums.StaffingRequestStatus;
import com.nurseadda.project.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssignmentServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private StaffProfileRepository staffProfileRepository;
    @Mock
    private StaffingRequestRepository staffingRequestRepository;
    @Mock
    private AssignmentRepository assignmentRepository;
    @Mock
    private StaffDocumentRepository staffDocumentRepository;
    @Mock
    private RateConfigRepository rateConfigRepository;

    @InjectMocks
    private AssignmentServiceImpl assignmentService;

    private User clientUser;
    private Client client;
    private User staffUser;
    private StaffProfile staffProfile;
    private StaffingRequest staffingRequest;
    private RateConfig rateConfig;

    @BeforeEach
    void setUp() {
        clientUser = new User();
        clientUser.setId(1L);
        clientUser.setEmail("rahul@hospital.com");
        clientUser.setFirstName("Rahul");
        clientUser.setLastName("Mehta");
        clientUser.setRole(Role.ROLE_USER);

        client = new Client();
        client.setId(1L);
        client.setUser(clientUser);

        staffUser = new User();
        staffUser.setId(2L);
        staffUser.setEmail("priya@nurse.com");
        staffUser.setFirstName("Priya");
        staffUser.setLastName("Sharma");
        staffUser.setRole(Role.ROLE_STAFF);

        staffProfile = new StaffProfile();
        staffProfile.setId(5L);
        staffUser.setPhone("9876543211");
        staffProfile.setUser(staffUser);
        staffProfile.setStaffCategory("ICU Nurse");
        staffProfile.setLocation("Mumbai");
        staffProfile.setVerified(true);

        staffingRequest = new StaffingRequest();
        staffingRequest.setId(12L);
        staffingRequest.setClient(client);
        staffingRequest.setDesignation("ICU Nurse");
        staffingRequest.setLocation("Mumbai");
        staffingRequest.setShift("Morning Shift");
        staffingRequest.setStartDate(LocalDate.of(2026, 9, 1));
        staffingRequest.setEndDate(LocalDate.of(2026, 9, 30));
        staffingRequest.setNumberOfStaff(1);
        staffingRequest.setStatus(StaffingRequestStatus.PENDING);
        staffingRequest.setDeadline(LocalDateTime.now().plusHours(4));
        staffingRequest.setEstimatedTotal(new BigDecimal("4000.00"));
        staffingRequest.setAdvanceAmount(new BigDecimal("1600.00"));
        staffingRequest.setAdvancePaid(false);

        rateConfig = new RateConfig();
        rateConfig.setId(1L);
        rateConfig.setShiftType("8HR");
        rateConfig.setClientHourlyRate(new BigDecimal("500"));
        rateConfig.setStaffHourlyRate(new BigDecimal("300"));
        rateConfig.setOvertimeMultiplier(new BigDecimal("1.5"));
    }

    // =====================================================================
    //  acceptStaffingRequest — POSITIVE
    // =====================================================================

    @Test
    @DisplayName("acceptStaffingRequest: valid request creates assignment")
    void acceptStaffingRequest_valid_createsAssignment() {
        staffingRequest.setAdvancePaid(true);

        when(userRepository.findByEmail("priya@nurse.com")).thenReturn(Optional.of(staffUser));
        when(staffProfileRepository.findByUserId(2L)).thenReturn(Optional.of(staffProfile));
        when(staffingRequestRepository.findById(12L)).thenReturn(Optional.of(staffingRequest));
        when(assignmentRepository.hasActiveAssignment(5L)).thenReturn(false);
        when(assignmentRepository.findByStaffingRequestIdAndStaffProfileId(12L, 5L)).thenReturn(List.of());

        Assignment savedAssignment = new Assignment();
        savedAssignment.setId(15L);
        savedAssignment.setStaffProfile(staffProfile);
        savedAssignment.setStaffingRequest(staffingRequest);
        savedAssignment.setAssignedBy(staffUser);
        savedAssignment.setStatus(AssignmentStatus.ACTIVE);
        savedAssignment.setAcceptedAt(LocalDateTime.now());
        when(assignmentRepository.save(any(Assignment.class))).thenReturn(savedAssignment);
        when(staffingRequestRepository.save(any(StaffingRequest.class))).thenReturn(staffingRequest);

        AssignmentResponse response = assignmentService.acceptStaffingRequest(12L, "priya@nurse.com");

        assertNotNull(response);
        assertEquals(15L, response.getId());
        assertEquals(AssignmentStatus.ACTIVE, response.getStatus());
        assertEquals(StaffingRequestStatus.ASSIGNED, staffingRequest.getStatus());

        verify(assignmentRepository).save(any(Assignment.class));
        verify(staffingRequestRepository).save(staffingRequest);
    }

    // =====================================================================
    //  acceptStaffingRequest — NEGATIVE
    // =====================================================================

    @Test
    @DisplayName("acceptStaffingRequest: unverified staff throws exception")
    void acceptStaffingRequest_unverifiedStaff_throwsException() {
        staffProfile.setVerified(false);
        when(userRepository.findByEmail("priya@nurse.com")).thenReturn(Optional.of(staffUser));
        when(staffProfileRepository.findByUserId(2L)).thenReturn(Optional.of(staffProfile));

        assertThrows(IllegalArgumentException.class,
                () -> assignmentService.acceptStaffingRequest(12L, "priya@nurse.com"));
    }

    @Test
    @DisplayName("acceptStaffingRequest: advance not paid throws exception")
    void acceptStaffingRequest_advanceNotPaid_throwsException() {
        when(userRepository.findByEmail("priya@nurse.com")).thenReturn(Optional.of(staffUser));
        when(staffProfileRepository.findByUserId(2L)).thenReturn(Optional.of(staffProfile));
        when(staffingRequestRepository.findById(12L)).thenReturn(Optional.of(staffingRequest));

        // advancePaid is false by default
        assertThrows(IllegalArgumentException.class,
                () -> assignmentService.acceptStaffingRequest(12L, "priya@nurse.com"));
    }

    @Test
    @DisplayName("acceptStaffingRequest: expired request throws exception")
    void acceptStaffingRequest_expiredRequest_throwsException() {
        staffingRequest.setAdvancePaid(true);
        staffingRequest.setDeadline(LocalDateTime.now().minusHours(1));

        when(userRepository.findByEmail("priya@nurse.com")).thenReturn(Optional.of(staffUser));
        when(staffProfileRepository.findByUserId(2L)).thenReturn(Optional.of(staffProfile));
        when(staffingRequestRepository.findById(12L)).thenReturn(Optional.of(staffingRequest));
        when(staffingRequestRepository.save(any(StaffingRequest.class))).thenReturn(staffingRequest);

        assertThrows(IllegalArgumentException.class,
                () -> assignmentService.acceptStaffingRequest(12L, "priya@nurse.com"));

        assertEquals(StaffingRequestStatus.EXPIRED, staffingRequest.getStatus());
    }

    @Test
    @DisplayName("acceptStaffingRequest: category mismatch throws exception")
    void acceptStaffingRequest_categoryMismatch_throwsException() {
        staffingRequest.setAdvancePaid(true);
        staffingRequest.setDesignation("General Nurse"); // Different from staff category

        when(userRepository.findByEmail("priya@nurse.com")).thenReturn(Optional.of(staffUser));
        when(staffProfileRepository.findByUserId(2L)).thenReturn(Optional.of(staffProfile));
        when(staffingRequestRepository.findById(12L)).thenReturn(Optional.of(staffingRequest));

        assertThrows(IllegalArgumentException.class,
                () -> assignmentService.acceptStaffingRequest(12L, "priya@nurse.com"));
    }

    @Test
    @DisplayName("acceptStaffingRequest: staff already accepted throws exception")
    void acceptStaffingRequest_alreadyAccepted_throwsException() {
        staffingRequest.setAdvancePaid(true);

        when(userRepository.findByEmail("priya@nurse.com")).thenReturn(Optional.of(staffUser));
        when(staffProfileRepository.findByUserId(2L)).thenReturn(Optional.of(staffProfile));
        when(staffingRequestRepository.findById(12L)).thenReturn(Optional.of(staffingRequest));
        when(assignmentRepository.hasActiveAssignment(5L)).thenReturn(false);
        when(assignmentRepository.findByStaffingRequestIdAndStaffProfileId(12L, 5L))
                .thenReturn(List.of(new Assignment())); // Already exists

        assertThrows(IllegalArgumentException.class,
                () -> assignmentService.acceptStaffingRequest(12L, "priya@nurse.com"));
    }

    // =====================================================================
    //  getPendingRequestsForStaff — POSITIVE
    // =====================================================================

    @Test
    @DisplayName("getPendingRequestsForStaff: returns matching requests")
    void getPendingRequestsForStaff_valid_returnsMatching() {
        staffingRequest.setAdvancePaid(true);

        when(userRepository.findByEmail("priya@nurse.com")).thenReturn(Optional.of(staffUser));
        when(staffProfileRepository.findByUserId(2L)).thenReturn(Optional.of(staffProfile));
        when(staffingRequestRepository.findByStatus(StaffingRequestStatus.PENDING))
                .thenReturn(List.of(staffingRequest));
        when(assignmentRepository.findByStaffingRequestIdAndStaffProfileId(12L, 5L))
                .thenReturn(List.of());

        List<StaffingRequestResponse> responses = assignmentService.getPendingRequestsForStaff("priya@nurse.com");

        assertEquals(1, responses.size());
        assertEquals(12L, responses.get(0).getId());
    }

    @Test
    @DisplayName("getPendingRequestsForStaff: advance not paid filtered out")
    void getPendingRequestsForStaff_advanceNotPaid_filtered() {
        staffingRequest.setAdvancePaid(false);

        when(userRepository.findByEmail("priya@nurse.com")).thenReturn(Optional.of(staffUser));
        when(staffProfileRepository.findByUserId(2L)).thenReturn(Optional.of(staffProfile));
        when(staffingRequestRepository.findByStatus(StaffingRequestStatus.PENDING))
                .thenReturn(List.of(staffingRequest));

        List<StaffingRequestResponse> responses = assignmentService.getPendingRequestsForStaff("priya@nurse.com");

        assertTrue(responses.isEmpty());
    }

    // =====================================================================
    //  payAdvance — POSITIVE
    // =====================================================================

    @Test
    @DisplayName("payAdvance: valid payment marks advance as paid")
    void payAdvance_valid_marksAdvancePaid() {
        when(userRepository.findByEmail("rahul@hospital.com")).thenReturn(Optional.of(clientUser));
        when(clientRepository.findByUserId(1L)).thenReturn(Optional.of(client));
        when(staffingRequestRepository.findById(12L)).thenReturn(Optional.of(staffingRequest));
        when(staffingRequestRepository.save(any(StaffingRequest.class))).thenReturn(staffingRequest);

        StaffingRequestResponse response = assignmentService.payAdvance(
                12L, "rahul@hospital.com", "order_abc", "pay_xyz", "sig_123");

        assertTrue(staffingRequest.isAdvancePaid());
        assertEquals("order_abc", staffingRequest.getAdvanceRazorpayOrderId());
        assertEquals("pay_xyz", staffingRequest.getAdvanceRazorpayPaymentId());
        assertTrue(response.isAdvancePaid());
    }

    @Test
    @DisplayName("payAdvance: already paid throws exception")
    void payAdvance_alreadyPaid_throwsException() {
        staffingRequest.setAdvancePaid(true);

        when(userRepository.findByEmail("rahul@hospital.com")).thenReturn(Optional.of(clientUser));
        when(clientRepository.findByUserId(1L)).thenReturn(Optional.of(client));
        when(staffingRequestRepository.findById(12L)).thenReturn(Optional.of(staffingRequest));

        assertThrows(IllegalArgumentException.class,
                () -> assignmentService.payAdvance(12L, "rahul@hospital.com", "order_abc", "pay_xyz", "sig_123"));
    }

    @Test
    @DisplayName("payAdvance: wrong client throws exception")
    void payAdvance_wrongClient_throwsException() {
        User otherUser = new User();
        otherUser.setId(99L);
        Client otherClient = new Client();
        otherClient.setId(99L);
        otherClient.setUser(otherUser);

        when(userRepository.findByEmail("other@hospital.com")).thenReturn(Optional.of(otherUser));
        when(clientRepository.findByUserId(99L)).thenReturn(Optional.of(otherClient));
        when(staffingRequestRepository.findById(12L)).thenReturn(Optional.of(staffingRequest));

        assertThrows(IllegalArgumentException.class,
                () -> assignmentService.payAdvance(12L, "other@hospital.com", "order_abc", "pay_xyz", "sig_123"));
    }
}

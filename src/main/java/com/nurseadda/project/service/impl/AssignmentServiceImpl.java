package com.nurseadda.project.service.impl;

import com.nurseadda.project.dto.request.AssignmentRequest;
import com.nurseadda.project.dto.request.AssignmentStatusUpdate;
import com.nurseadda.project.dto.request.StaffingRequestDto;
import com.nurseadda.project.dto.response.AssignmentResponse;
import com.nurseadda.project.dto.response.PageResponse;
import com.nurseadda.project.dto.response.StaffDetailsResponse;
import com.nurseadda.project.dto.response.StaffingRequestResponse;
import com.nurseadda.project.entity.*;
import com.nurseadda.project.enums.AssignmentStatus;
import com.nurseadda.project.enums.RequestType;
import com.nurseadda.project.enums.Role;
import com.nurseadda.project.enums.StaffingRequestStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import com.nurseadda.project.common.exception.ResourceNotFoundException;
import com.nurseadda.project.repository.*;
import com.nurseadda.project.service.AssignmentService;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final StaffProfileRepository staffProfileRepository;
    private final StaffingRequestRepository staffingRequestRepository;
    private final AssignmentRepository assignmentRepository;
    private final StaffDocumentRepository staffDocumentRepository;
    private final RateConfigRepository rateConfigRepository;
    private final com.nurseadda.project.service.EmailService emailService;

    // ─────────────────────────────────────────────
    // Staffing Requests
    // ─────────────────────────────────────────────

    @Override
    @Transactional
    public StaffingRequestResponse createStaffingRequest(String clientEmail, StaffingRequestDto request) {
        User user = userRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + clientEmail));

        Client client = clientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Client profile not found"));

        StaffingRequest staffingRequest = new StaffingRequest();
        staffingRequest.setClient(client);
        staffingRequest.setDesignation(request.getDesignation());
        staffingRequest.setLocation(request.getLocation());
        staffingRequest.setShift(request.getShift());
        staffingRequest.setStartDate(request.getStartDate());
        staffingRequest.setEndDate(request.getEndDate());
        staffingRequest.setNumberOfStaff(request.getNumberOfStaff());
        staffingRequest.setRequiredSkills(request.getRequiredSkills());
        staffingRequest.setRequestType(request.getRequestType());
        staffingRequest.setDeadline(LocalDateTime.now().plusHours(4));

        // Calculate 40% advance based on rate config and request type
        String shiftType = determineShiftType(request.getShift());
        var rateConfigOpt = rateConfigRepository.findByShiftType(shiftType);
        if (rateConfigOpt.isPresent()) {
            var rateConfig = rateConfigOpt.get();
            BigDecimal hourlyRate = rateConfig.getClientHourlyRate();
            BigDecimal hoursPerDay = BigDecimal.valueOf(shiftType.equals("12HR") ? 12 : 8);

            BigDecimal estimatedTotal;
            if (request.getRequestType() == com.nurseadda.project.enums.RequestType.MONTHLY) {
                // Monthly: hourlyRate × hoursPerDay × 26 working days × numberOfStaff
                BigDecimal workingDays = BigDecimal.valueOf(26);
                estimatedTotal = hourlyRate.multiply(hoursPerDay).multiply(workingDays)
                        .multiply(BigDecimal.valueOf(request.getNumberOfStaff()))
                        .setScale(2, RoundingMode.HALF_UP);
            } else {
                // On-call: hourlyRate × hoursPerDay × numberOfStaff (single day)
                estimatedTotal = hourlyRate.multiply(hoursPerDay)
                        .multiply(BigDecimal.valueOf(request.getNumberOfStaff()))
                        .setScale(2, RoundingMode.HALF_UP);
            }

            BigDecimal advanceAmt = estimatedTotal.multiply(new BigDecimal("0.40")).setScale(2, RoundingMode.HALF_UP);
            staffingRequest.setEstimatedTotal(estimatedTotal);
            staffingRequest.setAdvanceAmount(advanceAmt);
        }

        staffingRequest = staffingRequestRepository.save(staffingRequest);
        return mapToStaffingRequestResponse(staffingRequest);
    }

    @Override
    public List<StaffingRequestResponse> getClientStaffingRequests(String clientEmail) {
        User user = userRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + clientEmail));

        Client client = clientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Client profile not found"));

        return staffingRequestRepository.findByClientId(client.getId())
                .stream()
                .map(this::mapToStaffingRequestResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PageResponse<StaffingRequestResponse> getClientStaffingRequests(String clientEmail, Pageable pageable) {
        User user = userRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + clientEmail));
        Client client = clientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Client profile not found"));
        return PageResponse.of(staffingRequestRepository.findByClientId(client.getId(), pageable)
                .map(this::mapToStaffingRequestResponse));
    }

    @Override
    public PageResponse<StaffingRequestResponse> getClientStaffingRequests(String clientEmail, RequestType requestType, Pageable pageable) {
        if (requestType == null) return getClientStaffingRequests(clientEmail, pageable);
        User user = userRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + clientEmail));
        Client client = clientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Client profile not found"));
        return PageResponse.of(staffingRequestRepository.findByClientIdAndRequestType(client.getId(), requestType, pageable)
                .map(this::mapToStaffingRequestResponse));
    }

    @Override
    public List<StaffingRequestResponse> getAllStaffingRequests() {
        return staffingRequestRepository.findAll()
                .stream()
                .map(this::mapToStaffingRequestResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PageResponse<StaffingRequestResponse> getAllStaffingRequests(Pageable pageable) {
        return PageResponse.of(staffingRequestRepository.findAll(pageable)
                .map(this::mapToStaffingRequestResponse));
    }

    @Override
    public PageResponse<StaffingRequestResponse> getAllStaffingRequests(RequestType requestType, Pageable pageable) {
        if (requestType == null) return getAllStaffingRequests(pageable);
        List<StaffingRequestResponse> all = staffingRequestRepository.findAll(pageable)
                .getContent().stream()
                .filter(sr -> sr.getRequestType() == requestType)
                .map(this::mapToStaffingRequestResponse)
                .collect(Collectors.toList());
        long total = all.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) total / pageable.getPageSize()));
        return new PageResponse<>(all, pageable.getPageNumber(), pageable.getPageSize(), total, totalPages, pageable.getPageNumber() == 0, all.size() < pageable.getPageSize());
    }

    @Override
    public List<StaffingRequestResponse> getPendingRequestsForStaff(String staffEmail, RequestType requestType) {
        List<StaffingRequestResponse> all = getPendingRequestsForStaff(staffEmail);
        if (requestType == null) return all;
        return all.stream()
                .filter(r -> requestType.name().equals(r.getRequestType()))
                .collect(Collectors.toList());
    }

    @Override
    public List<StaffingRequestResponse> getPendingRequestsForStaff(String staffEmail) {
        User user = userRepository.findByEmail(staffEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + staffEmail));

        StaffProfile staffProfile = staffProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Staff profile not found"));

        // Only verified staff can see requests
        if (!staffProfile.isVerified()) {
            throw new IllegalArgumentException("Only verified staff can view pending requests");
        }

        // Get pending requests and filter by matching location/category
        List<StaffingRequest> pendingRequests = staffingRequestRepository.findByStatus(StaffingRequestStatus.PENDING);

        return pendingRequests.stream()
                .filter(sr -> sr.isAdvancePaid())
                .filter(sr -> isWithinDeadline(sr))
                .filter(sr -> matchesStaffProfile(sr, staffProfile))
                .filter(sr -> !hasAlreadyAccepted(sr.getId(), staffProfile.getId()))
                .map(this::mapToStaffingRequestResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PageResponse<StaffingRequestResponse> getPendingRequestsForStaff(String staffEmail, Pageable pageable) {
        // Get all filtered (same as non-paginated)
        List<StaffingRequestResponse> all = getPendingRequestsForStaff(staffEmail);
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), all.size());
        List<StaffingRequestResponse> pageContent = start < all.size() ? all.subList(start, end) : new ArrayList<>();
        int totalPages = Math.max(1, (int) Math.ceil((double) all.size() / pageable.getPageSize()));
        return new PageResponse<>(pageContent, pageable.getPageNumber(), pageable.getPageSize(), all.size(), totalPages, start == 0, end >= all.size());
    }

    @Override
    @Transactional
    public AssignmentResponse acceptStaffingRequest(Long requestId, String staffEmail) {
        User user = userRepository.findByEmail(staffEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + staffEmail));

        StaffProfile staffProfile = staffProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Staff profile not found"));

        // Only verified staff can accept
        if (!staffProfile.isVerified()) {
            throw new IllegalArgumentException("Only verified staff can accept requests");
        }

        StaffingRequest staffingRequest = staffingRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Staffing request not found with id: " + requestId));

        // Validate request is PENDING, advance paid, and within deadline
        if (staffingRequest.getStatus() != StaffingRequestStatus.PENDING) {
            throw new IllegalArgumentException("Request is not available. Current status: " + staffingRequest.getStatus());
        }
        if (!staffingRequest.isAdvancePaid()) {
            throw new IllegalArgumentException("Advance payment not completed for this request");
        }
        if (!isWithinDeadline(staffingRequest)) {
            staffingRequest.setStatus(StaffingRequestStatus.EXPIRED);
            staffingRequestRepository.save(staffingRequest);
            throw new IllegalArgumentException("Request has expired");
        }

        // Validate staff matches the request
        if (!matchesStaffProfile(staffingRequest, staffProfile)) {
            throw new IllegalArgumentException("This request does not match your profile (location/category)");
        }

        // Check if staff already has an active assignment
        if (assignmentRepository.hasActiveAssignment(staffProfile.getId())) {
            throw new IllegalArgumentException("You already have an active assignment");
        }

        // Check if staff already accepted this request
        if (hasAlreadyAccepted(requestId, staffProfile.getId())) {
            throw new IllegalArgumentException("You have already accepted this request");
        }

        // Auto-create assignment
        Assignment assignment = new Assignment();
        assignment.setStaffProfile(staffProfile);
        assignment.setStaffingRequest(staffingRequest);
        assignment.setAssignedBy(user); // Staff assigns themselves
        assignment.setStatus(AssignmentStatus.ACTIVE); // Auto-active
        assignment.setAcceptedAt(LocalDateTime.now());
        assignment.setNotes("Auto-assigned via staff acceptance");

        assignment = assignmentRepository.save(assignment);

        // Update staffing request status
        staffingRequest.setStatus(StaffingRequestStatus.ASSIGNED);
        staffingRequestRepository.save(staffingRequest);

        // Notify admin that staff accepted the assignment
        try {
            java.util.List<com.nurseadda.project.entity.User> admins = userRepository.findByRole(com.nurseadda.project.enums.Role.ROLE_ADMIN);
            java.util.List<com.nurseadda.project.entity.User> superAdmins = userRepository.findByRole(com.nurseadda.project.enums.Role.ROLE_SUPER_ADMIN);
            java.util.List<com.nurseadda.project.entity.User> allAdmins = new java.util.ArrayList<>();
            allAdmins.addAll(admins);
            allAdmins.addAll(superAdmins);
            for (com.nurseadda.project.entity.User admin : allAdmins) {
                emailService.sendAssignmentAcceptedEmail(
                        admin.getEmail(),
                        user.getFirstName() + " " + user.getLastName(),
                        staffingRequest.getDesignation(),
                        staffingRequest.getLocation()
                );
            }
        } catch (Exception e) {
            log.error("Failed to send assignment accepted notification: {}", e.getMessage());
        }

        return mapToAssignmentResponse(assignment);
    }

    private boolean isWithinDeadline(StaffingRequest sr) {
        return sr.getDeadline() == null || LocalDateTime.now().isBefore(sr.getDeadline());
    }

    private String determineShiftType(String shift) {
        if (shift == null) return "8HR";
        return shift.toLowerCase().contains("night") || shift.toLowerCase().contains("12") ? "12HR" : "8HR";
    }

    private boolean matchesStaffProfile(StaffingRequest sr, StaffProfile staff) {
        // Match by category (designation matches staff category)
        boolean categoryMatch = sr.getDesignation() != null
                && sr.getDesignation().equalsIgnoreCase(staff.getStaffCategory());

        // Match by location (if staff has location set)
        boolean locationMatch = staff.getLocation() == null
                || staff.getLocation().isEmpty()
                || sr.getLocation() == null
                || sr.getLocation().equalsIgnoreCase(staff.getLocation());

        return categoryMatch && locationMatch;
    }

    private boolean hasAlreadyAccepted(Long requestId, Long staffProfileId) {
        List<Assignment> existing = assignmentRepository.findByStaffingRequestIdAndStaffProfileId(requestId, staffProfileId);
        return existing != null && !existing.isEmpty();
    }

    @Override
    @Transactional
    public StaffingRequestResponse payAdvance(Long requestId, String clientEmail, String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) {
        User user = userRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + clientEmail));

        Client client = clientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Client profile not found"));

        StaffingRequest staffingRequest = staffingRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Staffing request not found with id: " + requestId));

        // Verify this request belongs to the client
        if (!staffingRequest.getClient().getId().equals(client.getId())) {
            throw new IllegalArgumentException("This staffing request does not belong to you");
        }

        // Verify request is still PENDING
        if (staffingRequest.getStatus() != StaffingRequestStatus.PENDING) {
            throw new IllegalArgumentException("Request is not in PENDING status");
        }

        // Verify advance not already paid
        if (staffingRequest.isAdvancePaid()) {
            throw new IllegalArgumentException("Advance already paid for this request");
        }

        // Verify Razorpay signature (basic verification)
        if (razorpayOrderId == null || razorpayPaymentId == null || razorpaySignature == null) {
            throw new IllegalArgumentException("Invalid payment details");
        }

        // Store payment details
        staffingRequest.setAdvanceRazorpayOrderId(razorpayOrderId);
        staffingRequest.setAdvanceRazorpayPaymentId(razorpayPaymentId);
        staffingRequest.setAdvanceRazorpaySignature(razorpaySignature);
        staffingRequest.setAdvancePaid(true);

        staffingRequest = staffingRequestRepository.save(staffingRequest);
        return mapToStaffingRequestResponse(staffingRequest);
    }

    @Override
    public List<StaffingRequestResponse> getStaffingRequestsByStatus(String status) {
        StaffingRequestStatus enumStatus = StaffingRequestStatus.valueOf(status.toUpperCase());
        return staffingRequestRepository.findByStatus(enumStatus)
                .stream()
                .map(this::mapToStaffingRequestResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PageResponse<StaffingRequestResponse> getStaffingRequestsByStatus(String status, Pageable pageable) {
        StaffingRequestStatus enumStatus = StaffingRequestStatus.valueOf(status.toUpperCase());
        return PageResponse.of(staffingRequestRepository.findByStatus(enumStatus, pageable)
                .map(this::mapToStaffingRequestResponse));
    }

    @Override
    public PageResponse<StaffingRequestResponse> getStaffingRequestsByStatus(String status, RequestType requestType, Pageable pageable) {
        if (requestType == null) return getStaffingRequestsByStatus(status, pageable);
        StaffingRequestStatus enumStatus = StaffingRequestStatus.valueOf(status.toUpperCase());
        return PageResponse.of(staffingRequestRepository.findByStatusAndRequestType(enumStatus, requestType, pageable)
                .map(this::mapToStaffingRequestResponse));
    }

    @Override
    @Transactional
    public StaffingRequestResponse updateStaffingRequestStatus(Long requestId, StaffingRequestStatus newStatus, String adminEmail, String userRole) {
        // FIX 1: Verify user is admin
        if (!userRole.equals(Role.ROLE_ADMIN.name()) && !userRole.equals(Role.ROLE_SUPER_ADMIN.name())) {
            throw new IllegalArgumentException("Only admin can update staffing request status");
        }

        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with email: " + adminEmail));

        StaffingRequest staffingRequest = staffingRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Staffing request not found with id: " + requestId));

        StaffingRequestStatus currentStatus = staffingRequest.getStatus();

        // Validate status transitions
        if (newStatus == StaffingRequestStatus.APPROVED && currentStatus != StaffingRequestStatus.PENDING) {
            throw new IllegalArgumentException("Can only approve a PENDING request");
        }
        if (newStatus == StaffingRequestStatus.REJECTED && currentStatus != StaffingRequestStatus.PENDING) {
            throw new IllegalArgumentException("Can only reject a PENDING request");
        }

        staffingRequest.setStatus(newStatus);
        staffingRequest = staffingRequestRepository.save(staffingRequest);
        return mapToStaffingRequestResponse(staffingRequest);
    }

    @Override
    public List<AssignmentResponse> getStaffingRequestAssignments(Long requestId, String clientEmail) {
        User user = userRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + clientEmail));

        Client client = clientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Client profile not found"));

        StaffingRequest staffingRequest = staffingRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Staffing request not found with id: " + requestId));

        // Verify this request belongs to the client
        if (!staffingRequest.getClient().getId().equals(client.getId())) {
            throw new IllegalArgumentException("This staffing request does not belong to you");
        }

        return assignmentRepository.findByStaffingRequestId(requestId)
                .stream()
                .map(this::mapToAssignmentResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PageResponse<AssignmentResponse> getStaffingRequestAssignments(Long requestId, String clientEmail, Pageable pageable) {
        User user = userRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + clientEmail));
        Client client = clientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Client profile not found"));
        StaffingRequest staffingRequest = staffingRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Staffing request not found with id: " + requestId));
        if (!staffingRequest.getClient().getId().equals(client.getId())) {
            throw new IllegalArgumentException("This staffing request does not belong to you");
        }
        return PageResponse.of(assignmentRepository.findByStaffingRequestId(requestId, pageable)
                .map(this::mapToAssignmentResponse));
    }

    // ─────────────────────────────────────────────
    // Assignments
    // ─────────────────────────────────────────────

    @Override
    @Transactional
    public AssignmentResponse createAssignment(String adminEmail, AssignmentRequest request) {
        // FIX 1: Verify caller is admin (service-level guard)
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with email: " + adminEmail));
        if (admin.getRole() != Role.ROLE_ADMIN && admin.getRole() != Role.ROLE_SUPER_ADMIN) {
            throw new IllegalArgumentException("Only admin can create assignments");
        }

        StaffProfile staffProfile = staffProfileRepository.findById(request.getStaffProfileId())
                .orElseThrow(() -> new ResourceNotFoundException("Staff profile not found with id: " + request.getStaffProfileId()));

        // Only verified staff can be assigned
        if (!staffProfile.isVerified()) {
            throw new IllegalArgumentException("Staff member is not verified and cannot be assigned");
        }

        // Check if staff already has an active assignment (no overlapping)
        if (assignmentRepository.hasActiveAssignment(staffProfile.getId())) {
            throw new IllegalArgumentException("Staff member already has an active assignment");
        }

        StaffingRequest staffingRequest = staffingRequestRepository.findById(request.getStaffingRequestId())
                .orElseThrow(() -> new ResourceNotFoundException("Staffing request not found with id: " + request.getStaffingRequestId()));

        // Check if request has expired
        if (staffingRequest.getDeadline() != null && LocalDateTime.now().isAfter(staffingRequest.getDeadline())) {
            // Auto-expire if still PENDING
            if (staffingRequest.getStatus() == StaffingRequestStatus.PENDING || staffingRequest.getStatus() == StaffingRequestStatus.APPROVED) {
                staffingRequest.setStatus(StaffingRequestStatus.EXPIRED);
                staffingRequestRepository.save(staffingRequest);
            }
            throw new IllegalArgumentException("Staffing request has expired. Deadline was: " + staffingRequest.getDeadline());
        }

        // Only APPROVED requests can have assignments
        if (staffingRequest.getStatus() != StaffingRequestStatus.APPROVED) {
            throw new IllegalArgumentException("Staffing request must be APPROVED before assigning staff. Current status: " + staffingRequest.getStatus());
        }

        Assignment assignment = new Assignment();
        assignment.setStaffProfile(staffProfile);
        assignment.setStaffingRequest(staffingRequest);
        assignment.setAssignedBy(admin);
        assignment.setStatus(AssignmentStatus.PENDING);
        assignment.setNotes(request.getNotes());

        assignment = assignmentRepository.save(assignment);
        return mapToAssignmentResponse(assignment);
    }

    @Override
    @Transactional
    public AssignmentResponse updateAssignmentStatus(String email, Long assignmentId, AssignmentStatusUpdate update, String userRole) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));

        AssignmentStatus newStatus = update.getStatus();

        // Validate status transitions based on role
        if (userRole.equals(Role.ROLE_STAFF.name())) {
            // Staff can only accept or reject
            if (newStatus != AssignmentStatus.ACCEPTED && newStatus != AssignmentStatus.REJECTED) {
                throw new IllegalArgumentException("Staff can only accept or reject assignments");
            }
            // Verify this assignment belongs to this staff
            StaffProfile staffProfile = staffProfileRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Staff profile not found"));
            if (!assignment.getStaffProfile().getId().equals(staffProfile.getId())) {
                throw new IllegalArgumentException("This assignment is not assigned to you");
            }
            if (newStatus == AssignmentStatus.ACCEPTED) {
                assignment.setAcceptedAt(LocalDateTime.now());
            }
        } else if (userRole.equals(Role.ROLE_ADMIN.name()) || userRole.equals(Role.ROLE_SUPER_ADMIN.name())) {
            // Admin can set ACTIVE, COMPLETED, or CANCELLED
            if (newStatus != AssignmentStatus.ACTIVE
                    && newStatus != AssignmentStatus.COMPLETED
                    && newStatus != AssignmentStatus.CANCELLED) {
                throw new IllegalArgumentException("Admin can only set status to ACTIVE, COMPLETED, or CANCELLED");
            }
            if (newStatus == AssignmentStatus.COMPLETED) {
                assignment.setCompletedAt(LocalDateTime.now());
            }
        } else {
            throw new IllegalArgumentException("You do not have permission to update assignment status");
        }

        assignment.setStatus(newStatus);
        if (update.getNotes() != null) {
            assignment.setNotes(update.getNotes());
        }

        assignment = assignmentRepository.save(assignment);
        return mapToAssignmentResponse(assignment);
    }

    @Override
    public List<AssignmentResponse> getStaffAssignments(String staffEmail) {
        User user = userRepository.findByEmail(staffEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + staffEmail));

        StaffProfile staffProfile = staffProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Staff profile not found"));

        return assignmentRepository.findByStaffProfileId(staffProfile.getId())
                .stream()
                .map(this::mapToAssignmentResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PageResponse<AssignmentResponse> getStaffAssignments(String staffEmail, Pageable pageable) {
        User user = userRepository.findByEmail(staffEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + staffEmail));
        StaffProfile staffProfile = staffProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Staff profile not found"));
        return PageResponse.of(assignmentRepository.findByStaffProfileId(staffProfile.getId(), pageable)
                .map(this::mapToAssignmentResponse));
    }

    @Override
    public List<AssignmentResponse> getAllAssignments() {
        return assignmentRepository.findAll()
                .stream()
                .map(this::mapToAssignmentResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PageResponse<AssignmentResponse> getAllAssignments(Pageable pageable) {
        return PageResponse.of(assignmentRepository.findAll(pageable)
                .map(this::mapToAssignmentResponse));
    }

    @Override
    public PageResponse<AssignmentResponse> getAllAssignments(RequestType requestType, Pageable pageable) {
        if (requestType == null) return getAllAssignments(pageable);
        List<AssignmentResponse> all = assignmentRepository.findAll(pageable)
                .getContent().stream()
                .filter(a -> a.getStaffingRequest().getRequestType() == requestType)
                .map(this::mapToAssignmentResponse)
                .collect(Collectors.toList());
        long total = all.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) total / pageable.getPageSize()));
        return new PageResponse<>(all, pageable.getPageNumber(), pageable.getPageSize(), total, totalPages, pageable.getPageNumber() == 0, all.size() < pageable.getPageSize());
    }

    @Override
    public List<AssignmentResponse> getAssignmentsByStatus(AssignmentStatus status, String email, String userRole) {
        if (userRole.equals(Role.ROLE_ADMIN.name()) || userRole.equals(Role.ROLE_SUPER_ADMIN.name())) {
            // Admin sees all
            return assignmentRepository.findByStatus(status)
                    .stream()
                    .map(this::mapToAssignmentResponse)
                    .collect(Collectors.toList());
        } else if (userRole.equals(Role.ROLE_STAFF.name())) {
            // Staff sees only own
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            StaffProfile sp = staffProfileRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Staff profile not found"));
            return assignmentRepository.findByStaffProfileIdAndStatus(sp.getId(), status)
                    .stream()
                    .map(this::mapToAssignmentResponse)
                    .collect(Collectors.toList());
        } else {
            // Client sees assigned-to-them
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            Client client = clientRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Client profile not found"));
            List<StaffingRequest> requests = staffingRequestRepository.findByClientId(client.getId());
            List<Assignment> all = new ArrayList<>();
            for (StaffingRequest sr : requests) {
                all.addAll(assignmentRepository.findByStaffingRequestIdAndStatus(sr.getId(), status));
            }
            return all.stream()
                    .map(this::mapToAssignmentResponse)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public PageResponse<AssignmentResponse> getAssignmentsByStatus(AssignmentStatus status, String email, String userRole, Pageable pageable) {
        List<AssignmentResponse> all;
        if (userRole.equals(Role.ROLE_ADMIN.name()) || userRole.equals(Role.ROLE_SUPER_ADMIN.name())) {
            all = assignmentRepository.findByStatus(status).stream().map(this::mapToAssignmentResponse).collect(Collectors.toList());
        } else if (userRole.equals(Role.ROLE_STAFF.name())) {
            User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
            StaffProfile sp = staffProfileRepository.findByUserId(user.getId()).orElseThrow(() -> new ResourceNotFoundException("Staff profile not found"));
            all = assignmentRepository.findByStaffProfileIdAndStatus(sp.getId(), status).stream().map(this::mapToAssignmentResponse).collect(Collectors.toList());
        } else {
            User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
            Client client = clientRepository.findByUserId(user.getId()).orElseThrow(() -> new ResourceNotFoundException("Client profile not found"));
            List<StaffingRequest> requests = staffingRequestRepository.findByClientId(client.getId());
            List<Assignment> collected = new ArrayList<>();
            for (StaffingRequest sr : requests) { collected.addAll(assignmentRepository.findByStaffingRequestIdAndStatus(sr.getId(), status)); }
            all = collected.stream().map(this::mapToAssignmentResponse).collect(Collectors.toList());
        }
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), all.size());
        List<AssignmentResponse> pageContent = start < all.size() ? all.subList(start, end) : new ArrayList<>();
        int totalPages = Math.max(1, (int) Math.ceil((double) all.size() / pageable.getPageSize()));
        return new PageResponse<>(pageContent, pageable.getPageNumber(), pageable.getPageSize(), all.size(), totalPages, start == 0, end >= all.size());
    }

    @Override
    public AssignmentResponse getAssignmentById(Long assignmentId, String email, String userRole) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));

        // FIX 2: Ownership check - staff can only see own, client can only see assigned-to-them
        if (userRole.equals(Role.ROLE_STAFF.name())) {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            StaffProfile sp = staffProfileRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Staff profile not found"));
            if (!assignment.getStaffProfile().getId().equals(sp.getId())) {
                throw new IllegalArgumentException("You do not have access to this assignment");
            }
        } else if (userRole.equals(Role.ROLE_USER.name())) {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            Client client = clientRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Client profile not found"));
            if (!assignment.getStaffingRequest().getClient().getId().equals(client.getId())) {
                throw new IllegalArgumentException("You do not have access to this assignment");
            }
        }
        // Admin/SuperAdmin can see all

        return mapToAssignmentResponse(assignment);
    }

    @Override
    public StaffDetailsResponse getStaffDetailsForAssignment(Long assignmentId, String email, String userRole) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));

        // FIX 2: Only admin or the client who owns the staffing request can view staff details
        if (!userRole.equals(Role.ROLE_ADMIN.name()) && !userRole.equals(Role.ROLE_SUPER_ADMIN.name())) {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            Client client = clientRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Client profile not found"));
            if (!assignment.getStaffingRequest().getClient().getId().equals(client.getId())) {
                throw new IllegalArgumentException("You do not have access to this assignment");
            }
        }

        StaffProfile staffProfile = assignment.getStaffProfile();
        User staffUser = staffProfile.getUser();

        StaffDetailsResponse response = new StaffDetailsResponse();

        // Assignment info
        response.setAssignmentId(assignment.getId());
        response.setAssignmentStatus(assignment.getStatus().name());
        response.setNotes(assignment.getNotes());
        response.setSentToClient(assignment.isSentToClient());
        response.setSentToClientAt(assignment.getSentToClientAt());
        response.setAcceptedAt(assignment.getAcceptedAt());
        response.setCreatedAt(assignment.getCreatedAt());

        // Staff profile info
        response.setStaffProfileId(staffProfile.getId());
        response.setStaffName(staffUser.getFirstName() + " " + staffUser.getLastName());
        response.setStaffEmail(staffUser.getEmail());
        response.setStaffPhone(staffUser.getPhone());
        response.setStaffCategory(staffProfile.getStaffCategory());
        response.setVerified(staffProfile.isVerified());

        // Staffing request info
        StaffingRequest sr = assignment.getStaffingRequest();
        response.setStaffingRequestId(sr.getId());
        response.setDesignation(sr.getDesignation());
        response.setLocation(sr.getLocation());
        response.setShift(sr.getShift());

        // Staff documents
        List<StaffDocument> docs = staffDocumentRepository.findByStaffProfileId(staffProfile.getId());
        List<StaffDetailsResponse.StaffDocumentInfo> docInfos = new ArrayList<>();
        for (StaffDocument doc : docs) {
            StaffDetailsResponse.StaffDocumentInfo docInfo = new StaffDetailsResponse.StaffDocumentInfo();
            docInfo.setId(doc.getId());
            docInfo.setDocumentType(doc.getDocumentType().name());
            docInfo.setFilePath(doc.getFilePath());
            docInfo.setFileName(doc.getFileName());
            docInfos.add(docInfo);
        }
        response.setDocuments(docInfos);

        return response;
    }

    @Override
    @Transactional
    public AssignmentResponse approveAssignmentToClient(Long assignmentId, String adminEmail, String userRole) {
        // FIX 1: Verify caller is admin
        if (!userRole.equals(Role.ROLE_ADMIN.name()) && !userRole.equals(Role.ROLE_SUPER_ADMIN.name())) {
            throw new IllegalArgumentException("Only admin can approve assignments to client");
        }

        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with email: " + adminEmail));

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));

        // Only ACCEPTED or ACTIVE assignments can be sent to client
        if (assignment.getStatus() != AssignmentStatus.ACCEPTED && assignment.getStatus() != AssignmentStatus.ACTIVE) {
            throw new IllegalArgumentException("Assignment must be ACCEPTED or ACTIVE before sending to client");
        }

        if (assignment.isSentToClient()) {
            throw new IllegalArgumentException("Assignment details already sent to client");
        }

        assignment.setSentToClient(true);
        assignment.setSentToClientAt(LocalDateTime.now());

        // Update staffing request status to ASSIGNED
        StaffingRequest sr = assignment.getStaffingRequest();
        if (sr.getStatus() == StaffingRequestStatus.APPROVED) {
            sr.setStatus(StaffingRequestStatus.ASSIGNED);
            staffingRequestRepository.save(sr);
        }

        assignment = assignmentRepository.save(assignment);

        // Notify client that staff details have been approved and sent
        try {
            com.nurseadda.project.entity.Client client = assignment.getStaffingRequest().getClient();
            com.nurseadda.project.entity.User clientUser = client.getUser();
            emailService.sendStaffApprovedToClientEmail(
                    clientUser.getEmail(),
                    clientUser.getFirstName() + " " + clientUser.getLastName(),
                    assignment.getStaffProfile().getUser().getFirstName() + " " + assignment.getStaffProfile().getUser().getLastName(),
                    assignment.getStaffingRequest().getDesignation(),
                    assignment.getStaffingRequest().getLocation()
            );
        } catch (Exception e) {
            log.error("Failed to send staff approved notification to client: {}", e.getMessage());
        }

        return mapToAssignmentResponse(assignment);
    }

    @Override
    public List<AssignmentResponse> getClientAssignments(String clientEmail) {
        User user = userRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + clientEmail));

        Client client = clientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Client profile not found"));

        // Get all staffing requests for this client
        List<StaffingRequest> requests = staffingRequestRepository.findByClientId(client.getId());
        List<Assignment> assignments = new ArrayList<>();

        for (StaffingRequest sr : requests) {
            List<Assignment> srAssignments = assignmentRepository.findByStaffingRequestId(sr.getId());
            assignments.addAll(srAssignments);
        }

        return assignments.stream()
                .map(this::mapToAssignmentResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PageResponse<AssignmentResponse> getClientAssignments(String clientEmail, Pageable pageable) {
        User user = userRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + clientEmail));
        Client client = clientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Client profile not found"));
        List<StaffingRequest> requests = staffingRequestRepository.findByClientId(client.getId());
        List<Long> requestIds = requests.stream().map(StaffingRequest::getId).collect(Collectors.toList());
        // Collect all assignments for these requests
        List<Assignment> allAssignments = new ArrayList<>();
        for (Long rid : requestIds) {
            allAssignments.addAll(assignmentRepository.findByStaffingRequestId(rid));
        }
        // Manual pagination
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), allAssignments.size());
        List<Assignment> pageContent = start < allAssignments.size() ? allAssignments.subList(start, end) : new ArrayList<>();
        List<AssignmentResponse> mapped = pageContent.stream()
                .map(this::mapToAssignmentResponse)
                .collect(Collectors.toList());
        return new PageResponse<>(mapped, pageable.getPageNumber(), pageable.getPageSize(),
                allAssignments.size(), (int) Math.ceil((double) allAssignments.size() / pageable.getPageSize()),
                start == 0, end >= allAssignments.size());
    }

    @Override
    public PageResponse<AssignmentResponse> getClientAssignments(String clientEmail, RequestType requestType, Pageable pageable) {
        if (requestType == null) return getClientAssignments(clientEmail, pageable);
        User user = userRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + clientEmail));
        Client client = clientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Client profile not found"));
        List<StaffingRequest> requests = staffingRequestRepository.findByClientId(client.getId())
                .stream()
                .filter(sr -> sr.getRequestType() == requestType)
                .collect(Collectors.toList());
        List<Assignment> allAssignments = new ArrayList<>();
        for (StaffingRequest sr : requests) {
            allAssignments.addAll(assignmentRepository.findByStaffingRequestId(sr.getId()));
        }
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), allAssignments.size());
        List<Assignment> pageContent = start < allAssignments.size() ? allAssignments.subList(start, end) : new ArrayList<>();
        List<AssignmentResponse> mapped = pageContent.stream().map(this::mapToAssignmentResponse).collect(Collectors.toList());
        int totalPages = Math.max(1, (int) Math.ceil((double) allAssignments.size() / pageable.getPageSize()));
        return new PageResponse<>(mapped, pageable.getPageNumber(), pageable.getPageSize(), allAssignments.size(), totalPages, start == 0, end >= allAssignments.size());
    }

    // ─────────────────────────────────────────────
    // Mapping helpers
    // ─────────────────────────────────────────────

    private StaffingRequestResponse mapToStaffingRequestResponse(StaffingRequest request) {
        StaffingRequestResponse response = new StaffingRequestResponse();
        response.setId(request.getId());
        response.setClientName(request.getClient().getUser().getFirstName() + " " + request.getClient().getUser().getLastName());
        response.setDesignation(request.getDesignation());
        response.setLocation(request.getLocation());
        response.setRequestType(request.getRequestType() != null ? request.getRequestType().name() : null);
        response.setShift(request.getShift());
        response.setStartDate(request.getStartDate());
        response.setEndDate(request.getEndDate());
        response.setNumberOfStaff(request.getNumberOfStaff());
        response.setRequiredSkills(request.getRequiredSkills());
        response.setStatus(request.getStatus().name());
        response.setDeadline(request.getDeadline());
        response.setEstimatedTotal(request.getEstimatedTotal());
        response.setAdvanceAmount(request.getAdvanceAmount());
        response.setAdvancePaid(request.isAdvancePaid());
        response.setCreatedAt(request.getCreatedAt());
        response.calculateRemainingTime();
        return response;
    }

    private AssignmentResponse mapToAssignmentResponse(Assignment assignment) {
        AssignmentResponse response = new AssignmentResponse();
        response.setId(assignment.getId());
        response.setStaffProfileId(assignment.getStaffProfile().getId());
        response.setStaffName(assignment.getStaffProfile().getUser().getFirstName() + " " + assignment.getStaffProfile().getUser().getLastName());
        response.setStaffEmail(assignment.getStaffProfile().getUser().getEmail());
        response.setStaffCategory(assignment.getStaffProfile().getStaffCategory());
        response.setStaffingRequestId(assignment.getStaffingRequest().getId());
        response.setDesignation(assignment.getStaffingRequest().getDesignation());
        response.setLocation(assignment.getStaffingRequest().getLocation());
        response.setShift(assignment.getStaffingRequest().getShift());
        response.setAssignedByName(assignment.getAssignedBy().getFirstName() + " " + assignment.getAssignedBy().getLastName());
        response.setStatus(assignment.getStatus());
        response.setNotes(assignment.getNotes());
        response.setSentToClient(assignment.isSentToClient());
        response.setSentToClientAt(assignment.getSentToClientAt());
        response.setAcceptedAt(assignment.getAcceptedAt());
        response.setCompletedAt(assignment.getCompletedAt());
        response.setCreatedAt(assignment.getCreatedAt());
        return response;
    }
}

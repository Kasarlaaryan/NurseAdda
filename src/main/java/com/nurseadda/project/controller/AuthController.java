package com.nurseadda.project.controller;

import com.nurseadda.project.dto.request.AdminRegisterRequest;
import com.nurseadda.project.dto.request.ChangePasswordRequest;
import com.nurseadda.project.dto.request.ClientProfileRequest;
import com.nurseadda.project.dto.request.ClientRegisterRequest;
import com.nurseadda.project.dto.request.ForgotPasswordRequest;
import com.nurseadda.project.dto.request.LoginRequest;
import com.nurseadda.project.dto.request.LogoutRequest;
import com.nurseadda.project.dto.request.RefreshTokenRequest;
import com.nurseadda.project.dto.request.ResetPasswordRequest;
import com.nurseadda.project.dto.request.SendOtpRequest;
import com.nurseadda.project.dto.request.StaffProfileRequest;
import com.nurseadda.project.dto.request.StaffRegisterRequest;
import com.nurseadda.project.dto.request.StaffVerificationRequest;
import com.nurseadda.project.dto.request.VerifyOtpRequest;
import com.nurseadda.project.dto.response.AuthResponseDto;
import com.nurseadda.project.dto.response.StaffProfileResponseDto;
import com.nurseadda.project.dto.response.UserResponseDto;
import com.nurseadda.project.security.JwtUtil;
import com.nurseadda.project.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register-staff")
    public ResponseEntity<String> registerStaff(@Valid @RequestBody StaffRegisterRequest staffRegisterRequest) {
        String message = authService.registerStaff(staffRegisterRequest);
        return ResponseEntity.status(HttpStatus.OK).body(message);
    }

    @PostMapping("/register-client")
    public ResponseEntity<String> registerClient(@Valid @RequestBody ClientRegisterRequest clientRegisterRequest) {
        String message = authService.registerClient(clientRegisterRequest);
        return ResponseEntity.status(HttpStatus.OK).body(message);
    }

    @PostMapping("/register-admin")
    public ResponseEntity<String> registerAdmin(@Valid @RequestBody AdminRegisterRequest adminRegisterRequest) {
        String message = authService.registerAdmin(adminRegisterRequest);
        return ResponseEntity.status(HttpStatus.OK).body(message);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponseDto response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/send-otp")
    public ResponseEntity<String> sendOtp(@Valid @RequestBody SendOtpRequest sendOtpRequest) {
        authService.sendOtp(sendOtpRequest);
        return ResponseEntity.ok("OTP sent to your email");
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@Valid @RequestBody VerifyOtpRequest verifyOtpRequest) {
        String message = authService.verifyOtp(verifyOtpRequest);
        return ResponseEntity.ok(message);
    }

    @PutMapping("/client-profile")
    public ResponseEntity<UserResponseDto> updateClientProfile(
            Authentication authentication,
            @Valid @RequestBody ClientProfileRequest clientProfileRequest
    ) {
        String email = (String) authentication.getPrincipal();
        UserResponseDto response = authService.updateClientProfile(email, clientProfileRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDto> refresh(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        AuthResponseDto response = authService.refresh(refreshTokenRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUser(Authentication authentication) {
        String email = (String) authentication.getPrincipal();
        UserResponseDto response = authService.getCurrentUser(email);
        return ResponseEntity.ok(response);
    }

    // =====================================================================
    //  Client profile CRUD
    // =====================================================================

    @GetMapping("/client-profile")
    public ResponseEntity<UserResponseDto> getClientProfile(Authentication authentication) {
        String email = (String) authentication.getPrincipal();
        return ResponseEntity.ok(authService.getClientProfile(email));
    }

    @DeleteMapping("/client-profile")
    public ResponseEntity<String> deleteClientProfile(Authentication authentication) {
        String email = (String) authentication.getPrincipal();
        authService.deleteClientProfile(email);
        return ResponseEntity.ok("Client profile deleted successfully");
    }

    // =====================================================================
    //  Admin / Super Admin profile
    // =====================================================================

    @GetMapping("/admin-profile")
    public ResponseEntity<UserResponseDto> getAdminProfile(Authentication authentication) {
        String email = (String) authentication.getPrincipal();
        return ResponseEntity.ok(authService.getAdminProfile(email));
    }

    @PutMapping("/admin-profile")
    public ResponseEntity<UserResponseDto> updateAdminProfile(
            Authentication authentication,
            @Valid @RequestBody ClientProfileRequest adminProfileRequest
    ) {
        String email = (String) authentication.getPrincipal();
        return ResponseEntity.ok(authService.updateAdminProfile(email, adminProfileRequest));
    }

    // =====================================================================
    //  Admin management endpoints
    // =====================================================================

    @GetMapping("/admin/users")
    public ResponseEntity<Page<UserResponseDto>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        if (page < 0) page = 0;
        if (size < 1) size = 10;
        if (size > 100) size = 100;
        return ResponseEntity.ok(authService.getAllUsers(PageRequest.of(page, size)));
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable Long userId) {
        authService.deleteUser(userId);
        return ResponseEntity.ok("User deleted successfully");
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            HttpServletRequest request,
            @RequestBody(required = false) LogoutRequest logoutRequest
    ) {
        String accessToken = jwtUtil.retrieveTokenFromRequest(request);
        authService.logout(accessToken, logoutRequest);
        return ResponseEntity.ok("Logged out successfully");
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<String> resendOtp(@Valid @RequestBody SendOtpRequest sendOtpRequest) {
        authService.sendOtp(sendOtpRequest);
        return ResponseEntity.ok("OTP sent to your email");
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest changePasswordRequest
    ) {
        String email = (String) authentication.getPrincipal();
        authService.changePassword(email, changePasswordRequest);
        return ResponseEntity.ok("Password changed successfully");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        authService.forgotPassword(forgotPasswordRequest);
        return ResponseEntity.ok("OTP sent to your email");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
        authService.resetPassword(resetPasswordRequest);
        return ResponseEntity.ok("Password reset successfully");
    }

    @PatchMapping("/users/{userId}/unlock")
    public ResponseEntity<String> unlockAccount(@PathVariable Long userId) {
        return ResponseEntity.ok(authService.unlockAccount(userId));
    }

    @PutMapping(value = "/staff-profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StaffProfileResponseDto> updateStaffProfile(
            Authentication authentication,
            @Valid @RequestPart("profile") StaffProfileRequest staffProfileRequest,
            @RequestPart(value = "stateBoardCertificate", required = false) MultipartFile stateBoardCertificate,
            @RequestPart(value = "educationalDocuments", required = false) List<MultipartFile> educationalDocuments,
            @RequestPart(value = "photos", required = false) List<MultipartFile> photos
    ) {
        String email = (String) authentication.getPrincipal();
        StaffProfileResponseDto response = authService.updateStaffProfile(
                email, staffProfileRequest, stateBoardCertificate, educationalDocuments, photos);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/staff/{userId}/verification")
    public ResponseEntity<StaffProfileResponseDto> verifyStaffProfile(
            @PathVariable Long userId,
            @Valid @RequestBody StaffVerificationRequest verificationRequest
    ) {
        StaffProfileResponseDto response = authService.verifyStaffProfile(
                userId, verificationRequest.getVerified());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/staff-profile")
    public ResponseEntity<StaffProfileResponseDto> getStaffProfile(Authentication authentication) {
        String email = (String) authentication.getPrincipal();
        return ResponseEntity.ok(authService.getStaffProfile(email));
    }

    @GetMapping("/staff/{id}")
    public ResponseEntity<StaffProfileResponseDto> getStaffProfileById(@PathVariable Long id) {
        return ResponseEntity.ok(authService.getStaffProfileById(id));
    }

    @GetMapping("/staff")
    public ResponseEntity<Page<StaffProfileResponseDto>> getAllStaffProfiles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        if (page < 0) {
            page = 0;
        }
        if (size < 1) {
            size = 10;
        }
        if (size > 100) {
            size = 100;
        }
        return ResponseEntity.ok(authService.getAllStaffProfiles(PageRequest.of(page, size)));
    }

}

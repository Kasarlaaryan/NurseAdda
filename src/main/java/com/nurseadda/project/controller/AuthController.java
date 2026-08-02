package com.nurseadda.project.controller;

import com.nurseadda.project.dto.request.ClientRegisterRequest;
import com.nurseadda.project.dto.request.LoginRequest;
import com.nurseadda.project.dto.request.SendOtpRequest;
import com.nurseadda.project.dto.request.StaffProfileRequest;
import com.nurseadda.project.dto.request.StaffRegisterRequest;
import com.nurseadda.project.dto.request.UpdateProfileRequest;
import com.nurseadda.project.dto.request.VerifyOtpRequest;
import com.nurseadda.project.dto.response.AuthResponseDto;
import com.nurseadda.project.dto.response.StaffProfileResponseDto;
import com.nurseadda.project.dto.response.UserResponseDto;
import com.nurseadda.project.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<AuthResponseDto> verifyOtp(@Valid @RequestBody VerifyOtpRequest verifyOtpRequest) {
        AuthResponseDto response = authService.verifyOtp(verifyOtpRequest);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    public ResponseEntity<UserResponseDto> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest updateProfileRequest
    ) {
        String email = (String) authentication.getPrincipal();
        UserResponseDto response = authService.updateProfile(email, updateProfileRequest);
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/staff-profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StaffProfileResponseDto> updateStaffProfile(
            Authentication authentication,
            @Valid @RequestPart("profile") StaffProfileRequest staffProfileRequest,
            @RequestPart(value = "passportPhoto", required = false) MultipartFile passportPhoto,
            @RequestPart(value = "educationalDocuments", required = false) List<MultipartFile> educationalDocuments
    ) {
        String email = (String) authentication.getPrincipal();
        StaffProfileResponseDto response = authService.updateStaffProfile(
                email, staffProfileRequest, passportPhoto, educationalDocuments);
        return ResponseEntity.ok(response);
    }
}

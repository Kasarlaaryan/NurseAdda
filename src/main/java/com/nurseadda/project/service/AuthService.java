package com.nurseadda.project.service;

import com.nurseadda.project.common.exception.IllegalCredentialsException;
import com.nurseadda.project.common.exception.InvalidOtpException;
import com.nurseadda.project.common.exception.OtpExpiredException;
import com.nurseadda.project.common.exception.ResourceNotFoundException;
import com.nurseadda.project.common.exception.UserAlreadyExistException;
import com.nurseadda.project.common.exception.UserNotFoundException;
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
import com.nurseadda.project.dto.request.VerifyOtpRequest;
import com.nurseadda.project.dto.response.AuthResponseDto;
import com.nurseadda.project.dto.response.StaffProfileResponseDto;
import com.nurseadda.project.dto.response.UserResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AuthService {

    String registerStaff(StaffRegisterRequest staffRegisterRequest) throws UserAlreadyExistException;

    String registerClient(ClientRegisterRequest clientRegisterRequest) throws UserAlreadyExistException;

    String registerAdmin(AdminRegisterRequest adminRegisterRequest) throws UserAlreadyExistException;

    AuthResponseDto login(LoginRequest loginRequest) throws IllegalCredentialsException, UserNotFoundException;

    void sendOtp(SendOtpRequest sendOtpRequest) throws UserNotFoundException;

    String verifyOtp(VerifyOtpRequest verifyOtpRequest) throws UserNotFoundException, InvalidOtpException, OtpExpiredException;

    UserResponseDto updateClientProfile(String email, ClientProfileRequest clientProfileRequest) throws UserNotFoundException;

    StaffProfileResponseDto updateStaffProfile(
            String email,
            StaffProfileRequest staffProfileRequest,
            MultipartFile stateBoardCertificate,
            List<MultipartFile> educationalDocuments,
            List<MultipartFile> photos
    ) throws UserNotFoundException, ResourceNotFoundException;

    AuthResponseDto refresh(RefreshTokenRequest refreshTokenRequest);

    UserResponseDto getCurrentUser(String email) throws UserNotFoundException;

    void logout(String accessToken, LogoutRequest logoutRequest);

    void changePassword(String email, ChangePasswordRequest changePasswordRequest)
            throws UserNotFoundException, IllegalCredentialsException;

    void forgotPassword(ForgotPasswordRequest forgotPasswordRequest) throws UserNotFoundException;

    void resetPassword(ResetPasswordRequest resetPasswordRequest)
            throws UserNotFoundException, InvalidOtpException, OtpExpiredException;

    String unlockAccount(Long userId) throws UserNotFoundException;

    StaffProfileResponseDto verifyStaffProfile(Long userId, boolean verified) throws ResourceNotFoundException;

    StaffProfileResponseDto getStaffProfile(String email) throws UserNotFoundException, ResourceNotFoundException;

    Page<StaffProfileResponseDto> getAllStaffProfiles(Pageable pageable);

    StaffProfileResponseDto getStaffProfileById(Long id) throws ResourceNotFoundException;

    // =====================================================================
    //  Client profile CRUD
    // =====================================================================

    UserResponseDto getClientProfile(String email) throws UserNotFoundException;

    void deleteClientProfile(String email) throws UserNotFoundException;

    // =====================================================================
    //  Admin profile
    // =====================================================================

    UserResponseDto getAdminProfile(String email) throws UserNotFoundException;

    UserResponseDto updateAdminProfile(String email, ClientProfileRequest request) throws UserNotFoundException;

    // =====================================================================
    //  Admin management
    // =====================================================================

    Page<UserResponseDto> getAllUsers(Pageable pageable);

    void deleteUser(Long userId) throws UserNotFoundException;
}

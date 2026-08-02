package com.nurseadda.project.service;

import com.nurseadda.project.common.exception.IllegalCredentialsException;
import com.nurseadda.project.common.exception.InvalidOtpException;
import com.nurseadda.project.common.exception.OtpExpiredException;
import com.nurseadda.project.common.exception.ResourceNotFoundException;
import com.nurseadda.project.common.exception.UserAlreadyExistException;
import com.nurseadda.project.common.exception.UserNotFoundException;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AuthService {

    String registerStaff(StaffRegisterRequest staffRegisterRequest) throws UserAlreadyExistException;

    String registerClient(ClientRegisterRequest clientRegisterRequest) throws UserAlreadyExistException;

    AuthResponseDto login(LoginRequest loginRequest) throws IllegalCredentialsException, UserNotFoundException;

    void sendOtp(SendOtpRequest sendOtpRequest) throws UserNotFoundException;

    AuthResponseDto verifyOtp(VerifyOtpRequest verifyOtpRequest) throws UserNotFoundException, InvalidOtpException, OtpExpiredException;

    UserResponseDto updateProfile(String email, UpdateProfileRequest updateProfileRequest) throws UserNotFoundException;

    StaffProfileResponseDto updateStaffProfile(
            String email,
            StaffProfileRequest staffProfileRequest,
            MultipartFile passportPhoto,
            List<MultipartFile> educationalDocuments
    ) throws UserNotFoundException, ResourceNotFoundException;
}

package com.nurseadda.project.service;

import com.nurseadda.project.common.exception.ResourceNotFoundException;
import com.nurseadda.project.common.exception.UserAlreadyExistException;
import com.nurseadda.project.common.exception.UserNotFoundException;
import com.nurseadda.project.dto.request.AdminCreateUserRequest;
import com.nurseadda.project.dto.request.AdminUpdateStaffProfileRequest;
import com.nurseadda.project.dto.request.AdminUpdateUserRequest;
import com.nurseadda.project.dto.response.StaffDocumentResponseDto;
import com.nurseadda.project.dto.response.StaffProfileResponseDto;
import com.nurseadda.project.dto.response.UserResponseDto;
import com.nurseadda.project.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface AdminService {

    Page<UserResponseDto> getAllUsers(Pageable pageable, Role role);

    UserResponseDto getUserById(Long id) throws UserNotFoundException;

    UserResponseDto createUser(AdminCreateUserRequest request) throws UserAlreadyExistException;

    UserResponseDto updateUser(Long id, AdminUpdateUserRequest request, String actingEmail)
            throws UserNotFoundException, UserAlreadyExistException;

    void deleteUser(Long id, String actingEmail) throws UserNotFoundException;

    UserResponseDto setUserEnabled(Long id, boolean enabled, String actingEmail) throws UserNotFoundException;

    Page<StaffProfileResponseDto> getAllStaffProfiles(Pageable pageable);

    StaffProfileResponseDto getStaffProfileByUserId(Long userId)
            throws UserNotFoundException, ResourceNotFoundException;

    StaffProfileResponseDto updateStaffProfileByAdmin(Long userId, AdminUpdateStaffProfileRequest request)
            throws ResourceNotFoundException;

    StaffProfileResponseDto verifyStaffProfile(Long userId, boolean verified) throws ResourceNotFoundException;

    List<StaffDocumentResponseDto> getStaffDocuments(Long userId) throws ResourceNotFoundException;

    StaffDocumentResponseDto verifyStaffDocument(Long userId, Long documentId, boolean verified)
            throws ResourceNotFoundException;

    StaffDocumentResponseDto requestDocumentReplacement(
            Long userId, Long documentId, boolean requested, String reason, LocalDate expiryDate
    ) throws ResourceNotFoundException;
}

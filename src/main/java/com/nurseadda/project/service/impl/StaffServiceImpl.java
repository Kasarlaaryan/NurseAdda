package com.nurseadda.project.service.impl;

import com.nurseadda.project.common.exception.ResourceNotFoundException;
import com.nurseadda.project.dto.request.StaffProfileRequest;
import com.nurseadda.project.dto.response.StaffProfileResponse;
import com.nurseadda.project.entity.StaffProfile;
import com.nurseadda.project.entity.User;
import com.nurseadda.project.repository.StaffProfileRepository;
import com.nurseadda.project.repository.UserRepository;
import com.nurseadda.project.service.StaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StaffServiceImpl implements StaffService {

    private final UserRepository userRepository;
    private final StaffProfileRepository staffProfileRepository;

    @Override
    @Transactional
    public StaffProfileResponse updateProfile(String email, StaffProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        StaffProfile profile = staffProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("StaffProfile", "user", email));

        profile.setAadharNumber(request.getAadharNumber());
        profile.setNursingCouncilRegNumber(request.getNursingCouncilRegNumber());
        profile.setYearsOfExperience(request.getYearsOfExperience());
        profile.setSpecializations(request.getSpecializations());
        profile.setCurrentAddress(request.getCurrentAddress());
        profile.setAvailabilityStatus(request.getAvailabilityStatus());
        profile.setBankAccountNumber(request.getBankAccountNumber());
        profile.setBankName(request.getBankName());
        profile.setIfscCode(request.getIfscCode());

        staffProfileRepository.save(profile);

        return toResponse(profile, user);
    }

    @Override
    @Transactional(readOnly = true)
    public StaffProfileResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        StaffProfile profile = staffProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("StaffProfile", "user", email));

        return toResponse(profile, user);
    }

    @Override
    @Transactional
    public StaffProfileResponse verifyProfile(Long userId, boolean verified) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        StaffProfile profile = staffProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("StaffProfile", userId));

        profile.setVerified(verified);
        staffProfileRepository.save(profile);

        return toResponse(profile, user);
    }

    private StaffProfileResponse toResponse(StaffProfile profile, User user) {
        return StaffProfileResponse.builder()
                .id(profile.getId())
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .staffCategory(profile.getStaffCategory())
                .aadharNumber(profile.getAadharNumber())
                .nursingCouncilRegNumber(profile.getNursingCouncilRegNumber())
                .yearsOfExperience(profile.getYearsOfExperience())
                .specializations(profile.getSpecializations())
                .currentAddress(profile.getCurrentAddress())
                .availabilityStatus(profile.getAvailabilityStatus())
                .bankAccountNumber(profile.getBankAccountNumber())
                .bankName(profile.getBankName())
                .ifscCode(profile.getIfscCode())
                .verified(profile.isVerified())
                .build();
    }
}

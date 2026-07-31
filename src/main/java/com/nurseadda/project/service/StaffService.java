package com.nurseadda.project.service;

import com.nurseadda.project.dto.request.StaffProfileRequest;
import com.nurseadda.project.dto.response.StaffProfileResponse;

public interface StaffService {

    StaffProfileResponse updateProfile(String email, StaffProfileRequest request);

    StaffProfileResponse getProfile(String email);

    /**
     * Sets the admin verification status of a staff member's profile.
     * Only verified staff are eligible for assignments.
     */
    StaffProfileResponse verifyProfile(Long userId, boolean verified);
}

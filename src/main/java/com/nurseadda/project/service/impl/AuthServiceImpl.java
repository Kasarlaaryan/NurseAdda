package com.nurseadda.project.service.impl;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.nurseadda.project.common.exception.IllegalCredentialsException;
import com.nurseadda.project.common.exception.InvalidOtpException;
import com.nurseadda.project.common.exception.OtpExpiredException;
import com.nurseadda.project.common.exception.ResourceNotFoundException;
import com.nurseadda.project.common.exception.UserAlreadyExistException;
import com.nurseadda.project.common.exception.UserNotFoundException;
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
import com.nurseadda.project.entity.Client;
import com.nurseadda.project.entity.StaffDocument;
import com.nurseadda.project.entity.StaffProfile;
import com.nurseadda.project.entity.User;
import com.nurseadda.project.enums.Role;
import com.nurseadda.project.enums.StaffDocumentType;
import com.nurseadda.project.model.PasswordReset;
import com.nurseadda.project.model.PendingRegistration;
import com.nurseadda.project.repository.ClientRepository;
import com.nurseadda.project.repository.PasswordResetRepository;
import com.nurseadda.project.repository.PendingRegistrationRepository;
import com.nurseadda.project.repository.StaffDocumentRepository;
import com.nurseadda.project.repository.StaffProfileRepository;
import com.nurseadda.project.repository.UserRepository;
import com.nurseadda.project.security.JwtUtil;
import com.nurseadda.project.security.TokenBlacklistService;
import com.nurseadda.project.service.AuthService;
import com.nurseadda.project.service.EmailService;
import com.nurseadda.project.service.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final StaffProfileRepository staffProfileRepository;
    private final ClientRepository clientRepository;
    private final EmailService emailService;
    private final PendingRegistrationRepository pendingRegistrationRepository;
    private final StaffDocumentRepository staffDocumentRepository;
    private final TokenBlacklistService tokenBlacklistService;
    private final PasswordResetRepository passwordResetRepository;

    @Value("${app.otp.length}")
    private int otpLength;

    @Value("${app.otp.expiry-minutes}")
    private int otpExpiryMinutes;

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Override
    public String registerStaff(StaffRegisterRequest staffRegisterRequest) throws UserAlreadyExistException {
        if (userService.existsByEmail(staffRegisterRequest.getEmail())) {
            throw new UserAlreadyExistException(
                    "User already exists with email : "
                            + staffRegisterRequest.getEmail()
            );
        }

        String code = generateOtp();

        String[] nameParts = splitName(staffRegisterRequest.getFullName());

        PendingRegistration pending = PendingRegistration.builder()
                .email(staffRegisterRequest.getEmail())
                .password(passwordEncoder.encode(staffRegisterRequest.getPassword()))
                .firstName(nameParts[0])
                .lastName(nameParts[1])
                .phone(staffRegisterRequest.getPhone())
                .role(Role.ROLE_STAFF.name())
                .staffCategory(staffRegisterRequest.getStaffCategory())
                .code(code)
                .expiresAt(LocalDateTime.now().plusMinutes(otpExpiryMinutes))
                .verified(false)
                .build();

        pendingRegistrationRepository.save(pending, otpExpiryMinutes);
        emailService.sendOtp(staffRegisterRequest.getEmail(), code);

        return "OTP sent to your email";
    }

    @Override
    public String registerClient(ClientRegisterRequest clientRegisterRequest) throws UserAlreadyExistException {
        if (userService.existsByEmail(clientRegisterRequest.getEmail())) {
            throw new UserAlreadyExistException(
                    "User already exists with email : "
                            + clientRegisterRequest.getEmail()
            );
        }

        String code = generateOtp();

        PendingRegistration pending = PendingRegistration.builder()
                .email(clientRegisterRequest.getEmail())
                .password(passwordEncoder.encode(clientRegisterRequest.getPassword()))
                .firstName(clientRegisterRequest.getFirstName())
                .lastName(clientRegisterRequest.getLastName())
                .phone(clientRegisterRequest.getMobileNumber())
                .role(Role.ROLE_USER.name())
                .code(code)
                .expiresAt(LocalDateTime.now().plusMinutes(otpExpiryMinutes))
                .verified(false)
                .build();

        pendingRegistrationRepository.save(pending, otpExpiryMinutes);
        emailService.sendOtp(clientRegisterRequest.getEmail(), code);

        return "OTP sent to your email";
    }

    @Override
    @Transactional
    public AuthResponseDto login(LoginRequest loginRequest) throws IllegalCredentialsException, UserNotFoundException {
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new IllegalCredentialsException("Incorrect Login Details"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new IllegalCredentialsException("Invalid email or password");
        }

        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        UserResponseDto userResponseDto = modelMapper.map(user, UserResponseDto.class);
        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userResponseDto)
                .build();
    }

    @Override
    @Transactional
    public void sendOtp(SendOtpRequest sendOtpRequest) throws UserNotFoundException {
        PendingRegistration pending = pendingRegistrationRepository.findByEmail(sendOtpRequest.getEmail())
                .orElseThrow(() -> new UserNotFoundException(
                        "No pending registration found with email : " + sendOtpRequest.getEmail()
                ));

        String code = generateOtp();
        pending.setCode(code);
        pending.setExpiresAt(LocalDateTime.now().plusMinutes(otpExpiryMinutes));
        pending.setVerified(false);

        pendingRegistrationRepository.save(pending, otpExpiryMinutes);
        emailService.sendOtp(sendOtpRequest.getEmail(), code);
    }

    @Override
    @Transactional
    public AuthResponseDto verifyOtp(VerifyOtpRequest verifyOtpRequest) throws UserNotFoundException, InvalidOtpException, OtpExpiredException {
        PendingRegistration pending = pendingRegistrationRepository.findByEmail(verifyOtpRequest.getEmail())
                .orElseThrow(() -> new InvalidOtpException("No pending registration found. Please register first"));

        if (pending.isVerified()) {
            throw new InvalidOtpException("OTP already used");
        }

        if (pending.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new OtpExpiredException("OTP has expired");
        }

        if (!pending.getCode().equals(verifyOtpRequest.getOtp())) {
            throw new InvalidOtpException("Invalid OTP");
        }

        if (userService.existsByEmail(pending.getEmail())) {
            throw new UserAlreadyExistException(
                    "User already exists with email : "
                            + pending.getEmail()
            );
        }

        User user = new User();
        user.setEmail(pending.getEmail());
        user.setPassword(pending.getPassword());
        user.setFirstName(pending.getFirstName());
        user.setLastName(pending.getLastName());
        user.setPhone(pending.getPhone());
        user.setRole(Role.valueOf(pending.getRole()));
        user.setEmailVerified(true);

        User savedUser = userRepository.save(user);

        if (Role.ROLE_STAFF.name().equals(pending.getRole())) {
            StaffProfile staffProfile = new StaffProfile();
            staffProfile.setUser(savedUser);
            staffProfile.setStaffCategory(pending.getStaffCategory());
            staffProfileRepository.save(staffProfile);
        } else {
            Client client = new Client();
            client.setUser(savedUser);
            clientRepository.save(client);
        }

        pendingRegistrationRepository.deleteByEmail(pending.getEmail());

        return buildAuthResponse(savedUser);
    }

    @Override
    @Transactional
    public AuthResponseDto refresh(RefreshTokenRequest refreshTokenRequest) {
        String refreshToken = refreshTokenRequest.getRefreshToken();

        String email;
        try {
            if (!JwtUtil.TYPE_REFRESH.equals(jwtUtil.retrieveTokenType(refreshToken))) {
                throw new IllegalCredentialsException("Invalid refresh token");
            }
            email = jwtUtil.retrieveEmailFromToken(refreshToken);
        } catch (JWTVerificationException e) {
            throw new IllegalCredentialsException("Invalid or expired refresh token");
        }

        if (tokenBlacklistService.isBlacklisted(refreshToken)) {
            throw new IllegalCredentialsException("Refresh token has been revoked. Please login again");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalCredentialsException("Invalid refresh token"));

        // Do not issue tokens for disabled accounts
        if (!user.isEnabled()) {
            throw new IllegalCredentialsException("Account is disabled. Please contact an administrator");
        }

        // Rotate the refresh token: revoke the old one, issue a fresh pair
        tokenBlacklistService.blacklist(refreshToken, jwtUtil.retrieveRemainingValiditySeconds(refreshToken));

        return buildAuthResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getCurrentUser(String email) throws UserNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with email : " + email
                ));
        return modelMapper.map(user, UserResponseDto.class);
    }

    @Override
    @Transactional
    public void logout(String accessToken, LogoutRequest logoutRequest) {
        // Always revoke the presented access token
        tokenBlacklistService.blacklist(accessToken, jwtUtil.retrieveRemainingValiditySeconds(accessToken));

        // Optionally revoke the refresh token too, invalidating the whole session
        if (logoutRequest != null
                && logoutRequest.getRefreshToken() != null
                && !logoutRequest.getRefreshToken().isBlank()) {
            tokenBlacklistService.blacklist(
                    logoutRequest.getRefreshToken(),
                    jwtUtil.retrieveRemainingValiditySeconds(logoutRequest.getRefreshToken())
            );
        }
    }

    @Override
    @Transactional
    public void changePassword(String email, ChangePasswordRequest changePasswordRequest)
            throws UserNotFoundException, IllegalCredentialsException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with email : " + email
                ));

        if (!passwordEncoder.matches(changePasswordRequest.getCurrentPassword(), user.getPassword())) {
            throw new IllegalCredentialsException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest forgotPasswordRequest) throws UserNotFoundException {
        String email = forgotPasswordRequest.getEmail();

        // Fail loudly if the account does not exist (avoids emailing strangers)
        userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(
                        "No account found with email : " + email
                ));

        String code = generateOtp();

        PasswordReset reset = PasswordReset.builder()
                .email(email)
                .code(code)
                .expiresAt(LocalDateTime.now().plusMinutes(otpExpiryMinutes))
                .build();

        passwordResetRepository.save(reset, otpExpiryMinutes);
        emailService.sendOtp(email, code);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest resetPasswordRequest)
            throws UserNotFoundException, InvalidOtpException, OtpExpiredException {
        String email = resetPasswordRequest.getEmail();

        PasswordReset reset = passwordResetRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidOtpException(
                        "No password reset request found for email : " + email
                ));

        if (reset.getExpiresAt().isBefore(LocalDateTime.now())) {
            passwordResetRepository.deleteByEmail(email);
            throw new OtpExpiredException("OTP has expired");
        }

        if (!reset.getCode().equals(resetPasswordRequest.getOtp())) {
            throw new InvalidOtpException("Invalid OTP");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with email : " + email
                ));

        user.setPassword(passwordEncoder.encode(resetPasswordRequest.getNewPassword()));
        userRepository.save(user);

        // Reset code is single-use
        passwordResetRepository.deleteByEmail(email);
    }

    @Override
    @Transactional
    public String unlockAccount(Long userId) throws UserNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with id : " + userId
                ));

        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);

        return "Account unlocked successfully";
    }

    @Override
    @Transactional
    public UserResponseDto updateClientProfile(String email, ClientProfileRequest clientProfileRequest) throws UserNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with email : " + email
                ));

        if (clientProfileRequest.getFirstName() != null
                && !clientProfileRequest.getFirstName().isBlank()) {
            user.setFirstName(clientProfileRequest.getFirstName());
        }

        if (clientProfileRequest.getLastName() != null
                && !clientProfileRequest.getLastName().isBlank()) {
            user.setLastName(clientProfileRequest.getLastName());
        }

        if (clientProfileRequest.getMobileNumber() != null
                && !clientProfileRequest.getMobileNumber().isBlank()) {
            user.setPhone(clientProfileRequest.getMobileNumber());
        }

        User updatedUser = userRepository.save(user);
        return modelMapper.map(updatedUser, UserResponseDto.class);
    }

    @Override
    @Transactional
    public StaffProfileResponseDto updateStaffProfile(
            String email,
            StaffProfileRequest staffProfileRequest,
            MultipartFile stateBoardCertificate,
            List<MultipartFile> educationalDocuments,
            List<MultipartFile> photos
    ) throws UserNotFoundException, ResourceNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with email : " + email
                ));

        StaffProfile staffProfile = staffProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Staff profile not found for user : " + email
                ));

        // Once verified, documents can only be changed with administrator
        // approval, never directly through this profile-update endpoint.
        if (staffProfile.isVerified() && hasDocumentFiles(stateBoardCertificate, educationalDocuments, photos)) {
            throw new IllegalArgumentException(
                    "Your profile is verified. Documents can only be changed when "
                            + "the administrator requests a replacement."
            );
        }

        if (staffProfileRequest.getAadharCardNumber() != null
                && !staffProfileRequest.getAadharCardNumber().isBlank()) {
            staffProfile.setAadharCardNumber(staffProfileRequest.getAadharCardNumber());
        }

        if (staffProfileRequest.getLicenseValidityDate() != null) {
            staffProfile.setLicenseValidityDate(staffProfileRequest.getLicenseValidityDate());
        }

        if (staffProfileRequest.getLicenseRenewalDate() != null) {
            staffProfile.setLicenseRenewalDate(staffProfileRequest.getLicenseRenewalDate());
        }

        // State board certificate: replace any existing one
        if (stateBoardCertificate != null && !stateBoardCertificate.isEmpty()) {
            staffDocumentRepository.deleteByStaffProfileIdAndDocumentType(
                    staffProfile.getId(), StaffDocumentType.STATE_BOARD_CERTIFICATE);
            String certPath = storeFile(stateBoardCertificate, user.getId(), "certificate");
            saveDocument(staffProfile, StaffDocumentType.STATE_BOARD_CERTIFICATE,
                    safeFileName(stateBoardCertificate, certPath), certPath);
        }

        // Educational documents: append to existing certificates
        if (educationalDocuments != null) {
            for (MultipartFile file : educationalDocuments) {
                if (file != null && !file.isEmpty()) {
                    String path = storeFile(file, user.getId(), "education");
                    saveDocument(staffProfile, StaffDocumentType.EDUCATIONAL_CERTIFICATE,
                            safeFileName(file, path), path);
                }
            }
        }

        // Photos: append to existing photos
        if (photos != null) {
            for (MultipartFile photo : photos) {
                if (photo != null && !photo.isEmpty()) {
                    String path = storeFile(photo, user.getId(), "photos");
                    saveDocument(staffProfile, StaffDocumentType.PHOTO,
                            safeFileName(photo, path), path);
                }
            }
        }

        staffProfileRepository.save(staffProfile);

        return buildStaffProfileResponse(staffProfile);
    }

    private boolean hasDocumentFiles(MultipartFile stateBoardCertificate,
                                     List<MultipartFile> educationalDocuments,
                                     List<MultipartFile> photos) {
        return (stateBoardCertificate != null && !stateBoardCertificate.isEmpty())
                || hasNonEmptyFile(educationalDocuments)
                || hasNonEmptyFile(photos);
    }

    private boolean hasNonEmptyFile(List<MultipartFile> files) {
        if (files == null) {
            return false;
        }
        return files.stream().anyMatch(file -> file != null && !file.isEmpty());
    }

    @Override
    @Transactional
    public StaffProfileResponseDto verifyStaffProfile(Long userId, boolean verified) throws ResourceNotFoundException {
        StaffProfile staffProfile = staffProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Staff profile not found for user id : " + userId
                ));

        staffProfile.setVerified(verified);
        staffProfileRepository.save(staffProfile);

        User staffUser = staffProfile.getUser();
        if (verified) {
            emailService.sendProfileVerifiedEmail(staffUser.getEmail(), staffUser.getFirstName());
        } else {
            emailService.sendProfileRejectedEmail(staffUser.getEmail(), staffUser.getFirstName());
        }

        return buildStaffProfileResponse(staffProfile);
    }

    @Override
    @Transactional(readOnly = true)
    public StaffProfileResponseDto getStaffProfile(String email) throws UserNotFoundException, ResourceNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with email : " + email
                ));

        StaffProfile staffProfile = staffProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Staff profile not found for user : " + email
                ));

        return buildStaffProfileResponse(staffProfile);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StaffProfileResponseDto> getAllStaffProfiles(Pageable pageable) {
        Page<StaffProfile> profilePage = staffProfileRepository.findAll(pageable);

        List<Long> profileIds = profilePage.getContent().stream()
                .map(StaffProfile::getId)
                .toList();

        // Batch load documents for all profiles on this page in ONE IN query
        // (instead of N queries via buildStaffProfileResponse).
        Map<Long, List<StaffDocument>> documentsByProfile = profileIds.isEmpty()
                ? Map.of()
                : staffDocumentRepository.findByStaffProfileIdIn(profileIds).stream()
                        .collect(Collectors.groupingBy(d -> d.getStaffProfile().getId()));

        return profilePage.map(profile -> buildStaffProfileResponse(
                profile,
                documentsByProfile.getOrDefault(profile.getId(), List.of())
        ));
    }

    private String storeFile(MultipartFile file, Long userId, String folder) {
        try {
            Path uploadPath = Paths.get(uploadDir, "staff", String.valueOf(userId), folder);
            Files.createDirectories(uploadPath);

            String originalName = file.getOriginalFilename();
            String extension = "";
            if (originalName != null && originalName.contains(".")) {
                String rawExtension = originalName.substring(originalName.lastIndexOf('.') + 1);
                // Only keep a safe alphanumeric extension to prevent path traversal
                if (rawExtension.matches("[A-Za-z0-9]{1,10}")) {
                    extension = "." + rawExtension;
                }
            }

            String storedName = UUID.randomUUID() + extension;
            Path target = uploadPath.resolve(storedName);
            file.transferTo(target);
            return target.toString().replace('\\', '/');
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store uploaded file", e);
        }
    }

    private String safeFileName(MultipartFile file, String fallbackPath) {
        String originalName = file.getOriginalFilename();
        return (originalName == null || originalName.isBlank())
                ? fallbackPath.substring(fallbackPath.lastIndexOf('/') + 1)
                : originalName;
    }

    private void saveDocument(StaffProfile staffProfile, StaffDocumentType type,
                              String fileName, String filePath) {
        StaffDocument document = new StaffDocument();
        document.setStaffProfile(staffProfile);
        document.setDocumentType(type);
        document.setFileName(fileName);
        document.setFilePath(filePath);
        staffDocumentRepository.save(document);
    }

    private StaffProfileResponseDto buildStaffProfileResponse(StaffProfile staffProfile) {
        List<StaffDocument> documents = staffDocumentRepository.findByStaffProfileId(staffProfile.getId());
        return buildStaffProfileResponse(staffProfile, documents);
    }

    private StaffProfileResponseDto buildStaffProfileResponse(StaffProfile staffProfile,
                                                              List<StaffDocument> documents) {
        String stateBoardCertificatePath = documents.stream()
                .filter(d -> d.getDocumentType() == StaffDocumentType.STATE_BOARD_CERTIFICATE)
                .map(StaffDocument::getFilePath)
                .findFirst()
                .orElse(null);

        List<String> educationalDocumentPaths = documents.stream()
                .filter(d -> d.getDocumentType() == StaffDocumentType.EDUCATIONAL_CERTIFICATE)
                .map(StaffDocument::getFilePath)
                .toList();

        List<String> photoPaths = documents.stream()
                .filter(d -> d.getDocumentType() == StaffDocumentType.PHOTO)
                .map(StaffDocument::getFilePath)
                .toList();

        return StaffProfileResponseDto.builder()
                .id(staffProfile.getId())
                .firstName(staffProfile.getUser().getFirstName())
                .lastName(staffProfile.getUser().getLastName())
                .email(staffProfile.getUser().getEmail())
                .phone(staffProfile.getUser().getPhone())
                .staffCategory(staffProfile.getStaffCategory())
                .aadharCardNumber(staffProfile.getAadharCardNumber())
                .licenseValidityDate(staffProfile.getLicenseValidityDate())
                .licenseRenewalDate(staffProfile.getLicenseRenewalDate())
                .verified(staffProfile.isVerified())
                .stateBoardCertificatePath(stateBoardCertificatePath)
                .educationalDocumentPaths(educationalDocumentPaths)
                .photoPaths(photoPaths)
                .build();
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < otpLength; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }

    private AuthResponseDto buildAuthResponse(User user) {
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        UserResponseDto userResponseDto = modelMapper.map(user, UserResponseDto.class);
        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userResponseDto)
                .build();
    }

    private String[] splitName(String fullName) {
        String trimmed = fullName == null ? "" : fullName.trim();
        if (trimmed.isEmpty()) {
            return new String[]{"", ""};
        }
        int firstSpace = trimmed.indexOf(' ');
        if (firstSpace == -1) {
            return new String[]{trimmed, ""};
        }
        return new String[]{trimmed.substring(0, firstSpace), trimmed.substring(firstSpace + 1)};
    }
}

package com.nurseadda.project.service.impl;

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
import com.nurseadda.project.entity.Client;
import com.nurseadda.project.entity.StaffDocument;
import com.nurseadda.project.entity.StaffProfile;
import com.nurseadda.project.entity.User;
import com.nurseadda.project.enums.Role;
import com.nurseadda.project.enums.StaffDocumentType;
import com.nurseadda.project.model.PendingRegistration;
import com.nurseadda.project.repository.ClientRepository;
import com.nurseadda.project.repository.PendingRegistrationRepository;
import com.nurseadda.project.repository.StaffDocumentRepository;
import com.nurseadda.project.repository.StaffProfileRepository;
import com.nurseadda.project.repository.UserRepository;
import com.nurseadda.project.security.JwtUtil;
import com.nurseadda.project.service.AuthService;
import com.nurseadda.project.service.EmailService;
import com.nurseadda.project.service.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.UUID;

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
                .phone(clientRegisterRequest.getPhone())
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
    public UserResponseDto updateProfile(String email, UpdateProfileRequest updateProfileRequest) throws UserNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with email : " + email
                ));

        if (updateProfileRequest.getFirstName() != null
                && !updateProfileRequest.getFirstName().isBlank()) {
            user.setFirstName(updateProfileRequest.getFirstName());
        }

        if (updateProfileRequest.getLastName() != null
                && !updateProfileRequest.getLastName().isBlank()) {
            user.setLastName(updateProfileRequest.getLastName());
        }

        if (updateProfileRequest.getPhone() != null
                && !updateProfileRequest.getPhone().isBlank()) {
            user.setPhone(updateProfileRequest.getPhone());
        }

        User updatedUser = userRepository.save(user);
        return modelMapper.map(updatedUser, UserResponseDto.class);
    }

    @Override
    @Transactional
    public StaffProfileResponseDto updateStaffProfile(
            String email,
            StaffProfileRequest staffProfileRequest,
            MultipartFile passportPhoto,
            List<MultipartFile> educationalDocuments
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
        if (staffProfile.isVerified() && hasDocumentFiles(passportPhoto, educationalDocuments)) {
            throw new IllegalArgumentException(
                    "Your profile is verified. Documents can only be changed when "
                            + "the administrator requests a replacement."
            );
        }

        if (staffProfileRequest.getAadharCardNumber() != null
                && !staffProfileRequest.getAadharCardNumber().isBlank()) {
            staffProfile.setAadharCardNumber(staffProfileRequest.getAadharCardNumber());
        }

        if (staffProfileRequest.getQualification() != null) {
            staffProfile.setQualification(staffProfileRequest.getQualification());
        }

        if (staffProfileRequest.getLicenseNumber() != null
                && !staffProfileRequest.getLicenseNumber().isBlank()) {
            staffProfile.setLicenseNumber(staffProfileRequest.getLicenseNumber());
        }

        if (staffProfileRequest.getLicenseExpiryDate() != null) {
            staffProfile.setLicenseExpiryDate(staffProfileRequest.getLicenseExpiryDate());
        }

        if (staffProfileRequest.getYearsOfExperience() != null) {
            staffProfile.setYearsOfExperience(staffProfileRequest.getYearsOfExperience());
        }

        if (staffProfileRequest.getPanCardNumber() != null
                && !staffProfileRequest.getPanCardNumber().isBlank()) {
            staffProfile.setPanCardNumber(staffProfileRequest.getPanCardNumber());
        }

        // Passport photo: replace any existing passport photo
        if (passportPhoto != null && !passportPhoto.isEmpty()) {
            staffDocumentRepository.deleteByStaffProfileIdAndDocumentType(
                    staffProfile.getId(), StaffDocumentType.PASSPORT_PHOTO);
            String photoPath = storeFile(passportPhoto, user.getId(), "passport");
            saveDocument(staffProfile, StaffDocumentType.PASSPORT_PHOTO,
                    safeFileName(passportPhoto, photoPath), photoPath);
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

        staffProfileRepository.save(staffProfile);

        return buildStaffProfileResponse(staffProfile);
    }

    private boolean hasDocumentFiles(MultipartFile passportPhoto,
                                     List<MultipartFile> educationalDocuments) {
        return (passportPhoto != null && !passportPhoto.isEmpty())
                || hasNonEmptyFile(educationalDocuments);
    }

    private boolean hasNonEmptyFile(List<MultipartFile> files) {
        if (files == null) {
            return false;
        }
        return files.stream().anyMatch(file -> file != null && !file.isEmpty());
    }

    private String storeFile(MultipartFile file, Long userId, String folder) {
        try {
            Path uploadPath = Paths.get(uploadDir, "staff", String.valueOf(userId), folder);
            Files.createDirectories(uploadPath);

            String originalName = file.getOriginalFilename();
            String extension = "";
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf('.'));
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

        String passportPhotoPath = documents.stream()
                .filter(d -> d.getDocumentType() == StaffDocumentType.PASSPORT_PHOTO)
                .map(StaffDocument::getFilePath)
                .findFirst()
                .orElse(null);

        List<String> educationalDocumentPaths = documents.stream()
                .filter(d -> d.getDocumentType() == StaffDocumentType.EDUCATIONAL_CERTIFICATE)
                .map(StaffDocument::getFilePath)
                .toList();

        return StaffProfileResponseDto.builder()
                .id(staffProfile.getId())
                .staffCategory(staffProfile.getStaffCategory())
                .aadharCardNumber(staffProfile.getAadharCardNumber())
                .qualification(staffProfile.getQualification())
                .licenseNumber(staffProfile.getLicenseNumber())
                .licenseExpiryDate(staffProfile.getLicenseExpiryDate())
                .yearsOfExperience(staffProfile.getYearsOfExperience())
                .panCardNumber(staffProfile.getPanCardNumber())
                .verified(staffProfile.isVerified())
                .passportPhotoPath(passportPhotoPath)
                .educationalDocumentPaths(educationalDocumentPaths)
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

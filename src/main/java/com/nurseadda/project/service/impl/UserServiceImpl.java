package com.nurseadda.project.service.impl;

import com.nurseadda.project.common.exception.UserAlreadyExistException;
import com.nurseadda.project.dto.response.UserResponseDto;
import com.nurseadda.project.entity.User;
import com.nurseadda.project.repository.UserRepository;
import com.nurseadda.project.service.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    public UserResponseDto save(User user) throws UserAlreadyExistException {
        if (existsByEmail(user.getEmail())) {
            throw new UserAlreadyExistException(
                    "User already exists with email : "
                            + user.getEmail()
            );
        }

        User savedUser = userRepository.save(user);
        return modelMapper.map(savedUser, UserResponseDto.class);
    }
}

package com.nurseadda.project.service.impl;

import com.nurseadda.project.common.exception.ResourceNotFoundException;
import com.nurseadda.project.dto.request.ClientRequest;
import com.nurseadda.project.dto.response.ClientResponse;
import com.nurseadda.project.entity.Client;
import com.nurseadda.project.entity.User;
import com.nurseadda.project.repository.ClientRepository;
import com.nurseadda.project.repository.UserRepository;
import com.nurseadda.project.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;

    @Override
    @Transactional
    public ClientResponse updateProfile(String email, ClientRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        Client client = clientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Client", "user", email));

        client.setOrganizationName(request.getOrganizationName());
        client.setOrganizationType(request.getOrganizationType());
        client.setContactPerson(request.getContactPerson());
        client.setAddress(request.getAddress());

        clientRepository.save(client);

        return toResponse(client, user);
    }

    @Override
    @Transactional(readOnly = true)
    public ClientResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        Client client = clientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Client", "user", email));

        return toResponse(client, user);
    }

    private ClientResponse toResponse(Client client, User user) {
        return ClientResponse.builder()
                .id(client.getId())
                .userId(user.getId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .organizationName(client.getOrganizationName())
                .organizationType(client.getOrganizationType())
                .contactPerson(client.getContactPerson())
                .address(client.getAddress())
                .build();
    }
}

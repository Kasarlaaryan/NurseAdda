package com.nurseadda.project.controller;

import com.nurseadda.project.dto.request.ClientRequest;
import com.nurseadda.project.dto.response.ClientResponse;
import com.nurseadda.project.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @PreAuthorize("hasRole('CLIENT')")
    @PutMapping("/profile")
    public ResponseEntity<ClientResponse> updateProfile(
            Authentication authentication,
            @Valid @RequestBody ClientRequest request) {
        return ResponseEntity.ok(clientService.updateProfile(authentication.getName(), request));
    }

    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/profile")
    public ResponseEntity<ClientResponse> getProfile(Authentication authentication) {
        return ResponseEntity.ok(clientService.getProfile(authentication.getName()));
    }
}

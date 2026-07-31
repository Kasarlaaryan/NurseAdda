package com.nurseadda.project.service;

import com.nurseadda.project.dto.request.ClientRequest;
import com.nurseadda.project.dto.response.ClientResponse;

public interface ClientService {

    ClientResponse updateProfile(String email, ClientRequest request);

    ClientResponse getProfile(String email);
}

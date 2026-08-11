package com.nurseadda.project.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogoutRequest {

    /**
     * Optional refresh token. When provided it is also revoked, so the whole
     * session is invalidated instead of just the current access token.
     */
    private String refreshToken;
}

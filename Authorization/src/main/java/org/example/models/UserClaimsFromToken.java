package org.example.models;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.*;

public record UserClaimsFromToken(
        String token,
        String subject,
        Map<String, Claim> claims,
        Date expiresAt,
        Date issuedAt
) {

    public UserClaimsFromToken(DecodedJWT decodedJWT) {
        this(
                decodedJWT.getToken(),
                decodedJWT.getSubject(),
                decodedJWT.getClaims(),
                decodedJWT.getExpiresAt(),
                decodedJWT.getIssuedAt()
        );
    }
}

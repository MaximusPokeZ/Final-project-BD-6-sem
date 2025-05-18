package org.example.services;

import org.example.dto.response.TokenResponse;

public interface RegisterService {
  TokenResponse register(String username);
}

package org.example.controllers.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.example.controllers.RegisterController;
import org.example.dto.response.TokenResponse;
import org.example.services.RegisterService;

@RestController
@RequiredArgsConstructor
public class RegisterControllerImpl implements RegisterController {



  private final RegisterService registerService;

  @Override
  @GetMapping("/register")
  @ResponseStatus(HttpStatus.OK)
  public TokenResponse register(@RequestParam("username") String username) {
    return registerService.register(username);
  }
}

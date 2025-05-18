package org.example.services.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.example.dto.response.TokenResponse;
import org.example.models.User;
import org.example.services.RegisterService;
import org.example.services.UserService;

import java.time.Duration;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegisterServiceImpl implements RegisterService {

  private final UserService userService;

  @Value("${token.signing.secret}")
  private String secret;

  @Value("${token.issuer}")
  private String issuer;

  @Value("${token.lifetime}")
  private Duration tokenLifetime;

  @Override
  public TokenResponse register(String username) {

    Date issuredDate = new Date();
    Date expiredDate = new Date(issuredDate.getTime() + tokenLifetime.toMillis());

    if (userService.findUserByUsername(username).isPresent()) {
      log.info("there is such user in the table!");
      return new TokenResponse();
    }

    User tmpUser = new User();
    tmpUser.setUsername(username);
    User userFromDB = userService.createUser(tmpUser);


    Algorithm algorithm = Algorithm.HMAC256(secret);

    String jwtEncodedString = JWT.create()
            .withIssuer(issuer)
            .withSubject(userFromDB.getUsername())
            .withIssuedAt(issuredDate)
            .withExpiresAt(expiredDate)
            .sign(algorithm);

    return new TokenResponse(jwtEncodedString);
  }
}

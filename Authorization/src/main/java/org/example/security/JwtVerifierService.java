package org.example.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.example.services.UserService;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtVerifierService {

  private final UserService userService;

  @Value("${token.signing.secret}")
  private String secret;

  @Value("${token.issuer}")
  private String issuer;

  public boolean verify(String token) {

    if (token == null) {
      log.warn("token is null");
      return false;
    }

    Algorithm algorithm = Algorithm.HMAC256(secret);

    JWTVerifier jwtVerifier = JWT.require(algorithm).withIssuer(issuer).build();

    String username;
    try{
      DecodedJWT decodedJWT = jwtVerifier.verify(token);
      username = decodedJWT.getSubject();
    } catch (TokenExpiredException e) {
      log.warn("Token has expired: {}", e.getMessage());
      return false;
    } catch (SignatureVerificationException e) {
      log.warn("Token signature is invalid: {}", e.getMessage());
      return false;
    } catch (JWTVerificationException e) {
      log.warn("Token verification failed: {}", e.getMessage());
      return false;
    }


    try{
      userService.loadUserByUsername(username);
    } catch (UsernameNotFoundException e) {
      log.warn("Да нет такого юзера!!!!: {}", username);
      return false;
    }

    log.info("User loaded: {}", username);

    return true;
  }
}

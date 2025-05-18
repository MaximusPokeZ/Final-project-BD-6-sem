package org.example.utils;

import com.auth0.jwt.JWT;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.example.exception.ParseTokenException;

@UtilityClass
public class TokenUtils {

  private static final String TOKEN_PREFIX = "Bearer ";

  public String getSubject(String token) {
    return JWT.decode(token).getSubject();
  }

  public String extractToken(String header) {
    if (StringUtils.isBlank(header) || !header.startsWith(TOKEN_PREFIX)) {
      return null;
    }


    try {
      return header.substring(TOKEN_PREFIX.length());
    } catch (Exception ex) {
      throw new ParseTokenException("Invalid token");
    }
  }
}

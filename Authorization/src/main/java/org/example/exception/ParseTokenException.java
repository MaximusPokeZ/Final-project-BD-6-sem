package org.example.exception;

public class ParseTokenException extends RuntimeException {

  public ParseTokenException(String message) {
    super(message);
  }
  public ParseTokenException(String message, Throwable cause) {
    super(message, cause);
  }
}

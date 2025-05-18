package org.example.services;

import org.example.models.User;

import java.util.Optional;

public interface UserService {

  User createUser(User user);

  User loadUserByUsername(String username);

  Optional<User> findUserByUsername(String username);
}

package com.miniecommerce.userservice.service;

import com.miniecommerce.userservice.entity.User;
import com.miniecommerce.userservice.exception.ResourceAlreadyExistsException;
import com.miniecommerce.userservice.exception.ResourceNotFoundException;
import com.miniecommerce.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;

    public List<User> getAllUsers() {
        log.info("Fetching all users");
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        log.info("Fetching user with id: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    public User createUser(User user) {
        log.info("Creating new user with email: {}", user.getEmail());

        // Check if email already exists
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new ResourceAlreadyExistsException("User", "email", user.getEmail());
        }

        User savedUser = userRepository.save(user);
        log.info("User created successfully with id: {}", savedUser.getId());
        return savedUser;
    }

    public User updateUser(Long id, User userDetails) {
        log.info("Updating user with id: {}", id);

        User user = getUserById(id);

        // Check if email is being changed and if it already exists
        if (!user.getEmail().equals(userDetails.getEmail()) &&
                userRepository.findByEmail(userDetails.getEmail()).isPresent()) {
            throw new ResourceAlreadyExistsException("User", "email", userDetails.getEmail());
        }

        user.setName(userDetails.getName());
        user.setEmail(userDetails.getEmail());
        user.setPhone(userDetails.getPhone());

        User updatedUser = userRepository.save(user);
        log.info("User updated successfully");
        return updatedUser;
    }

    public void deleteUser(Long id) {
        log.info("Deleting user with id: {}", id);
        User user = getUserById(id);
        userRepository.delete(user);
        log.info("User deleted successfully");
    }
}

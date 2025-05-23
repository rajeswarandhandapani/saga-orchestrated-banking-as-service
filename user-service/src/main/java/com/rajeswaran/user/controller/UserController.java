package com.rajeswaran.user.controller;

import com.rajeswaran.user.entity.User;
import com.rajeswaran.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping
    public List<User> getAllUsers() {
        log.info("Received request: getAllUsers");
        List<User> users = userService.getAllUsers();
        log.info("Completed request: getAllUsers, count={}", users.size());
        return users;
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        log.info("Received request: getUserById, id={}", id);
        Optional<User> user = userService.getUserById(id);
        if (user.isPresent()) {
            log.info("Completed request: getUserById, found userId={}", id);
            return ResponseEntity.ok(user.get());
        } else {
            log.info("Completed request: getUserById, userId={} not found", id);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        log.info("Received request: createUser, payload={}", user);
        User created = userService.createUser(user);
        log.info("Completed request: createUser, createdId={}", created.getId());
        return created;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("Received request: deleteUser, id={}", id);
        userService.deleteUser(id);
        log.info("Completed request: deleteUser, id={}", id);
        return ResponseEntity.noContent().build();
    }
}

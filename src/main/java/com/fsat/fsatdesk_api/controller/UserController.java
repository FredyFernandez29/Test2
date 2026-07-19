package com.fsat.fsatdesk_api.controller;

import com.fsat.fsatdesk_api.model.User;
import com.fsat.fsatdesk_api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    @GetMapping
    public List<User> getAll() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public User getOne(@PathVariable String id) {
        return userService.findById(id).orElseThrow();
    }

    @PostMapping
    public User create(@RequestBody User user, @RequestParam String password) {
        return userService.createUser(user, password);
    }

    @PutMapping("/{id}")
    public User update(@PathVariable String id, @RequestBody User user, @RequestParam(required = false) String newPassword) {
        return userService.updateUser(id, user, newPassword);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        userService.deleteUser(id);
    }

    @PatchMapping("/{id}/toggle")
    public void toggle(@PathVariable String id) {
        userService.toggleActivo(id);
    }
}
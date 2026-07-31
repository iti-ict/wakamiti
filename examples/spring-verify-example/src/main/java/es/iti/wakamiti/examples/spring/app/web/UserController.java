/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.wakamiti.examples.spring.app.web;


import java.util.List;

import jakarta.persistence.EntityNotFoundException;

import es.iti.wakamiti.examples.spring.app.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import es.iti.wakamiti.examples.spring.app.model.UserDAO;


/**
 * REST controller that exposes the user management operations of the example
 * application.
 */
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserDAO userDAO;

    /**
     * Lists all persisted users.
     *
     * @return users currently stored by the example repository
     */
    @GetMapping
    public List<User> getAllUsers() {
        return userDAO.getAllUsers();
    }

    /**
     * Retrieves one user by identifier.
     *
     * @param id user identifier
     * @return requested user
     * @throws EntityNotFoundException when no user exists with the given id
     */
    @GetMapping("/{id}")
    public User getUser(@PathVariable("id") int id) {
        return userDAO.getUserById(id);
    }

    /**
     * Creates a new user.
     *
     * @param user payload to persist
     * @return persisted user
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User createUser(@RequestBody User user) {
        return userDAO.createUser(user);
    }

    /**
     * Replaces one existing user.
     *
     * @param id user identifier
     * @param user replacement payload
     * @return updated user
     * @throws EntityNotFoundException when no user exists with the given id
     */
    @PutMapping("/{id}")
    public User modifyUser(
            @PathVariable("id") int id,
            @RequestBody User user
    ) {
        return userDAO.modifyUser(id, user);
    }

    /**
     * Deletes one user by identifier.
     *
     * @param id user identifier
     * @throws EntityNotFoundException when no user exists with the given id
     */
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable("id") int id) {
        userDAO.deleteUser(id);
    }

    /**
     * Converts missing-entity errors into HTTP 404 responses.
     *
     * @param e thrown exception
     * @return error message sent to the client
     */
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String entityNotFound(EntityNotFoundException e) {
        return e.getMessage();
    }

}

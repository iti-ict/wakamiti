/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.wakamiti.examples.spring.junit;


import java.util.List;

import jakarta.persistence.EntityNotFoundException;

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


/**
 * REST controller exposing CRUD operations for {@link User} entities.
 */
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserDAO userDAO;

    /**
     * Lists every persisted user.
     *
     * @return users currently stored in the repository
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
     * @throws EntityNotFoundException when no user exists with the given
     *                                 identifier
     */
    @GetMapping("{id}")
    public User getUser(@PathVariable int id) {
        return userDAO.getUserById(id);
    }

    /**
     * Creates a new user.
     *
     * @param user user payload to persist
     * @return persisted user including generated fields
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User createUser(@RequestBody User user) {
        return userDAO.createUser(user);
    }

    /**
     * Replaces the persisted data of one existing user.
     *
     * @param id   user identifier
     * @param user user payload with the new values
     * @return updated user
     * @throws EntityNotFoundException when no user exists with the given
     *                                 identifier
     */
    @PutMapping
    public User modifyUser(
            @PathVariable int id,
            @RequestBody User user
    ) {
        return userDAO.modifyUser(id, user);
    }

    /**
     * Deletes one user by identifier.
     *
     * @param id user identifier
     * @throws EntityNotFoundException when no user exists with the given
     *                                 identifier
     */
    @DeleteMapping
    public void deleteUser(@PathVariable int id) {
        userDAO.deleteUser(id);
    }

    /**
     * Converts repository {@link EntityNotFoundException} into a 404 response.
     *
     * @param e raised exception
     * @return plain-text error message
     */
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String entityNotFound(EntityNotFoundException e) {
        return e.getMessage();
    }

}

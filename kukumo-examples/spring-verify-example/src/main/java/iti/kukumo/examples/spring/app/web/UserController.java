/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.examples.spring.app.web;


import java.util.List;

import javax.persistence.EntityNotFoundException;

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

import iti.kukumo.examples.spring.app.model.User;
import iti.kukumo.examples.spring.app.model.UserDAO;


@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserDAO userDAO;


    @GetMapping
    public List<User> getAllUsers() {
        return userDAO.getAllUsers();
    }


    @GetMapping("{id}")
    public User getUser(@PathVariable int id) {
        return userDAO.getUserById(id);
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User createUser(@RequestBody User user) {
        return userDAO.createUser(user);
    }


    @PutMapping
    public User modifyUser(@PathVariable int id, @RequestBody User user) {
        return userDAO.modifyUser(id, user);
    }


    @DeleteMapping
    public void deleteUser(@PathVariable int id) {
        userDAO.deleteUser(id);
    }


    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String entityNotFound(EntityNotFoundException e) {
        return e.getMessage();
    }

}
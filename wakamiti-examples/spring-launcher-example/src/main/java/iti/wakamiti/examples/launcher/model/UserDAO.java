/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package iti.wakamiti.examples.launcher.model;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;

import iti.wakamiti.examples.launcher.dto.UserDTO;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class UserDAO {

    @PersistenceContext
    private EntityManager entityManager;


    public List<UserDTO> getAllUsers() {
        return entityManager.createQuery("select u from User u",User.class).getResultList()
            .stream()
            .map(UserDTO::new)
            .collect(Collectors.toList());
    }

    public UserDTO getUserById(int id) {
        User user = entityManager.find(User.class, id);
        if (user == null) {
            throw new EntityNotFoundException();
        }
        return new UserDTO(user);
    }

    public boolean userExists(int id) {
        return getUserById(id) != null;
    }

    @Transactional
    public UserDTO createUser(UserDTO userDTO) {
        User user = new User();
        user.firstName = userDTO.firstName;
        user.lastName = userDTO.lastName;
        entityManager.persist(user);
        return new UserDTO(user);
    }

    public void deleteUser(int id) {
        User user = entityManager.find(User.class, id);
        if (user == null) {
            throw new EntityNotFoundException();
        }
        entityManager.remove(user);
    }

    public UserDTO modifyUser(int id, UserDTO userDTO) {
        User user = entityManager.find(User.class, id);
        if (user == null) {
            throw new EntityNotFoundException();
        }
        user.firstName = userDTO.firstName;
        user.lastName = userDTO.lastName;
        return userDTO;
    }

}
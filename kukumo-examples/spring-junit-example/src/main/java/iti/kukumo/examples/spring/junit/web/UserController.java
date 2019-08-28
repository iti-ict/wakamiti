package iti.kukumo.examples.spring.junit.web;

import iti.kukumo.examples.spring.junit.model.User;
import iti.kukumo.examples.spring.junit.model.UserDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.util.List;


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

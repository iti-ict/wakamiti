package iti.kukumo.examples.spring.app.model;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class UserDAO {

    @PersistenceContext 
    private EntityManager entityManager;
    
    
    public List<User> getAllUsers() {
        return entityManager.createQuery("select u from User u",User.class).getResultList();
    }
    
    public User getUserById(int id) {
        User user = entityManager.find(User.class, id);
        if (user == null) {
            throw new EntityNotFoundException();
        }
        return user;
    }
    
    public boolean userExists(int id) {
        return getUserById(id) != null;
    }
    
    @Transactional
    public User createUser(User user) {
        entityManager.persist(user);
        return user;
    }
    
    public void deleteUser(int id) {
        User user = getUserById(id);
        if (user == null) {
            throw new EntityNotFoundException();
        }
        entityManager.remove(user);
    }
    
    public User modifyUser(int id, User user) {
        if (!userExists(id)) {
            throw new EntityNotFoundException();
        }
        user.id = id;
        return entityManager.merge(user);
        
    }
    
}

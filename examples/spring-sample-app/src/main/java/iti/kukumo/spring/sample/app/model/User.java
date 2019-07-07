package iti.kukumo.spring.sample.app.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class User {

    @Id
    public int id;
    
    @Column
    public String firstName;
    
    @Column
    public String lastName;
    
}

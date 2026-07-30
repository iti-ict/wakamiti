/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.examples.spring.junit;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


/**
 * Provides the User functionality used by Wakamiti.
 */
@Entity
@Table(name = "USERR") // Hd
public class User {

    /** Primary key persisted in the example's {@code USERR} table. */
    @Id
    public int id;

    /** User's given name used by the Spring/JUnit example assertions. */
    @Column
    public String firstName;

    /** User's family name used by the Spring/JUnit example assertions. */
    @Column
    public String lastName;

    /** @return database identifier of this example user */
    public int getId() {
        return id;
    }

    /** @return user's given name */
    public String getFirstName() {
        return firstName;
    }

    /** @return user's family name */
    public String getLastName() {
        return lastName;
    }

    /** @param id database identifier to assign */
    public void setId(int id) {
        this.id = id;
    }

    /** @param firstName user's given name */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /** @param lastName user's family name */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

}

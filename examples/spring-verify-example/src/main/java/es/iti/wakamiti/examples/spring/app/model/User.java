/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.wakamiti.examples.spring.app.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


/**
 * Provides the User functionality used by Wakamiti.
 */
@Entity
@Table(name = "APP_USER")
public class User {

    /** Primary key persisted in the {@code APP_USER} table. */
    @Id
    public int id;

    /** User's given name persisted by the verification example. */
    @Column
    public String firstName;

    /** User's family name persisted by the verification example. */
    @Column
    public String lastName;

}

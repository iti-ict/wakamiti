/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package iti.kukumo.examples.spring.junit;

public class UserDTO {

    public int id;
    public String firstName;
    public String lastName;

    public UserDTO() {

    }
    public UserDTO(User user) {
        this.id = user.id;
        this.firstName = user.firstName;
        this.lastName = user.lastName;
    }
}
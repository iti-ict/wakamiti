/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package iti.commons.slf4jansi;

import java.util.Properties;


public class DefaultStyles {

    public static Properties asProperties() {
        Properties styles = new Properties();
        styles.put("error","red,bold");
        styles.put("warn","yellow,bold");
        styles.put("trace","faint");
        styles.put("uri", "blue,underline");
        styles.put("id","cyan,bold");
        styles.put("important","magenta,bold");
        styles.put("highlight","white,bold");
        return styles;
    }

}
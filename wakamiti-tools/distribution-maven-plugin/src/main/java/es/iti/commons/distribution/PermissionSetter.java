/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.commons.distribution;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;


public class PermissionSetter {


    public static void set(Path file, Access access) throws IOException {
        Set<PosixFilePermission> permissions = new HashSet<>();
        collectReadPermissions(access,permissions);
        collectWritePermissions(access,permissions);
        collectExecutePermissions(access,permissions,file.toFile().isDirectory());
        Files.setPosixFilePermissions(file, permissions);
    }




    private static void collectWritePermissions(Access access, Set<PosixFilePermission> permissions) {
        switch (access.getWritableBy()) {
        case Access.ANY:
            permissions.add(PosixFilePermission.OTHERS_WRITE);
            permissions.add(PosixFilePermission.GROUP_WRITE);
            permissions.add(PosixFilePermission.OWNER_WRITE);
            break;
        case Access.GROUP:
            permissions.add(PosixFilePermission.GROUP_WRITE);
            permissions.add(PosixFilePermission.OWNER_WRITE);
            break;
        default:
            permissions.add(PosixFilePermission.OWNER_WRITE);
        }
    }

    private static void collectReadPermissions(Access access, Set<PosixFilePermission> permissions) {
        switch (access.getReadableBy()) {
        case Access.ANY:
            permissions.add(PosixFilePermission.OTHERS_READ);
            permissions.add(PosixFilePermission.GROUP_READ);
            permissions.add(PosixFilePermission.OWNER_READ);
            break;
        case Access.GROUP:
            permissions.add(PosixFilePermission.GROUP_READ);
            permissions.add(PosixFilePermission.OWNER_READ);
            break;
        default:
            permissions.add(PosixFilePermission.OWNER_READ);
        }
    }



    private static void collectExecutePermissions(Access access, Set<PosixFilePermission> permissions, boolean isFolder) {
        switch (access.getExecutableBy()) {
        case Access.ANY:
            permissions.add(PosixFilePermission.OTHERS_EXECUTE);
            permissions.add(PosixFilePermission.GROUP_EXECUTE);
            permissions.add(PosixFilePermission.OWNER_EXECUTE);
            break;
        case Access.GROUP:
            permissions.add(PosixFilePermission.GROUP_EXECUTE);
            permissions.add(PosixFilePermission.OWNER_EXECUTE);
            break;
        case Access.USER:
            permissions.add(PosixFilePermission.OWNER_EXECUTE);
            break;
        }
        if (isFolder) {
            switch (access.getReadableBy()) {
            case Access.ANY:
                permissions.add(PosixFilePermission.OTHERS_EXECUTE);
                permissions.add(PosixFilePermission.GROUP_EXECUTE);
                permissions.add(PosixFilePermission.OWNER_EXECUTE);
                break;
            case Access.GROUP:
                permissions.add(PosixFilePermission.GROUP_EXECUTE);
                permissions.add(PosixFilePermission.OWNER_EXECUTE);
                break;
            default:
                permissions.add(PosixFilePermission.OWNER_EXECUTE);
            }
        }
    }

}
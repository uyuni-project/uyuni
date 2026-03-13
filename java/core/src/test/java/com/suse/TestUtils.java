/*
 * Copyright (c) 2026 SUSE LCC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse;

import com.redhat.rhn.common.conf.ConfigDefaults;

import java.lang.reflect.Field;

/**
 * This Utilities class provides methods to assist with testing.
 * No specific scope is defined for it.
 */
public class TestUtils {
    /**
     * Overrides the ConfigDefaults instance
     * @param configDefaultsIn the ConfigDefaults instance
     * @throws NoSuchFieldException if a field with the specified name is not found.
     * @throws IllegalAccessException if the field is not accessible.
     */
    @SuppressWarnings("java:S3011")
    public static void setConfigDefaultsInstance(ConfigDefaults configDefaultsIn)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = ConfigDefaults.class.getDeclaredField("instance");
        field.setAccessible(true);
        field.set(null, configDefaultsIn);
    }

    private TestUtils() {
        // prevent instantiation
    }
}

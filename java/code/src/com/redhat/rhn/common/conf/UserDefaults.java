/*
 * Copyright (c) 2010 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.common.conf;


/**
 * UserDefaults
 */
public class UserDefaults {
    private static UserDefaults instance = new UserDefaults();
    public static final String MAX_USER_LENGTH = "max_user_len";
    public static final String MIN_USER_LENGTH = "min_user_len";
    public static final String MAX_EMAIL_LENGTH = "min_email_len";
    /**
     * Get instance of UserDefaults.
     * @return UserDefaults instance.
     */
    public static UserDefaults get() {
        return instance;
    }

    /**
     * @return the max login name length
     */
    public int getMaxUserLength() {
        return Config.get().getInt(MAX_USER_LENGTH, 64);
    }

    /**
     * @return the min login name length
     */
    public int getMinUserLength() {
        return Config.get().getInt(MIN_USER_LENGTH, 5);
    }

    /**
     * @return the max email length
     */
    public int getMaxEmailLength() {
        return Config.get().getInt(MAX_EMAIL_LENGTH, 128);
    }
}

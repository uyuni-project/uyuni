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
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.common.conf;

import com.redhat.rhn.domain.common.RhnConfiguration;
import com.redhat.rhn.domain.common.RhnConfigurationFactory;

/**
 * UserDefaults
 */
public class UserDefaults {
    private static UserDefaults instance = new UserDefaults();
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
        return RhnConfigurationFactory.getSingleton().getInteger(RhnConfiguration.KEYS.USER_LOGIN_LENGTH_MAX);
    }

    /**
     * @return the min login name length
     */
    public int getMinUserLength() {
        return RhnConfigurationFactory.getSingleton().getInteger(RhnConfiguration.KEYS.USER_LOGIN_LENGTH_MIN);
    }

    /**
     * @return the max email length
     */
    public int getMaxEmailLength() {
        return RhnConfigurationFactory.getSingleton().getInteger(RhnConfiguration.KEYS.USER_EMAIL_LENGTH_MAX);
    }
}

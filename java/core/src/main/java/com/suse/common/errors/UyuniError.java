/*
 * Copyright (c) 2025 SUSE LLC
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

package com.suse.common.errors;

import java.io.Serializable;

/**
 * Represents a generic error in scope of Uyuni: a category, the field it applies to (optional) and a message.
 * The message is used as it is: translate it when creating the error if it must be localized.
 */
public class UyuniError implements Serializable {
    private final UyuniErrorCategory category;
    private final String argumentKey;
    private final String message;

    /**
     * Constructor for an error without category (UNKNOWN) or field
     * @param messageIn the error message
     */
    public UyuniError(String messageIn) {
        this(UyuniErrorCategory.UNKNOWN, null, messageIn);
    }

    /**
     * Constructor
     * @param categoryIn the kind of error
     * @param argumentKeyIn the field or object the error applies to, e.g. "serverId"; can be null
     * @param messageIn the error message
     */
    public UyuniError(UyuniErrorCategory categoryIn, String argumentKeyIn, String messageIn) {
        this.category = categoryIn;
        this.argumentKey = argumentKeyIn;
        this.message = messageIn;
    }

    public UyuniErrorCategory getCategory() {
        return category;
    }

    public String getArgumentKey() {
        return argumentKey;
    }

    public String getMessage() {
        return message;
    }
}

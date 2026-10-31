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
 */

package com.suse.manager.hub;

import java.io.IOException;

public class InvalidResponseException extends IOException {

    /**
     * Creates a new instance with the given error message
     * @param message the error message
     */
    public InvalidResponseException(String message) {
        super(message);
    }

    /**
     * Creates a new instance with the given error message and cause
     * @param message the error message
     * @param cause what cause this invalid response exception
     */
    public InvalidResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}

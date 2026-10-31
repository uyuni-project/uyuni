/*
 * Copyright (c) 2024 SUSE LLC
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

package com.suse.manager.webui.utils.token;

/**
 * An exception happening during the generation of a token
 */
public class TokenBuildingException extends TokenException {

    /**
     * Builds an instance with the given message
     * @param message the message
     */
    public TokenBuildingException(String message) {
        super(message);
    }

    /**
     * Builds an instance with the given cause and message
     * @param message the message
     * @param cause what caused this exception
     */
    public TokenBuildingException(String message, Exception cause) {
        super(message, cause);
    }
}

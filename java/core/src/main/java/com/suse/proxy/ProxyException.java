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
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.suse.proxy;

public class ProxyException extends RuntimeException {

    /**
     * @param messageIn exception message
     */
    public ProxyException(String messageIn) {
        super(messageIn);
    }

    /**
     * @param causeIn cause
     */
    public ProxyException(Throwable causeIn) {
        super(causeIn);
    }

    /**
     * @param messageIn exception message
     * @param causeIn cause
     */
    public ProxyException(String messageIn, Throwable causeIn) {
        super(messageIn, causeIn);
    }
}

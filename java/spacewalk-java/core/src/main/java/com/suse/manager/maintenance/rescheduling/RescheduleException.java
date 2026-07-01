/*
 * Copyright (c) 2020 SUSE LLC
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
package com.suse.manager.maintenance.rescheduling;


@SuppressWarnings("serial")
/**
 * {@inheritDoc}
 */
public class RescheduleException extends Exception {

    /**
     * Constructor
     */
    public RescheduleException() {
        super();
    }

    /**
     * Constructor
     * @param message the message
     * @param cause the cause
     * @param enableSuppression enable suppression
     * @param writableStackTrace writeable stacktrace
     */
    public RescheduleException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * Constructor
     * @param message the message
     * @param cause the cause
     */
    public RescheduleException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor
     * @param message the message
     */
    public RescheduleException(String message) {
        super(message);
    }

    /**
     * Constructor
     * @param cause the cause
     */
    public RescheduleException(Throwable cause) {
        super(cause);
    }
}

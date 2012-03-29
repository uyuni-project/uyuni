/**
 * Copyright (c) 2011 Novell
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

package com.redhat.rhn.common.logging;

/**
 * Special implementation of {@link RuntimeException} to indicate problems with
 * the audit logging component.
 */
public class AuditLogException extends RuntimeException {

    /** Serial Version UID */
    private static final long serialVersionUID = -1642618909441547963L;

    /**
     * Constructor that takes a message only.
     * @param msg message
     */
    public AuditLogException(String msg) {
        super(msg);
    }

    /**
     * Constructor that takes a {@link Throwable} only.
     * @param t throwable
     */
    public AuditLogException(Throwable t) {
        super(t);
    }

    /**
     * Constructor that takes a message and a {@link Throwable}.
     * @param msg message
     * @param t throwable
     */
    public AuditLogException(String msg, Throwable t) {
        super(msg, t);
    }
}

/**
 * Copyright (c) 2009--2014 Red Hat, Inc.
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

package com.redhat.rhn.common.messaging;

/**
 * A interface representing all messages that can be sent through the
 * messaging system,
 *
 * @version $Rev$
 */
public interface EventMessage {

    /**
     * Convert the internal representation to a text string
     * @return String representation of EventMessage.
     */
    String toText();

    /**
     * Get the user that scheduled the Event (needed to initialize logging)
     * @return the user that scheduled the Event
     */
    Long getUserId();
}



/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.frontend.xmlrpc;

import com.redhat.rhn.FaultException;

/**
 * Content sync failed.
 */
public class ContentSyncException extends FaultException {

    /**
     * Constructor
     */
    public ContentSyncException() {
        super(11003, "contentSync", "content_sync");
    }

    /**
     * Constructor
     *
     * @param message exception message
     */
    public ContentSyncException(String message) {
        super(11003, "content_sync", message);
    }

    /**
     * Constructor
     *
     * @param cause the cause (which is saved for later retrieval
     *              by the Throwable.getCause() method). (A null value is
     *              permitted, and indicates that the cause is nonexistent or
     *              unknown.)
     */
    public ContentSyncException(Throwable cause) {
        super(11003, "contentSync", "content_sync", cause);
    }

}

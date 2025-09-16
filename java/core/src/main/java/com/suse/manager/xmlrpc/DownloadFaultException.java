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
package com.suse.manager.xmlrpc;

import com.redhat.rhn.FaultException;


@SuppressWarnings("serial")
public class DownloadFaultException extends FaultException {

    /**
     * Constructor
     *
     * @param d exception
     */
    public DownloadFaultException(Throwable d) {
        super(2803, "downloadError", "Download failed", d);
    }

    /**
     * Constructor
     *
     * @param url the url
     * @param d exception
     */
    public DownloadFaultException(String url, Throwable d) {
        super(2803, "downloadError", String.format("Download from '%s' failed", url), d);
    }
}

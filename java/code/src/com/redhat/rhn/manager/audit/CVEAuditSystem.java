/*
 * Copyright (c) 2013 SUSE LLC
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
package com.redhat.rhn.manager.audit;

import java.util.Iterator;
import java.util.Set;

/**
 * base interface for audit results
 */
public interface CVEAuditSystem {

    /**
     * Return the ID.
     * @return the ID
     */
    long getId();

    /**
     * Return the name.
     * @return the name
     */
    String getName();

    /**
     * Return the patch status.
     * @return the patchStatus
     */
    PatchStatus getPatchStatus();

    /**
     * Return the set of channels.
     * @return the channels
     */
    Set<AuditChannelInfo> getChannels();

    /**
     * Return the set of erratas.
     * @return the erratas
     */
    Set<ErrataIdAdvisoryPair> getErratas();

    /**
     * Returns if the system was scanned with OVAL instead of Channels
     *
     * @return {@code True} of server was scanned with OVAL and {@code False} otherwise.
     */
    boolean isScannedWithOVAL();

    /**
     * Return the closest channel as {@link String} for CSV file download.
     * @return closest channel name
     */
    default String getChannelName() {
        String ret = "";
        Iterator<AuditChannelInfo> it = getChannels().iterator();
        if (it.hasNext()) {
            AuditChannelInfo c = it.next();
            ret = c.getName();
        }
        return ret;
    }

    /**
     * Return the closest patch as {@link String} for CSV file download.
     * @return closest patch advisory
     */
    default String getPatchAdvisory() {
        String ret = "";
        Iterator<ErrataIdAdvisoryPair> it = getErratas().iterator();
        if (it.hasNext()) {
            ErrataIdAdvisoryPair e = it.next();
            ret = e.getAdvisory();
        }
        return ret;
    }
}

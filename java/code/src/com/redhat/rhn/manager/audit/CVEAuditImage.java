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

import java.util.Set;

/**
 * Class representing a record with data about a single system containing that system's
 * patch status regarding a certain given CVE identifier as well as sets of relevant
 * channels and erratas.
 *
 */
public class CVEAuditImage implements CVEAuditSystem {

    private long id;
    private String name;
    private PatchStatus patchStatus;
    // If server was scanned wth CVEAuditManager#doAuditSystem instead of CVEAuditManagerOVAL#doAuditSystem then
    // it's possible to get false negatives.
    private boolean scannedWithOVAL;

    private Set<AuditChannelInfo> channels;
    private Set<ErrataIdAdvisoryPair> erratas;

    /**
     * Constructor
     * @param idIn id
     * @param nameIn name
     * @param statusIn status
     * @param channelsIn channels
     * @param erratasIn errata
     */
    public CVEAuditImage(long idIn, String nameIn, PatchStatus statusIn,
                         Set<AuditChannelInfo> channelsIn,
                         Set<ErrataIdAdvisoryPair> erratasIn) {
        this.id = idIn;
        this.name = nameIn;
        this.patchStatus = statusIn;
        this.channels = channelsIn;
        this.erratas = erratasIn;
    }

    /**
     * Return the system ID.
     * @return the id
     */
    @Override
    public long getId() {
        return id;
    }

    /**
     * Return the system name.
     * @return the name
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Return the patch status.
     * @return the patchStatus
     */
    @Override
    public PatchStatus getPatchStatus() {
        return patchStatus;
    }

    /**
     * Return the set of channels.
     * @return the channels
     */
    @Override
    public Set<AuditChannelInfo> getChannels() {
        return channels;
    }

    /**
     * Return the set of erratas.
     * @return the erratas
     */
    @Override
    public Set<ErrataIdAdvisoryPair> getErratas() {
        return erratas;
    }

    @Override
    public boolean isScannedWithOVAL() {
        return scannedWithOVAL;
    }

}

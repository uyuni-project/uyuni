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
import java.util.LinkedHashSet;
import java.util.Set;


/**
 * Class representing a record with data about a single system containing that system's
 * patch status regarding a certain given CVE identifier as well as sets of relevant
 * channels and erratas.
 *
 */
public class CVEAuditSystemBuilder {

    private long systemID;
    private String systemName;
    private PatchStatus patchStatus;
    // If system was audited wth CVEAuditManager#doAuditSystem instead of CVEAuditManagerOVAL#doAuditSystem then
    // it's possible to get false negatives.
    private boolean scannedWithOVAL;

    // LinkedHashSet is used to preserve insertion order when iterating
    private Set<AuditChannelInfo> channels =
            new LinkedHashSet<>();
    private Set<ErrataIdAdvisoryPair> erratas = new LinkedHashSet<>();

    /**
     * Constructor expecting a system ID.
     *
     * @param systemIDIn the system ID
     */
    public CVEAuditSystemBuilder(long systemIDIn) {
        setSystemID(systemIDIn);
    }

    /**
     * Return the system ID.
     * @return the systemID
     */
    public long getSystemID() {
        return systemID;
    }

    /**
     * Set the system ID.
     * @param systemIDIn the systemID to set
     */
    public void setSystemID(long systemIDIn) {
        this.systemID = systemIDIn;
    }

    /**
     * Return the system name.
     * @return the systemName
     */
    public String getSystemName() {
        return systemName;
    }

    /**
     * Set the system name.
     * @param systemNameIn the systemName to set
     */
    public void setSystemName(String systemNameIn) {
        this.systemName = systemNameIn;
    }

    /**
     * Return the patch status.
     * @return the patchStatus
     */
    public PatchStatus getPatchStatus() {
        return patchStatus;
    }

    /**
     * Returns a number that can be used to sort by patch status.
     * @return the patch status ranks
     */
    public int getPatchStatusRank() {
        return patchStatus.ordinal();
    }

    /**
     * Set the patch status.
     * @param patchStatusIn the patchStatus to set
     */
    public void setPatchStatus(PatchStatus patchStatusIn) {
        this.patchStatus = patchStatusIn;
    }

    /**
     * Return the set of channels.
     * @return the channels
     */
    public Set<AuditChannelInfo> getChannels() {
        return channels;
    }

    /**
     * Add a single channel.
     * @param channelIn a channel
     */
    public void addChannel(AuditChannelInfo channelIn) {
        this.channels.add(channelIn);
    }

    /**
     * Set the channels
     * @param channelSetIn the channels
     */
    public void setChannels(Set<AuditChannelInfo> channelSetIn) {
        this.channels = channelSetIn;
    }

    /**
     * Return the set of erratas.
     * @return the erratas
     */
    public Set<ErrataIdAdvisoryPair> getErratas() {
        return erratas;
    }

    /**
     * Add a single errata.
     * @param errataIn an errata
     */
    public void addErrata(ErrataIdAdvisoryPair errataIn) {
        this.erratas.add(errataIn);
    }

    /**
     * Set the erratas
     * @param errataSetIn the erratas
     */
    public void setErratas(Set<ErrataIdAdvisoryPair> errataSetIn) {
        this.erratas = errataSetIn;
    }

    /**
     * Return the closest channel as {@link String} for CSV file download.
     * @return closest channel name
     */
    public String getChannelName() {
        String ret = "";
        Iterator<AuditChannelInfo> it = channels.iterator();
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
    public String getPatchAdvisory() {
        String ret = "";
        Iterator<ErrataIdAdvisoryPair> it = erratas.iterator();
        if (it.hasNext()) {
            ErrataIdAdvisoryPair e = it.next();
            ret = e.getAdvisory();
        }
        return ret;
    }

    /** {@inheritDoc} */
    public Long getId() {
        return systemID;
    }

    /**
     * Returns {@code True} if server was scanned with OVAL and {@code False} otherwise
     * */
    public boolean isScannedWithOVAL() {
        return scannedWithOVAL;
    }

    /**
     * Sets scannedWithOVAL
     * @param scannedWithOVALIn the value to set
     * */
    public void setScannedWithOVAL(boolean scannedWithOVALIn) {
        this.scannedWithOVAL = scannedWithOVALIn;
    }
}

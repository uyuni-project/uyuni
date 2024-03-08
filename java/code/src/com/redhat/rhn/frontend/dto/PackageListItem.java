/*
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
package com.redhat.rhn.frontend.dto;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.manager.rhnpackage.PackageManager;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * PackageListItem
 */
public class PackageListItem extends IdComboDto {
    private List elabIdCombo;
    private Long id;
    private String nvre;
    private List elabNvre;
    private String patchType;
    private String name;
    private String version;
    private String release;
    private String epoch;
    private String timestamp;
    private String actionStatus;
    private Long packageId;
    private Long nameId;
    private Long evrId;
    private Long archId;
    private String packageType;
    private String path;
    private String arch;
    private List channelName;
    private List channelId;
    private String evr;
    private String evra;
    private String summary;
    private String nvrea;
    private Date installTime;
    private String locked;
    private String pending; // Either PackageManager.PKG_PENDING_LOCK for "to be locked"
                            // or PackageManager.PKG_PENDING_UNLOCK for "to be unlocked"
    private boolean retracted;

    private boolean ptf;

    private boolean partOfPtf;

    private boolean selectable;

    private String stream;

    /**
     * Default constructor
     */
    public PackageListItem() {
        // Assuming package is free to operate by default.
        this.locked = null;
        // Assuming the package can be always selectable
        this.selectable = true;
    }

    /**
     * Set locked status.
     *
     * @param lockedIn Not null for unlocked and null for locked. Usually "Y".
     */
    public void setLocked(String lockedIn) {
        this.locked = lockedIn;
    }

    /**
     * Get the status that the package is locked on the client machine or is in the process.
     *
     * @return Returns not null if package is locked and null otherwise.
     */
    public String getLocked() {
        return this.locked;
    }

    /**
     * Set locked status. Either value of PackageManager.PKG_PENDING_LOCK for
     * "to be locked" or value of PackageManager.PKG_PENDING_UNLOCK for "to be
     * unlocked".
     *
     * @param lockedStatus "L" or "U" for pending status direction.
     * @throws java.lang.Exception in case of an unknown lock status
     */
    public void setPending(String lockedStatus) throws Exception {
        if (lockedStatus != null && lockedStatus.isEmpty()) {
            lockedStatus = null;
        }

        if (lockedStatus != null &&
            (!lockedStatus.equals(PackageManager.PKG_PENDING_LOCK) &&
             (!lockedStatus.equals(PackageManager.PKG_PENDING_UNLOCK)))) {
            throw new Exception(String.format("Unknown lock status: %s", lockedStatus));
        }

        this.pending = lockedStatus;
    }

    /**
     * Returns locked status of the package. Either value of
     * PackageManager.PKG_PENDING_LOCK for "to be locked" or value of
     * PackageManager.PKG_PENDING_UNLOCK for "to be unlocked".
     *
     * @return String one character value of locked status.
     */
    public String getPending() {
        return this.pending;
    }

    /**
     * Gets the retracted.
     *
     * @return retracted
     */
    public boolean isRetracted() {
        return retracted;
    }

    /**
     * Sets the retracted.
     *
     * @param retractedIn the retracted
     */
    public void setRetracted(boolean retractedIn) {
        retracted = retractedIn;
    }

    /**
     * Check if the package is the main one of a ptf
     * @return true if the package is a ptf
     */
    public boolean isMasterPtfPackage() {
        return ptf;
    }

    /**
     * Sets if the package is the main one of a ptf
     * @param ptfIn true if the package is  a ptf
     */
    public void setMasterPtfPackage(boolean ptfIn) {
        this.ptf = ptfIn;
    }

    /**
     * Check if the package is part of a ptf
     * @return true if the package is part of a ptf
     */
    public boolean isPartOfPtf() {
        return partOfPtf;
    }

    /**
     * Sets if the package is part of a ptf
     * @param partOfPtfIn true if the package is part of a ptf
     */
    public void setPartOfPtf(boolean partOfPtfIn) {
        this.partOfPtf = partOfPtfIn;
    }

    /**
     * @return Returns the arch.
     */
    public String getArch() {
        return arch;
    }
    /**
     * @param archIn The arch to set.
     */
    public void setArch(String archIn) {
        this.arch = archIn;
    }
    /**
     * @return Returns the channelId.
     */
    public List getChannelId() {
        return channelId;
    }
    /**
     * @param channelIdIn The channelId to set.
     */
    public void setChannelId(List channelIdIn) {
        this.channelId = channelIdIn;
    }
    /**
     * @return Returns the channelName.
     */
    public List getChannelName() {
        return channelName;
    }
    /**
     * @param channelNameIn The channelName to set.
     */
    public void setChannelName(List channelNameIn) {
        this.channelName = channelNameIn;
    }
    /**
     * @return Returns the path.
     */
    public String getPath() {
        return path;
    }
    /**
     * @param pathIn The path to set.
     */
    public void setPath(String pathIn) {
        this.path = pathIn;
    }
    /**
     * @return Returns the epoch.
     */
    public String getEpoch() {
        return epoch;
    }
    /**
     * @param epochIn The epoch to set.
     */
    public void setEpoch(String epochIn) {
        epoch = epochIn;
    }
    /**
     *
     * @return Returns the packageId
     */
    public Long getPackageId() {
        return packageId;
    }
    /**
     *
     * @param packageIdIn The packageId to set
     */
    public void setPackageId(Long packageIdIn) {
        packageId = packageIdIn;
    }

    /**
     * @return Returns the Id.
     */
    @Override
    public Long getId() {
        return id;
    }
    /**
     * @param idIn The Id to set.
     */
    public void setId(Long idIn) {
        id = idIn;
    }
    /**
     * @return Returns the evrId.
     */
    public Long getEvrId() {
        return evrId;
    }
    /**
     * @param evrIdIn The evrId to set.
     */
    public void setEvrId(Long evrIdIn) {
        evrId = evrIdIn;
    }
    /**
     * @return Returns the archId.
     */
    public Long getArchId() {
        return archId;
    }
    /**
     * @param archIdIn The archId to set.
     */
    public void setArchId(Long archIdIn) {
        archId = archIdIn;
    }
    /**
     * @return Returns the elab idCombo.
     */
    public List getElabIdCombo() {
        return elabIdCombo;
    }
    /**
     * @param elabIdComboIn The elabIdCombo to set.
     */
    public void setElabIdCombo(List elabIdComboIn) {
        elabIdCombo = elabIdComboIn;
    }
    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }
    /**
     * @param nameIn The name to set.
     */
    public void setName(String nameIn) {
        name = nameIn;
    }
    /**
     * @return Returns the nameId.
     */
    public Long getNameId() {
        return nameId;
    }
    /**
     * @param nameIdIn The nameId to set.
     */
    public void setNameId(Long nameIdIn) {
        nameId = nameIdIn;
    }
    /**
     * @return Returns the nvre.
     */
    public String getNvre() {
        return nvre;
    }
    /**
     * @param nvreIn The nvre to set.
     */
    public void setNvre(String nvreIn) {
        nvre = nvreIn;
    }
    /**
     * @return Returns the elab nvre.
     */
    public List getElabNvre() {
        return elabNvre;
    }
    /**
     * @param elabNvreIn The elab nvre to set.
     */
    public void setElabNvre(List elabNvreIn) {
        elabNvre = elabNvreIn;
    }
    /**
     * @return Returns the release.
     */
    public String getRelease() {
        return release;
    }
    /**
     * @param releaseIn The release to set.
     */
    public void setRelease(String releaseIn) {
        release = releaseIn;
    }
    /**
     * @return Returns the version.
     */
    public String getVersion() {
        return version;
    }
    /**
     * @param versionIn The version to set.
     */
    public void setVersion(String versionIn) {
        version = versionIn;
    }
    /**
     * Returns the channel name and id instance variables as
     * a list of HashMaps all in a single convenient Object
     * @return list of HashMaps with name and id keys
     */
    public List<Map<String, Object>> getChannels() {
        List<Map<String, Object>> retval = new ArrayList<>();
        for (int i = 0; i < channelId.size(); i++) {
            Map<String, Object> current = new HashMap<>();
            current.put("id", channelId.get(i));
            current.put("name", channelName.get(i));
            retval.add(current);
        }
        return retval;
    }
    /**
     * @return Returns the patch type.
     */
    public String getPatchType() {
        return patchType;
    }
    /**
     * @param patchTypeIn The version to set.
     */
    public void setPatchType(String patchTypeIn) {
        patchType = patchTypeIn;
    }
    /**
     * @return Returns the timestamp for patch installs.
     */
    public String getTimestamp() {
        if (timestamp == null) {
            timestamp = LocalizationService
                        .getInstance()
                        .getMessage("patches.installed.notavailable");
        }

        return timestamp;
    }
    /**
     * @param timeStampIn The version to set.
     */
    public void setTimestamp(String timeStampIn) {
        timestamp = timeStampIn;
    }
    /**
     * @return Returns the timestamp for patch installs.
     */
    public String getActionStatus() {
        if (actionStatus == null) {
            actionStatus = "Not Available";
        }
        return actionStatus;
    }
    /**
     * @param actionStatusIn The status to set.
     */
    public void setActionStatus(String actionStatusIn) {
        actionStatus = actionStatusIn;
    }

    /**
     * @return Returns the evr.
     */
    public String getEvr() {
        return evr;
    }
    /**
     * @param evrIn The evr to set.
     */
    public void setEvr(String evrIn) {
        this.evr = evrIn;
    }
    /**
     * @return Returns the evra.
     */
    public String getEvra() {
        return evra;
    }
    /**
     * @param evraIn The evra to set.
     */
    public void setEvra(String evraIn) {
        this.evra = evraIn;
    }
    /**
     * @return Returns the summary.
     */
    public String getSummary() {
        return summary;
    }
    /**
     * @param aSummary The summary to set.
     */
    public void setSummary(String aSummary) {
        this.summary = aSummary;
    }
    /**
     * @return Returns the nvrea.
     */
    public String getNvrea() {
        return nvrea;
    }
    /**
     * @param aNvrea The nvrea to set.
     */
    public void setNvrea(String aNvrea) {
        this.nvrea = aNvrea;
    }

    /**
     * @return Returns The package type.
     */
    public String getPackageType() {
        return packageType;
    }

    /**
     * @param packageTypeIn The package type to set.
     */
    public void setPackageType(String packageTypeIn) {
        this.packageType = packageTypeIn;
    }

    /**
     * Getter for installTime
     * @return String when package was installed (as reported by rpm database).
    */
    public String getInstallTime() {
        if (installTime == null) {
            return "";
        }
        return LocalizationService.getInstance().formatDate(installTime);
    }

    /**
     * Getter for the Date object
     * @return installTime Date object
     */
    public Date getInstallTimeObj() {
        return installTime;
    }

    /**
     * Setter for installTime
     * @param installTimeIn to set
    */
    public void setInstalltime(Date installTimeIn) {
        installTime = installTimeIn;
    }

    /**
     * Specifies if this package is selectable
     * @return true if it can be selected
     */
    @Override
    public boolean isSelectable() {
        return selectable;
    }

    /**
     * Sets the selectable status for this package
     * @param selectableIn true to allow the package to be selected
     */
    public void setSelectable(boolean selectableIn) {
        this.selectable = selectableIn;
    }

    /**
     * Gets the stream for this package.
     *
     * @return The stream this package is associated with.
     */
    public String getStream() {
        return stream;
    }

    /**
     * Sets the stream for this package.
     *
     * @param streamIn The Appstream to set for this package.
     */
    public void setStream(String streamIn) {
        stream = streamIn;
    }

    /**
     * Get a string representation of NEVR:
     *
     * virt-manager-0.2.6-7.0.2.el5
     *
     * @return String representation of package's NEVR
     */
    public String getNevr() {
        String e = (this.getEpoch() != null) ? this.getEpoch() : "0";
        String v = (this.getVersion() != null) ? this.getVersion() : "0";
        String r = (this.getRelease() != null) ? this.getRelease() : "0";
        return this.getName() + "-" + e + ":" + v + "-" + r;
    }

    /**
     * Get a string representation of NEVRA:
     *
     * @return String representation of package's NEVRA
     */
    public String getNevra() {
        String e = (this.getEpoch() != null) ? this.getEpoch() : "0";
        String v = (this.getVersion() != null) ? this.getVersion() : "0";
        String r = (this.getRelease() != null) ? this.getRelease() : "0";
        String a = (this.getArch() != null) ? this.getArch() : "0";
        return this.getName() + "-" + e + ":" + v + "-" + r + "." + a;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSelectionKey() {
        if (getNvrea() != null) {
            return getIdCombo() + "~*~" + getNvrea();
        }
        return getIdCombo() + "~*~" + getNvre();
    }

    /**
     * Returns a map of the keys used in this Package List .
     * @return a map.
     */
    public Map<String, Long> getKeyMap() {
        Map<String, Long> ret = new HashMap<>();
        ret.put("name_id", getIdOne());
        ret.put("evr_id", getIdTwo());
        ret.put("arch_id", getIdThree());
        return ret;
    }

    /**
     * Returns a unique id (nameId x archId) for HashMap
     * @return a map id
     */
    public String getMapHash() {
        return "" + getNameId() + "|" + getArchId();
    }

    /**
     * Returns a list of Key map representation for a given list of package  items
     * @param items the list of package items to be converted.
     * @return the list of key maps associated with the given items
     */
    public static  List<Map<String, Long>> toKeyMaps(List<PackageListItem> items) {
        List<Map<String, Long>> ret = new LinkedList<>();
        for (PackageListItem item : items) {
            ret.add(item.getKeyMap());
        }
        return ret;
    }

    /**
     * Basically constructs a PackageListItem from a selection key.
     * @param key the select key string containing other metadata
     * @return the constructed PackageListItem.
     */
    public static PackageListItem parse(String key) {
        String [] row = key.split("\\~\\*\\~");
        PackageListItem item  = new PackageListItem();
        item.setIdCombo(row[0]);
        if (row.length > 1) {
            item.setNvre(row[1]);
        }
        return item;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this);

        if (this.getIdCombo() != null) {
            builder.append("idCombo", this.getIdCombo());
        }
        if (this.getId() != null) {
            builder.append("id", this.getId());
        }
        if (this.getNvre() != null) {
            builder.append("nvre", this.getNvre());
        }
        if (this.getPatchType() != null) {
            builder.append("patchType", this.getPatchType());
        }
        if (this.getName() != null) {
            builder.append("name", this.getName());
        }
        if (this.getVersion() != null) {
            builder.append("version", this.getVersion());
        }
        if (this.getRelease() != null) {
            builder.append("release", this.getRelease());
        }
        if (this.getEpoch() != null) {
            builder.append("epoch", this.getEpoch());
        }
        if (this.getArch() != null) {
            builder.append("arch", this.getArch());
        }
        if (this.getTimestamp() != null) {
            builder.append("timestamp", this.getTimestamp());
        }
        if (this.getActionStatus() != null) {
            builder.append("actionStatus", this.getActionStatus());
        }
        if (this.getTimestamp() != null) {
            builder.append("timestamp", this.getTimestamp());
        }
        if (this.getPackageId() != null) {
            builder.append("packageId", this.getPackageId());
        }
        if (this.getNameId() != null) {
            builder.append("nameId", this.getNameId());
        }
        if (this.getEvrId() != null) {
            builder.append("evrId", this.getEvrId());
        }
        if (this.getArchId() != null) {
            builder.append("archId", this.getArchId());
        }
        if (this.getPath() != null) {
            builder.append("path", this.getPath());
        }
        if (this.getEvr() != null) {
            builder.append("evr", this.getEvr());
        }
        if (this.getEvra() != null) {
            builder.append("evra", this.getEvra());
        }
        if (this.getSummary() != null) {
            builder.append("summary", this.getSummary());
        }
        if (this.getNvrea() != null) {
            builder.append("nvrea", this.getNvrea());
        }
        return builder.toString();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PackageListItem)) {
            return false;
        }

        PackageListItem that = (PackageListItem) o;

        return !Objects.equals(idCombo, that.idCombo);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return idCombo != null ? idCombo.hashCode() : 0;
    }
}

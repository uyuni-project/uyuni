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

package com.suse.manager.model.hub;

import com.redhat.rhn.domain.channel.Channel;

import java.util.Date;
import java.util.Objects;

public class ModifyCustomChannelInfoJson {

    private final String label;

    private Long peripheralOrgId;
    private String originalChannelLabel;

    private String baseDir;
    private String name;
    private String summary;
    private String description;
    private String productNameLabel;
    private Boolean gpgCheck;
    private String gpgKeyUrl;
    private String gpgKeyId;
    private String gpgKeyFp;
    private Long endOfLifeDate;

    private String channelProductProduct;
    private String channelProductVersion;
    private String channelAccess;
    private String maintainerName;
    private String maintainerEmail;
    private String maintainerPhone;
    private String supportPolicy;
    private String updateTag;
    private Boolean installerUpdates;

    /**
     * Constructor
     *
     * @param labelIn The channel label
     */
    public ModifyCustomChannelInfoJson(String labelIn) {
        label = labelIn;

        gpgCheck = true;
        channelAccess = Channel.PRIVATE;
        installerUpdates = false;
        originalChannelLabel = null;
    }

    private Date longToDate(Long longIn) {
        if (null == longIn) {
            return new Date();
        }
        else {
            return new Date(longIn);
        }
    }

    private Long dateToLong(Date dateIn) {
        if (null != dateIn) {
            return dateIn.getTime();
        }

        return 0L;
    }

    /**
     * @return Returns the label.
     */
    public String getLabel() {
        return label;
    }

    /**
     * @return Returns the peripheral org id
     */
    public Long getPeripheralOrgId() {
        return peripheralOrgId;
    }

    /**
     * @param o The peripheral org id to set.
     */
    public void setPeripheralOrgId(Long o) {
        this.peripheralOrgId = o;
    }

    /**
     * @return Returns the original channel label if cloned.
     */
    public String getOriginalChannelLabel() {
        return originalChannelLabel;
    }

    /**
     * @param originalChannelLabelIn The he original channel label if cloned.
     */
    public void setOriginalChannelLabel(String originalChannelLabelIn) {
        originalChannelLabel = originalChannelLabelIn;
    }

    /**
     * @return Returns the baseDir.
     */
    public String getBaseDir() {
        return baseDir;
    }

    /**
     * @param b The baseDir to set.
     */
    public void setBaseDir(String b) {
        this.baseDir = b;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param n The name to set.
     */
    public void setName(String n) {
        this.name = n;
    }

    /**
     * @return Returns the summary.
     */
    public String getSummary() {
        return summary;
    }

    /**
     * @param s The summary to set.
     */
    public void setSummary(String s) {
        this.summary = s;
    }

    /**
     * @return Returns the description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param d The description to set.
     */
    public void setDescription(String d) {
        this.description = d;
    }

    /**
     * @return the productName id
     */
    public String getProductNameLabel() {
        return productNameLabel;
    }

    /**
     * @param p the productName idto set
     */
    public void setProductNameLabel(String p) {
        this.productNameLabel = p;
    }

    /**
     * @return the GPGCheck
     */
    public Boolean isGpgCheck() {
        return gpgCheck;
    }

    /**
     * @param gpgCheckIn the GPGCheck to set
     */
    public void setGpgCheck(Boolean gpgCheckIn) {
        this.gpgCheck = gpgCheckIn;
    }

    /**
     * @return Returns the gPGKeyUrl.
     */
    public String getGpgKeyUrl() {
        return gpgKeyUrl;
    }

    /**
     * @param k The gPGKeyUrl to set.
     */
    public void setGpgKeyUrl(String k) {
        gpgKeyUrl = k;
    }

    /**
     * @return Returns the gPGKeyId.
     */
    public String getGpgKeyId() {
        return gpgKeyId;
    }

    /**
     * @param k The gPGKeyId to set.
     */
    public void setGpgKeyId(String k) {
        gpgKeyId = k;
    }

    /**
     * @return Returns the gPGKeyFp.
     */
    public String getGpgKeyFp() {
        return gpgKeyFp;
    }

    /**
     * @param k The gPGKeyFP to set.
     */
    public void setGpgKeyFp(String k) {
        gpgKeyFp = k;
    }


    /**
     * @return Returns the endOfLife.
     */
    public Date getEndOfLifeDate() {
        return longToDate(endOfLifeDate);
    }

    /**
     * @param endOfLifeDateIn The endOfLife to set.
     */
    public void setEndOfLifeDate(Date endOfLifeDateIn) {
        this.endOfLifeDate = dateToLong(endOfLifeDateIn);
    }

    /**
     * @return Returns the product name
     */
    public String getChannelProductProduct() {
        return channelProductProduct;
    }

    /**
     * @param channelProductProductIn The product name to set
     */
    public void setChannelProductProduct(String channelProductProductIn) {
        this.channelProductProduct = channelProductProductIn;
    }

    /**
     * @return Returns the product version
     */
    public String getChannelProductVersion() {
        return channelProductVersion;
    }

    /**
     * @param channelProductVersionIn The product version to set
     */
    public void setChannelProductVersion(String channelProductVersionIn) {
        this.channelProductVersion = channelProductVersionIn;
    }

    /**
     * @param acc public, protected, or private
     */
    public void setChannelAccess(String acc) {
        channelAccess = acc;
    }

    /**
     * @return public, protected, or private
     */
    public String getChannelAccess() {
        return channelAccess;
    }

    /**
     * @return maintainer's name
     */
    public String getMaintainerName() {
        return maintainerName;
    }

    /**
     * @param mname maintainer's name
     */
    public void setMaintainerName(String mname) {
        maintainerName = mname;
    }

    /**
     * @return maintainer's email
     */
    public String getMaintainerEmail() {
        return maintainerEmail;
    }

    /**
     * @param email maintainer's email
     */
    public void setMaintainerEmail(String email) {
        maintainerEmail = email;
    }

    /**
     * @return maintainer's phone number
     */
    public String getMaintainerPhone() {
        return maintainerPhone;
    }

    /**
     * @param phone maintainer's phone number (string)
     */
    public void setMaintainerPhone(String phone) {
        maintainerPhone = phone;
    }

    /**
     * @return channel's support policy
     */
    public String getSupportPolicy() {
        return supportPolicy;
    }

    /**
     * @param policy channel support policy
     */
    public void setSupportPolicy(String policy) {
        supportPolicy = policy;
    }

    /**
     * @return the updateTag
     */
    public String getUpdateTag() {
        return updateTag;
    }

    /**
     * @param updateTagIn the update tag
     */
    public void setUpdateTag(String updateTagIn) {
        updateTag = updateTagIn;
    }

    /**
     * @return Returns the installerUpdates.
     */
    public Boolean isInstallerUpdates() {
        return installerUpdates;
    }

    /**
     * @param installerUpdatesIn The installerUpdates to set.
     */
    public void setInstallerUpdates(Boolean installerUpdatesIn) {
        installerUpdates = installerUpdatesIn;
    }

    @Override
    public boolean equals(Object oIn) {
        if (oIn == null || getClass() != oIn.getClass()) {
            return false;
        }
        ModifyCustomChannelInfoJson that = (ModifyCustomChannelInfoJson) oIn;
        return Objects.equals(getLabel(), that.getLabel()) &&
                Objects.equals(getPeripheralOrgId(), that.getPeripheralOrgId()) &&
                Objects.equals(getOriginalChannelLabel(), that.getOriginalChannelLabel()) &&
                Objects.equals(getBaseDir(), that.getBaseDir()) &&
                Objects.equals(getName(), that.getName()) &&
                Objects.equals(getSummary(), that.getSummary()) &&
                Objects.equals(getDescription(), that.getDescription()) &&
                Objects.equals(getProductNameLabel(), that.getProductNameLabel()) &&
                Objects.equals(isGpgCheck(), that.isGpgCheck()) &&
                Objects.equals(getGpgKeyUrl(), that.getGpgKeyUrl()) &&
                Objects.equals(getGpgKeyId(), that.getGpgKeyId()) &&
                Objects.equals(getGpgKeyFp(), that.getGpgKeyFp()) &&
                Objects.equals(getEndOfLifeDate(), that.getEndOfLifeDate()) &&
                Objects.equals(getChannelProductProduct(), that.getChannelProductProduct()) &&
                Objects.equals(getChannelProductVersion(), that.getChannelProductVersion()) &&
                Objects.equals(getChannelAccess(), that.getChannelAccess()) &&
                Objects.equals(getMaintainerName(), that.getMaintainerName()) &&
                Objects.equals(getMaintainerEmail(), that.getMaintainerEmail()) &&
                Objects.equals(getMaintainerPhone(), that.getMaintainerPhone()) &&
                Objects.equals(getSupportPolicy(), that.getSupportPolicy()) &&
                Objects.equals(getUpdateTag(), that.getUpdateTag()) &&
                Objects.equals(isInstallerUpdates(), that.isInstallerUpdates());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLabel(), getPeripheralOrgId(), getOriginalChannelLabel(), getBaseDir(), getName(),
                getSummary(), getDescription(), getProductNameLabel(), isGpgCheck(), getGpgKeyUrl(), getGpgKeyId(),
                getGpgKeyFp(), getEndOfLifeDate(), getChannelProductProduct(), getChannelProductVersion(),
                getChannelAccess(), getMaintainerName(), getMaintainerEmail(), getMaintainerPhone(),
                getSupportPolicy(), getUpdateTag(), isInstallerUpdates());
    }

    protected String toStringCore() {
        final StringBuilder sb = new StringBuilder();
        sb.append("label='").append(label).append('\'');
        sb.append(", peripheralOrgId=").append(peripheralOrgId);
        sb.append(", originalChannelLabel='").append(originalChannelLabel).append('\'');
        sb.append(", baseDir='").append(baseDir).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", summary='").append(summary).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", productNameLabel='").append(productNameLabel).append('\'');
        sb.append(", gpgCheck=").append(gpgCheck);
        sb.append(", gpgKeyUrl='").append(gpgKeyUrl).append('\'');
        sb.append(", gpgKeyId='").append(gpgKeyId).append('\'');
        sb.append(", gpgKeyFp='").append(gpgKeyFp).append('\'');
        sb.append(", endOfLifeDate=").append(endOfLifeDate);
        sb.append(", channelProductProduct='").append(channelProductProduct).append('\'');
        sb.append(", channelProductVersion='").append(channelProductVersion).append('\'');
        sb.append(", channelAccess='").append(channelAccess).append('\'');
        sb.append(", maintainerName='").append(maintainerName).append('\'');
        sb.append(", maintainerEmail='").append(maintainerEmail).append('\'');
        sb.append(", maintainerPhone='").append(maintainerPhone).append('\'');
        sb.append(", supportPolicy='").append(supportPolicy).append('\'');
        sb.append(", updateTag='").append(updateTag).append('\'');
        sb.append(", installerUpdates=").append(installerUpdates);
        return sb.toString();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ModifyCustomChannelInfoJson{");
        sb.append(toStringCore());
        sb.append('}');
        return sb.toString();
    }
}

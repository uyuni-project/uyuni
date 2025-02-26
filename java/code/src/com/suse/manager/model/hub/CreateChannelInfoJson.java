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

import com.suse.scc.model.SCCRepositoryJson;

import java.util.Objects;

public class CreateChannelInfoJson extends ModifyChannelInfoJson {

    private String parentChannelLabel;
    private String channelArchLabel;
    private String checksumTypeLabel;
    private SCCRepositoryJson repositoryInfo;

    /**
     * Constructor
     *
     * @param labelIn The channel label
     */
    public CreateChannelInfoJson(String labelIn) {
        super(labelIn);
        repositoryInfo = new SCCRepositoryJson();
    }

    /**
     * @return Returns the parentChannel.
     */
    public String getParentChannelLabel() {
        return parentChannelLabel;
    }

    /**
     * @param p The parentChannel to set.
     */
    public void setParentChannelLabel(String p) {
        this.parentChannelLabel = p;
    }

    /**
     * @return Returns the channelArch id
     */
    public String getChannelArchLabel() {
        return channelArchLabel;
    }

    /**
     * @param c The channelArch label to set.
     */
    public void setChannelArchLabel(String c) {
        this.channelArchLabel = c;
    }

    /**
     * @return Returns the checksum type label
     */
    public String getChecksumTypeLabel() {
        return checksumTypeLabel;
    }

    /**
     * @param checksumTypeLabelIn The checksum type label to set.
     */
    public void setChecksumTypeLabel(String checksumTypeLabelIn) {
        this.checksumTypeLabel = checksumTypeLabelIn;
    }

    /**
     * @return SCCRepositoryJson object with repository info
     */
    public SCCRepositoryJson getRepositoryInfo() {
        return repositoryInfo;
    }

    /**
     * @param repositoryInfoIn The SCCRepositoryJson object with repository info
     */
    public void setRepositoryInfo(SCCRepositoryJson repositoryInfoIn) {
        repositoryInfo = repositoryInfoIn;
    }

    @Override
    public boolean equals(Object oIn) {
        if (oIn == null || getClass() != oIn.getClass()) {
            return false;
        }
        if (!super.equals(oIn)) {
            return false;
        }
        CreateChannelInfoJson that = (CreateChannelInfoJson) oIn;
        return Objects.equals(getParentChannelLabel(), that.getParentChannelLabel()) &&
                Objects.equals(getChannelArchLabel(), that.getChannelArchLabel()) &&
                Objects.equals(getChecksumTypeLabel(), that.getChecksumTypeLabel()) &&
                Objects.equals(getRepositoryInfo(), that.getRepositoryInfo());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getParentChannelLabel(), getChannelArchLabel(), getChecksumTypeLabel(),
                getRepositoryInfo());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CreateChannelInfoJson{");
        sb.append(toStringCore());
        sb.append("parentChannelLabel='").append(parentChannelLabel).append('\'');
        sb.append(", channelArchLabel='").append(channelArchLabel).append('\'');
        sb.append(", checksumTypeLabel='").append(checksumTypeLabel).append('\'');
        sb.append(", repositoryInfo=").append(repositoryInfo);
        sb.append('}');
        return sb.toString();
    }
}

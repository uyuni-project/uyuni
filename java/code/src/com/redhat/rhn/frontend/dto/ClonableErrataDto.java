/**
 * Copyright (c) 2009--2013 Red Hat, Inc.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ClonableErrataDto
 * @version $Rev$
 */
public class ClonableErrataDto extends BaseDto {

    protected Long id;
    protected String advisory;
    protected String advisoryType;
    protected String advisoryName;
    protected String synopsis;
    protected String updateDate;
    protected Boolean alreadyCloned;
    protected List channelId = new ArrayList();
    protected List channelName =  new ArrayList();
    protected boolean rebootSuggested;
    protected boolean restartSuggested;

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
     *
     * @return List of channel maps
     */
    public List<Object> getChannelMap() {
      List<Object> l = new ArrayList<Object>();
        for (int i = 0; i < channelId.size(); i++) {
          Map<String, Object> m = new HashMap<String, Object>();
          m.put("id", channelId.get(i));
          m.put("name", channelName.get(i));
          l.add(m);
        }
        return l;
      }

    /**
     * @param channelNameIn The channelName to set.
     */
    public void setChannelName(List channelNameIn) {
        this.channelName = channelNameIn;
    }

    /**
     * {@inheritDoc}
     */
    public Long getId() {
        return id;
    }

    /**
     * @return Returns the advisory.
     */
    public String getAdvisory() {
        return advisory;
    }

    /**
     * @param advisoryIn The advisory to set.
     */
    public void setAdvisory(String advisoryIn) {
        this.advisory = advisoryIn;
    }

    /**
     * @return Returns the advisoryName.
     */
    public String getAdvisoryName() {
        return advisoryName;
    }

    /**
     * @param advisoryNameIn The advisoryName to set.
     */
    public void setAdvisoryName(String advisoryNameIn) {
        this.advisoryName = advisoryNameIn;
    }

    /**
     * @return Returns the advisoryType.
     */
    public String getAdvisoryType() {
        return advisoryType;
    }

    /**
     * @param advisoryTypeIn The advisoryType to set.
     */
    public void setAdvisoryType(String advisoryTypeIn) {
        this.advisoryType = advisoryTypeIn;
    }

    /**
     * @return Returns the synopsis.
     */
    public String getSynopsis() {
        return synopsis;
    }

    /**
     * @param synopsisIn The synopsis to set.
     */
    public void setSynopsis(String synopsisIn) {
        this.synopsis = synopsisIn;
    }

    /**
     * @return Returns the updateDate.
     */
    public String getUpdateDate() {
        return updateDate;
    }

    /**
     * @param updateDateIn The updateDate to set.
     */
    public void setUpdateDate(String updateDateIn) {
        this.updateDate = updateDateIn;
    }

    /**
     * @param idIn The id to set.
     */
    public void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * @return Returns the setAlreadyCloned.
     */
    public Boolean getAlreadyCloned() {
        return alreadyCloned;
    }

    /**
     * For PostgreSQL we need to accept also Integer and cast it to Long.
     *
     * @param setAlreadyClonedIn The setAlreadyCloned to set.
     */
    public void setAlreadyCloned(Integer setAlreadyClonedIn) {
        setAlreadyCloned(Long.valueOf(setAlreadyClonedIn));
    }

    /**
     * @param setAlreadyClonedIn The setAlreadyCloned to set.
     */
    public void setAlreadyCloned(Long setAlreadyClonedIn) {
        if (setAlreadyClonedIn.equals(1L)) {
            this.alreadyCloned = Boolean.TRUE;
        }
        else {
            this.alreadyCloned = Boolean.FALSE;
        }
    }

    /**
     * Returns true if the advisory is a Product Enhancement.
     * @return true if the advisory is a Product Enhancement.
     */
    public boolean isProductEnhancement() {
        return "Product Enhancement Advisory".equals(getAdvisoryType());
    }

    /**
     * Returns true if the advisory is a Security Advisory.
     * @return true if the advisory is a Security Advisory.
     */
    public boolean isSecurityAdvisory() {
        return "Security Advisory".equals(getAdvisoryType());
    }

    /**
     * Returns true if the advisory is a Bug Fix.
     * @return true if the advisory is a Bug Fix.
     */
    public boolean isBugFix() {
        return "Bug Fix Advisory".equals(getAdvisoryType());
    }

    /**
     * Gets if {@code reboot_suggested} flag is set.
     *
     * @return True if {@code reboot_suggested} flag is set.
     */
    public boolean isRebootSuggested() {
        return rebootSuggested;
    }

    /**
     * Sets {@code reboot_suggested} flag.
     *
     * @param rebootSuggestedIn {@code reboot_suggested} flag
     */
    public void setRebootSuggested(boolean rebootSuggestedIn) {
        rebootSuggested = rebootSuggestedIn;
    }

    /**
     * Gets if {@code restart_suggested} flag is set.
     *
     * @return True if {@code restart_suggested} flag is set.
     */
    public boolean isRestartSuggested() {
        return restartSuggested;
    }

    /**
     * Sets {@code restart_suggested} flag.
     *
     * @param restartSuggestedIn {@code restart_suggested} flag
     */
    public void setRestartSuggested(boolean restartSuggestedIn) {
        this.restartSuggested = restartSuggestedIn;
    }
}

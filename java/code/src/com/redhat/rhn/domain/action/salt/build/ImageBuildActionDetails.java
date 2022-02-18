/*
 * Copyright (c) 2016 SUSE LLC
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
package com.redhat.rhn.domain.action.salt.build;

import com.redhat.rhn.domain.action.ActionChild;

import java.util.HashSet;
import java.util.Set;

/**
 * ApplyStatesActionDetails - Class representation of the table rhnActionApplyStates.
 */
public class ImageBuildActionDetails extends ActionChild {

    private Long id;
    private Long actionId;
    private String version;
    private Long imageProfileId;
    private Set<ImageBuildActionResult> results = new HashSet<>();

    /**
     * Gets version.
     *
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets version.
     *
     * @param versionIn the version
     */
    public void setVersion(String versionIn) {
        this.version = versionIn;
    }

    /**
     * Gets image profile id.
     *
     * @return the image profile id
     */
    public Long getImageProfileId() {
        return imageProfileId;
    }

    /**
     * Sets image profile id.
     *
     * @param imageProfileIdIn the image profile id
     */
    public void setImageProfileId(Long imageProfileIdIn) {
        this.imageProfileId = imageProfileIdIn;
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param idIn the id to set
     */
    public void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * @return the action id
     */
    public Long getActionId() {
        return actionId;
    }

    /**
     * @param actionIdIn the action id to set
     */
    public void setActionId(Long actionIdIn) {
        this.actionId = actionIdIn;
    }

    /**
     * @return the results
     */
    public Set<ImageBuildActionResult> getResults() {
        return results;
    }

    /**
     * @param resultsIn the results to set
     */
    public void setResults(Set<ImageBuildActionResult> resultsIn) {
        this.results = resultsIn;
    }

    /**
     * Add {@link com.redhat.rhn.domain.action.salt.ApplyStatesActionResult} to the results
     * set.
     *
     * @param resultIn ApplyStatesActionResult to add to the set
     */
    public void addResult(ImageBuildActionResult resultIn) {
        resultIn.setParentScriptActionDetails(this);
        results.add(resultIn);
    }
}

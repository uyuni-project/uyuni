/**
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
package com.redhat.rhn.domain.action.salt.inspect;

import com.redhat.rhn.domain.action.ActionChild;

import java.util.HashSet;
import java.util.Set;

/**
 * ImageInspectActionDetails - Class representation of the table rhnActionImageInspect.
 */
public class ImageInspectActionDetails extends ActionChild {

    private long id;
    private long actionId;
    private String name;
    private String version;
    private long imageStoreId;
    private Set<ImageInspectActionResult> results = new HashSet<>();
    private Long buildActionId;

    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param versionIn the version
     */
    public void setVersion(String versionIn) {
        this.version = versionIn;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param nameIn the name in
     */
    public void setName(String nameIn) {
        this.name = nameIn;
    }

    /**
     * @return the image store id
     */
    public long getImageStoreId() {
        return imageStoreId;
    }

    /**
     * @param imageStoreIdIn the image store id to set
     */
    public void setImageStoreId(long imageStoreIdIn) {
        this.imageStoreId = imageStoreIdIn;
    }

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @param idIn the id to set
     */
    public void setId(long idIn) {
        this.id = idIn;
    }

    /**
     * @return the action id
     */
    public long getActionId() {
        return actionId;
    }

    /**
     * @param actionIdIn the action id to set
     */
    public void setActionId(long actionIdIn) {
        this.actionId = actionIdIn;
    }

    /**
     * @return the build action id
     */
    public Long getBuildActionId() {
        return buildActionId;
    }

    /**
     * @param buildActionIdIn the action id to set
     */
    public void setBuildActionId(Long buildActionIdIn) {
        this.buildActionId = buildActionIdIn;
    }

    /**
     * @return the results
     */
    public Set<ImageInspectActionResult> getResults() {
        return results;
    }

    /**
     * @param resultsIn the results to set
     */
    public void setResults(Set<ImageInspectActionResult> resultsIn) {
        this.results = resultsIn;
    }

    /**
     * Add {@link com.redhat.rhn.domain.action.salt.ApplyStatesActionResult} to the results
     * set.
     *
     * @param resultIn ApplyStatesActionResult to add to the set
     */
    public void addResult(ImageInspectActionResult resultIn) {
        resultIn.setParentScriptActionDetails(this);
        results.add(resultIn);
    }
}

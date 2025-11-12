/*
 * Copyright (c) 2016--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.domain.action.salt.inspect;

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.action.Action;


import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/**
 * ImageInspectActionDetails - Class representation of the table rhnActionImageInspect.
 */
@Entity
@Table(name = "rhnActionImageInspect")
public class ImageInspectActionDetails extends BaseDomainHelper {

    @Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "image_inspect_seq")
	@SequenceGenerator(name = "image_inspect_seq", sequenceName = "RHN_ACT_IMAGE_INSPECT_ID_SEQ", allocationSize = 1)
    private Long id;

    @Column
    private String name;

    @Column
    private String version;

    @Column(name = "image_store_id")
    private Long imageStoreId;

    @OneToMany(mappedBy = "id.actionImageInspectId", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ImageInspectActionResult> results = new HashSet<>();

    @Column(name = "build_action_id")
    private Long buildActionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "action_id", nullable = false)
    private Action parentAction;

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
    public Long getImageStoreId() {
        return imageStoreId;
    }

    /**
     * @param imageStoreIdIn the image store id to set
     */
    public void setImageStoreId(Long imageStoreIdIn) {
        this.imageStoreId = imageStoreIdIn;
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

    /**
     * Gets the parent Action associated with this ServerAction record
     * @return Returns the parentAction.
     */
    public Action getParentAction() {
        return parentAction;
    }

    /**
     * Sets the parent Action associated with this ServerAction record
     * @param parentActionIn The parentAction to set.
     */
    public void setParentAction(Action parentActionIn) {
        this.parentAction = parentActionIn;
    }
}

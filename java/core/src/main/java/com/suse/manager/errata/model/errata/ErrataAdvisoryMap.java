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
package com.suse.manager.errata.model.errata;

import com.redhat.rhn.domain.BaseDomainHelper;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "suseErrataAdvisoryMap")
public class ErrataAdvisoryMap extends BaseDomainHelper {

    private Long id;
    private String patchId;
    private String announcementId;
    private String advisoryUri;

    /**
     * Default constructor
     */
    public ErrataAdvisoryMap() {
        this(null, null, null);
    }

    /**
     * Constructor
     *
     * @param patchIdIn
     * @param announcementIdIn
     * @param advisoryUriIn
     */
    public ErrataAdvisoryMap(String patchIdIn, String announcementIdIn, String advisoryUriIn) {
        patchId = patchIdIn;
        announcementId = announcementIdIn;
        advisoryUri = advisoryUriIn;
    }

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    public void setId(Long idIn) {
        id = idIn;
    }

    @Column(name = "patch_id")
    public String getPatchId() {
        return patchId;
    }

    public void setPatchId(String patchIdIn) {
        patchId = patchIdIn;
    }

    @Column(name = "announcement_id")
    public String getAnnouncementId() {
        return announcementId;
    }

    public void setAnnouncementId(String announcementIdIn) {
        announcementId = announcementIdIn;
    }

    @Column(name = "advisory_uri")
    public String getAdvisoryUri() {
        return advisoryUri;
    }

    public void setAdvisoryUri(String advisoryUriIn) {
        advisoryUri = advisoryUriIn;
    }

    @Override
    public boolean equals(Object oIn) {
        if (!(oIn instanceof ErrataAdvisoryMap that)) {
            return false;
        }
        return Objects.equals(getPatchId(), that.getPatchId()) &&
                Objects.equals(getAnnouncementId(), that.getAnnouncementId()) &&
                Objects.equals(getAdvisoryUri(), that.getAdvisoryUri());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPatchId(),
                getAnnouncementId(),
                getAdvisoryUri());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ErrataAdvisoryMap{");
        sb.append("id=").append(id);
        sb.append(", patchId='").append(patchId).append('\'');
        sb.append(", announcementId='").append(announcementId).append('\'');
        sb.append(", advisoryUri='").append(advisoryUri).append('\'');
        sb.append('}');
        return sb.toString();
    }
}

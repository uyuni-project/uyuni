/*
 * Copyright (c) 2024 SUSE LLC
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
package com.redhat.rhn.domain.channel;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

@Entity
@Table(name = "suseAppstreamApi",
        indexes = @Index(name = "suse_appstream_api_rpm_idx", columnList = "rpm"))
public class AppStreamApi {

    /**
     * Standard constructor
     */
    public AppStreamApi() { }

    /**
     * Constructor
     *
     * @param rpmIn the name of the rpm
     * @param idIn the id of the appstream
     */
    public AppStreamApi(String rpmIn, Long idIn) {
        this.id = new AppStreamApiKey(idIn, rpmIn);
    }

    @EmbeddedId
    private AppStreamApiKey id;

    @ManyToOne
    @JoinColumn(name = "module_id", nullable = false, insertable = false, updatable = false)
    @MapsId("id")  // Maps the 'id' from AppStreamApiKey to the AppStream entity
    private AppStream appStream;

    public AppStreamApiKey getId() {
        return id;
    }

    public void setId(AppStreamApiKey idIn) {
        this.id = idIn;
    }

    public String getRpm() {
        return id.getRpm();
    }

    /**
     * set RPM
     * @param rpmIn input RPM
     */
    public void setRpm(String rpmIn) {
        id.setRpm(rpmIn);
    }

    public Long getModuleId() {
        return id.getId();
    }

    /**
     * set module ID
     * @param idIn input ID
     */
    public void setModuleId(Long idIn) {
        id.setId(idIn);
    }

    public AppStream getAppStream() {
        return appStream;
    }

    public void setAppStream(AppStream appStreamIn) {
        appStream = appStreamIn;
    }
}

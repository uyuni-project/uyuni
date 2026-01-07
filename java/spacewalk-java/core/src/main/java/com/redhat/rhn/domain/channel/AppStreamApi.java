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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

@Entity
@IdClass(AppStreamApiKey.class)
@Table(name = "suseAppstreamApi")
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
        id = idIn;
        rpm = rpmIn;
    }

    @Id
    @Column(name = "module_id")
    private Long id;

    @Id
    private String rpm;

    @ManyToOne
    @JoinColumn(name = "module_id")
    @MapsId("id")
    private AppStream appStream;

    public Long getId() {
        return id;
    }

    public void setId(Long idIn) {
        id = idIn;
    }

    public String getRpm() {
        return rpm;
    }

    public void setRpm(String rpmIn) {
        rpm = rpmIn;
    }

    public AppStream getAppStream() {
        return appStream;
    }

    public void setAppStream(AppStream appStreamIn) {
        appStream = appStreamIn;
    }
}

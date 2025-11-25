/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.domain.server;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "suseServerSAPWorkload")
public class SAPWorkload implements Serializable {

    @Serial
    private static final long serialVersionUID = 5293378241323211232L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sap_system_id")
    private String systemIdSAP;

    @Column(name = "instance_type")
    private String instanceType;

    @ManyToOne
    @JoinColumn(name = "server_id")
    private Server server;

    /**
     * Constructs a SAPWorkload instance.
     */
    public SAPWorkload() { }

    /**
     * Constructs a SAPWorkload instance with the specified server, system ID, and instance type.
     *
     * @param serverIn      the server
     * @param systemIdSAPIn the SAP system
     * @param instanceTypeIn the SAP type of instance
     */
    public SAPWorkload(Server serverIn, String systemIdSAPIn, String instanceTypeIn) {
        this.server = serverIn;
        this.systemIdSAP = systemIdSAPIn;
        this.instanceType = instanceTypeIn;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long idIn) {
        id = idIn;
    }

    public String getSystemIdSAP() {
        return systemIdSAP;
    }

    public void setSystemIdSAP(String systemIdSAPIn) {
        systemIdSAP = systemIdSAPIn;
    }

    public String getInstanceType() {
        return instanceType;
    }

    public void setInstanceType(String instanceTypeIn) {
        instanceType = instanceTypeIn;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server serverIn) {
        server = serverIn;
    }

    @Override
    public boolean equals(Object oIn) {
        if (this == oIn) {
            return true;
        }
        if (oIn == null || getClass() != oIn.getClass()) {
            return false;
        }
        SAPWorkload that = (SAPWorkload) oIn;
        return Objects.equals(systemIdSAP, that.systemIdSAP) &&
                Objects.equals(instanceType, that.instanceType) &&
                Objects.equals(server, that.server);
    }

    @Override
    public int hashCode() {
        return Objects.hash(systemIdSAP, instanceType, server);
    }
}

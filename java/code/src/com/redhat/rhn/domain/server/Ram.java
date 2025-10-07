/*
 * Copyright (c) 2009--2010 Red Hat, Inc.
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
package com.redhat.rhn.domain.server;

import com.redhat.rhn.domain.BaseDomainHelper;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * Represents the amount of ram on a particular server.
 */
@Entity
@Table(name = "rhnRam")
public class Ram extends BaseDomainHelper {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ram_seq")
    @SequenceGenerator(name = "ram_seq", sequenceName = "rhn_ram_id_seq", allocationSize = 1)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id")
    private Server server;
    @Column
    private long ram;
    @Column
    private long swap;

    /**
     * Represents a servers memory information.
     */
    public Ram() {
        super();
    }

    /**
     * Returns the database id of the ram object.
     * @return Returns the id.
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the database id of the ram object.
     * @param idIn The id to set.
     */
    protected void setId(Long idIn) {
        id = idIn;
    }

    /**
     * Returns the amount of total ram on a server.
     * @return the amount of total ram on a server.
     */
    public long getRam() {
        return ram;
    }

    /**
     * Sets the total amount of ram on a server
     * @param ramIn The total amount of ram on a server.
     */
    public void setRam(long ramIn) {
        ram = ramIn;
    }

    /**
     * The parent server.
     * @return Returns the server.
     */
    public Server getServer() {
        return server;
    }

    /**
     * Sets the parent server.
     * @param serverIn The server to set.
     */
    public void setServer(Server serverIn) {
        server = serverIn;
    }

    /**
     * Returns the amount of total swap on a server.
     * @return the amount of total swap on a server.
     */
    public long getSwap() {
        return swap;
    }

    /**
     * Sets the total swap on a server.
     * @param swapIn The total swap on a server.
     */
    public void setSwap(long swapIn) {
        swap = swapIn;
    }
}

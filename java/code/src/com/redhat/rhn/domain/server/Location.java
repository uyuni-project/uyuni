/*
 * Copyright (c) 2025 SUSE LLC
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

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Location - Class representation of the table rhnServerLocation.
 */
@Entity
@Table(name = "rhnServerLocation")
public class Location extends BaseDomainHelper {

    @Id
    @Column(name = "ID")
    @GeneratedValue(generator = "server_loc_seq")
    @GenericGenerator(
            name = "server_loc_seq",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @Parameter(name = "sequence_name", value = "rhn_server_loc_id_seq"),
                    @Parameter(name = "increment_size", value = "1")
            })
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id")
    private Server server;

    @Column(name = "MACHINE")
    private String machine;

    @Column(name = "RACK")
    private String rack;

    @Column(name = "ROOM")
    private String room;

    @Column(name = "BUILDING")
    private String building;

    @Column(name = "ADDRESS1")
    private String address1;

    @Column(name = "ADDRESS2")
    private String address2;

    @Column(name = "CITY")
    private String city;

    @Column(name = "STATE")
    private String state;

    @Column(name = "COUNTRY")
    private String country;

    /**
     * Getter for id
     * @return Long to get
    */
    public Long getId() {
        return this.id;
    }

    /**
     * Setter for id
     * @param idIn to set
    */
    protected void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * Getter for server
     * @return server to get
    */
    public Server getServer() {
        return this.server;
    }

    /**
     * Setter for server
     * @param serverIn to set
    */
    public void setServer(Server serverIn) {
        this.server = serverIn;
    }

    /**
     * Getter for machine
     * @return String to get
    */
    public String getMachine() {
        return this.machine;
    }

    /**
     * Setter for machine
     * @param machineIn to set
    */
    public void setMachine(String machineIn) {
        this.machine = machineIn;
    }

    /**
     * Getter for rack
     * @return String to get
    */
    public String getRack() {
        return this.rack;
    }

    /**
     * Setter for rack
     * @param rackIn to set
    */
    public void setRack(String rackIn) {
        this.rack = rackIn;
    }

    /**
     * Getter for room
     * @return String to get
    */
    public String getRoom() {
        return this.room;
    }

    /**
     * Setter for room
     * @param roomIn to set
    */
    public void setRoom(String roomIn) {
        this.room = roomIn;
    }

    /**
     * Getter for building
     * @return String to get
    */
    public String getBuilding() {
        return this.building;
    }

    /**
     * Setter for building
     * @param buildingIn to set
    */
    public void setBuilding(String buildingIn) {
        this.building = buildingIn;
    }

    /**
     * Getter for address1
     * @return String to get
    */
    public String getAddress1() {
        return this.address1;
    }

    /**
     * Setter for address1
     * @param address1In to set
    */
    public void setAddress1(String address1In) {
        this.address1 = address1In;
    }

    /**
     * Getter for address2
     * @return String to get
    */
    public String getAddress2() {
        return this.address2;
    }

    /**
     * Setter for address2
     * @param address2In to set
    */
    public void setAddress2(String address2In) {
        this.address2 = address2In;
    }

    /**
     * Getter for city
     * @return String to get
    */
    public String getCity() {
        return this.city;
    }

    /**
     * Setter for city
     * @param cityIn to set
    */
    public void setCity(String cityIn) {
        this.city = cityIn;
    }

    /**
     * Getter for state
     * @return String to get
    */
    public String getState() {
        return this.state;
    }

    /**
     * Setter for state
     * @param stateIn to set
    */
    public void setState(String stateIn) {
        this.state = stateIn;
    }

    /**
     * Getter for country
     * @return String to get
    */
    public String getCountry() {
        return this.country;
    }

    /**
     * Setter for country
     * @param countryIn to set
    */
    public void setCountry(String countryIn) {
        this.country = countryIn;
    }

    /**
     * Returns true if all of the attributes are blank.
     * @return true if all of the attributes are blank.
     */
    public boolean isEmpty() {
        return StringUtils.isBlank(machine) &&
            StringUtils.isBlank(rack) &&
            StringUtils.isBlank(room) &&
            StringUtils.isBlank(building) &&
            StringUtils.isBlank(address1) &&
            StringUtils.isBlank(address2) &&
            StringUtils.isBlank(city) &&
            StringUtils.isBlank(state) &&
            StringUtils.isBlank(country);
    }

}

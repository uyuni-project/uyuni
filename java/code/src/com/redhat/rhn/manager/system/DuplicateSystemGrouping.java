/*
 * Copyright (c) 2009--2011 Red Hat, Inc.
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
package com.redhat.rhn.manager.system;

import com.redhat.rhn.domain.Identifiable;
import com.redhat.rhn.frontend.dto.NetworkDto;
import com.redhat.rhn.frontend.struts.Expandable;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * DuplicateSystemBucket
 */
public class DuplicateSystemGrouping implements Expandable, Identifiable {

    private String key;
    private List<NetworkDto> systems;

    /**
     * Constructor
     * @param net networkDto Object
     */
    public DuplicateSystemGrouping(NetworkDto net) {
        key = net.getKey();
        systems = new ArrayList<>();
        systems.add(net);
    }


    /**
     * @return Returns the key.
     */
    public String getKey() {
        return key;
    }

    /**
     * @param newKey String Set new key
     */
    public void setKey(String newKey) {
        key = newKey;
    }

    /**
     * Add a object to the bucket if there is a match
     *
     * @param net the object to add
     * @return true if added, false otherwise
     */
    public boolean addIfMatch(NetworkDto net) {
        if (net.getKey().equals(key)) {
            systems.add(net);
            return true;
        }
        return false;
    }


    /**
     * @return Returns the systems.
     */
    public List<NetworkDto> getSystems() {
        return systems;
    }

    /**
     * Expand the systems
     * @return the expansion
     */
    @Override
    public List<NetworkDto> expand() {
       return   getSystems();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("Key: %s; Systems %s", getKey(), getSystems());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getId() {
        return (long) key.hashCode();
    }

}

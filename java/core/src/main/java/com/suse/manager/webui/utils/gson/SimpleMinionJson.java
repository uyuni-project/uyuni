/*
 * Copyright (c) 2017 SUSE LLC
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

package com.suse.manager.webui.utils.gson;

import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.frontend.dto.BaseTupleDto;

import javax.persistence.Tuple;

/**
 * Simple JSON representation of a Salt Minion.
 */
public class SimpleMinionJson extends BaseTupleDto {

    /**
     * Default constructor
     */
    public SimpleMinionJson() { }

    /**
     * Constructor used to populate using DTO projection from the data of query that list simple minions
     *
     * @param tuple JPA tuple
     */
    public SimpleMinionJson(Tuple tuple) {
        setId(getTupleValue(tuple, "id", Number.class).map(Number::longValue).orElse(null));
        setName(getTupleValue(tuple, "name", String.class).orElse("-"));
    }

    private Long id;
    private String name;

    /**
     * Instantiates a new Simple minion json.
     *
     * @param idIn       the system id
     * @param minionIdIn the minion id
     */
    public SimpleMinionJson(Long idIn, String minionIdIn) {
        id = idIn;
        name = minionIdIn;
    }

    /**
     * Gets system id.
     *
     * @return the system id
     */
    public Long getId() {
        return id;
    }

    /**
     * Gets minion id.
     *
     * @return the minion id
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the minion id
     *
     * @param idIn the minion id
     */
    public void setId(Long idIn) {
        id = idIn;
    }

    /**
     * Set the minion name
     *
     * @param nameIn the minion name
     */
    public void setName(String nameIn) {
        name = nameIn;
    }

    /**
     * Instantiates a new minion json from a {@link MinionServer}
     *
     * @param server the server
     * @return the simple minion json
     */
    public static SimpleMinionJson fromMinionServer(MinionServer server) {
        return new SimpleMinionJson(server.getId(), server.getMinionId());
    }
}

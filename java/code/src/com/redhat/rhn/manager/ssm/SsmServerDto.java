/*
 * Copyright (c) 2018 SUSE LLC
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

package com.redhat.rhn.manager.ssm;

import com.redhat.rhn.domain.server.Server;

/**
 * Dto holding basic server info.
 */
public class SsmServerDto {

    private long id;
    private String name;

    /**
     * No arg constructor.
     */
    public SsmServerDto() {
    }

    /**
     * Constructor.
     * @param idIn server id
     * @param nameIn server name
     */
    public SsmServerDto(long idIn, String nameIn) {
        this.id = idIn;
        this.name = nameIn;
    }

    /**
     * @return server id
     */
    public long getId() {
        return id;
    }

    /**
     * @return server name
     */
    public String getName() {
        return name;
    }

    /**
     * Factory method for creating dto from {@link Server} obj.
     * @param srv server
     * @return a dto
     */
    public static SsmServerDto from(Server srv) {
        return new SsmServerDto(srv.getId(), srv.getName());
    }
}

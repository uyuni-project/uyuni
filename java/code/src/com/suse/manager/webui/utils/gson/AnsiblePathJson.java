/*
 * Copyright (c) 2021 SUSE LLC
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

import com.redhat.rhn.domain.server.ansible.AnsiblePath;

/**
 * DTO for AnsiblePath
 */
public class AnsiblePathJson {

    private Long id;
    private Long minionServerId;
    private String type;
    private String path;

    /**
     * Standard constructor
     *
     * @param entity the backend entity
     */
    public AnsiblePathJson(AnsiblePath entity) {
        this.id = entity.getId();
        this.minionServerId = entity.getMinionServer().getId();
        this.type = entity.getEntityType().getLabel();
        this.path = entity.getPath().toString();
    }

    /**
     * Gets the id.
     *
     * @return id
     */
    public Long getId() {
        return id;
    }

    /**
     * Gets the minion server id.
     *
     * @return minionServerId
     */
    public Long getMinionServerId() {
        return minionServerId;
    }

    /**
     * Gets the type.
     *
     * @return type
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the path.
     *
     * @return path
     */
    public String getPath() {
        return path;
    }
}

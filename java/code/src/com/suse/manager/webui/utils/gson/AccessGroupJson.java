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
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.suse.manager.webui.utils.gson;

import com.redhat.rhn.frontend.dto.BaseTupleDto;

import javax.persistence.Tuple;

/**
 * Simple JSON representation of {@link com.redhat.rhn.domain.access.AccessGroup} for listing roles
 */
public class AccessGroupJson extends BaseTupleDto {

    /**
     * Constructor used to populate using DTO projection from the data of query that list access groups
     *
     * @param tuple JPA tuple
     */
    public AccessGroupJson(Tuple tuple) {
        setId(getTupleValue(tuple, "id" , Number.class).map(Number::longValue).orElse(null));
        setName(getTupleValue(tuple, "name" , String.class).orElse("-"));
        setDescription(getTupleValue(tuple, "description" , String.class).orElse("-"));
        setType(getTupleValue(tuple, "type" , String.class).orElse("-"));
        setUsers(getTupleValue(tuple, "users", Number.class).map(Number::longValue).orElse(null));
        setPermissions(getTupleValue(tuple, "permissions", Number.class).map(Number::longValue).orElse(null));
    }

    private Long id;
    private String name;
    private String description;
    private String type;
    private Long users;
    private Long permissions;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long idIn) {
        id = idIn;
    }

    public String getName() {
        return name;
    }

    public void setName(String nameIn) {
        name = nameIn;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String descriptionIn) {
        description = descriptionIn;
    }

    public String getType() {
        return type;
    }

    public void setType(String typeIn) {
        type = typeIn;
    }

    public Long getUsers() {
        return users;
    }

    public void setUsers(Long usersIn) {
        users = usersIn;
    }

    public Long getPermissions() {
        return permissions;
    }

    public void setPermissions(Long permissionsIn) {
        permissions = permissionsIn;
    }
}

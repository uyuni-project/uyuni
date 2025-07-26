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

import com.redhat.rhn.domain.access.AccessGroup;
import com.redhat.rhn.frontend.dto.BaseTupleDto;

import java.util.ArrayList;
import java.util.List;

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
        setOrgId(getTupleValue(tuple, "org_id" , Number.class).map(Number::longValue).orElse(null));
        setOrgName(getTupleValue(tuple, "org_name" , String.class).orElse("-"));
        setNumUsers(getTupleValue(tuple, "users", Number.class).map(Number::longValue).orElse(null));
        setNumPermissions(getTupleValue(tuple, "permissions", Number.class).map(Number::longValue).orElse(null));
    }

    /** Constructor used to serialize an access group into json object
     *
     * @param group the access group
     */
    public AccessGroupJson(AccessGroup group) {
        setId(group.getId());
        setName(group.getLabel());
        setDescription(group.getDescription());
        setOrgId(group.getOrg().getId());
        setOrgName(group.getOrg().getName());
        setUsers(new ArrayList<>());
        setPermissions(new ArrayList<>());
    }

    private Long id;
    private String name;
    private String description;
    private List<String> accessGroups;
    private Long orgId;
    private String orgName;
    private Long numUsers;
    private List<AccessGroupUserJson> users;
    private Long numPermissions;
    private List<NamespaceJson> permissions;

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

    public List<String> getAccessGroups() {
        return accessGroups;
    }

    public void setAccessGroups(List<String> accessGroupsIn) {
        accessGroups = accessGroupsIn;
    }

    public Long getOrgId() {
        return orgId;
    }

    public void setOrgId(Long orgIdIn) {
        orgId = orgIdIn;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgNameIn) {
        orgName = orgNameIn;
    }

    public Long getNumUsers() {
        return numUsers;
    }

    public void setNumUsers(Long numUsersIn) {
        numUsers = numUsersIn;
    }

    public List<AccessGroupUserJson> getUsers() {
        return users;
    }

    public void setUsers(List<AccessGroupUserJson> usersIn) {
        users = usersIn;
    }

    public Long getNumPermissions() {
        return numPermissions;
    }

    public void setNumPermissions(Long numPermissionsIn) {
        numPermissions = numPermissionsIn;
    }

    public List<NamespaceJson> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<NamespaceJson> permissionsIn) {
        permissions = permissionsIn;
    }
}

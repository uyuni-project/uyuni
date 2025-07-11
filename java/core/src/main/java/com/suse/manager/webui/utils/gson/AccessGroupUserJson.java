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
 * Simple JSON representation for listing users
 */
public class AccessGroupUserJson extends BaseTupleDto {

    /**
     * Constructor used to populate using DTO projection from the data of query that list access groups
     *
     * @param tuple JPA tuple
     */
    public AccessGroupUserJson(Tuple tuple) {
        setId(getTupleValue(tuple, "id" , Number.class).map(Number::longValue).orElse(null));
        setLogin(getTupleValue(tuple, "login" , String.class).orElse("-"));
        setEmail(getTupleValue(tuple, "email" , String.class).orElse("-"));
        setName(getTupleValue(tuple, "name" , String.class).orElse("-"));
        setOrgName(getTupleValue(tuple, "org_name" , String.class).orElse("-"));
    }

    private Long id;
    private String login;
    private String email;
    private String name;
    private String orgName;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long idIn) {
        id = idIn;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String loginIn) {
        login = loginIn;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String emailIn) {
        email = emailIn;
    }

    public String getName() {
        return name;
    }

    public void setName(String nameIn) {
        name = nameIn;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgNameIn) {
        orgName = orgNameIn;
    }
}

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
package com.redhat.rhn.frontend.dto;

import org.apache.commons.lang3.StringEscapeUtils;

/**
 * Simple DTO for transfering data from the DB to the UI through datasource.
 *
 */
public class MultiOrgUserOverview extends BaseDto {
    private Long id;
    private String login;
    private String loginUc;
    private String userLogin;
    private String userFirstName;
    private String userLastName;
    private String address;
    private Long orgAdmin;


    /**
     * get the id
     * @return the id
     */
    @Override
    public Long getId() {
        return id;
    }

    /**
     * get the user login
     * @return the user login
     */
    public String getUserLogin() {
        return StringEscapeUtils.escapeHtml4(userLogin);
    }

    /**
     * get the login
     * @return the login
     */
    public String getLogin() {
        return login;
    }

    /**
     * get the login Uppercased
     * @return the login Uppercased
     */
    public String getLoginUc() {
        return loginUc;
    }

    /**
     * get the user's first name
     * @return the user's first name
     */
    public String getUserFirstName() {
        return StringEscapeUtils.escapeHtml4(userFirstName);
    }

    /**
     * get the user's last name
     * @return the user's last name
     */
    public String getUserLastName() {
        return StringEscapeUtils.escapeHtml4(userLastName);
    }

    /**
     * Set the id
     * @param i the id to set.
     */
    public void setId(Long i) {
        id = i;
    }

    /**
     * Set the login
     * @param l the login to set.
     */
    public void setLogin(String l) {
        login = l;
    }

    /**
     * Set the upper case login
     * @param l the login to set.
     */
    public void setLoginUc(String l) {
        loginUc = l;
    }

    /**
     * Set the user login
     * @param l the id to set.
     */
    public void setUserLogin(String l) {
        userLogin = l;
    }

    /**
     * Set the first name
     * @param fname the first nameto set.
     */
    public void setUserFirstName(String fname) {
        userFirstName = fname;
    }

    /**
     * Set the last name
     * @param lname the last name to set.
     */
    public void setUserLastName(String lname) {
        userLastName = lname;
    }

    /**
     *
     * @return if user is a org admin
     */
    public Long getOrgAdmin() {
        return orgAdmin;
    }

    /**
     *
     * @param orgAdminIn if user is a org admin
     */
    public void setOrgAdmin(Long orgAdminIn) {
        this.orgAdmin = orgAdminIn;
    }

    /**
     *
     * @return email address for user
     */
    public String getAddress() {
        return address;
    }

    /**
     *
     * @param addressIn email address to set
     */
    public void setAddress(String addressIn) {
        this.address = addressIn;
    }

    /**
     * @return the Display name of the user.
     */
    public String getUserDisplayName() {
        return getUserLastName() + ", " + getUserFirstName();
    }
}


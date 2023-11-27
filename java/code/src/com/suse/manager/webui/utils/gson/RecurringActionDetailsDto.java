/*
 * Copyright (c) 2023 SUSE LLC
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

import java.util.Date;
import java.util.Map;
import java.util.Set;

public class RecurringActionDetailsDto {

    /** Array containing Quartz information */
    private Map<String, String> cronTimes;

    /** Is test run */
    private boolean test;

    /** Schedule creation date **/
    private Date createdAt;

    /**  Login of the schedule creator **/
    private String creatorLogin;

    /** The schedule type */
    private String type;

    /** The states assigned to a custom state schedule */
    private Set<StateConfigJson> states;

    /**
     * @return the Array containing Quartz information
     */
    public Map<String, String> getCronTimes() {
        return cronTimes;
    }

    /**
     * @return the Array containing Quartz information
     */
    public boolean isTest() {
        return test;
    }

    /**
     * Sets the cronTimes.
     *
     * @param cronTimesIn the cronTimes
     */
    public void setCronTimes(Map<String, String> cronTimesIn) {
        cronTimes = cronTimesIn;
    }

    /**
     * Sets the test.
     *
     * @param testIn the test
     */
    public void setTest(boolean testIn) {
        test = testIn;
    }

    /**
     * Gets the creation date
     *
     * @return createdAt
     */
    public Date getCreated() {
        return createdAt;
    }

    /**
     *  Sets the creation date
     *
     * @param createdAtIn schedule creation date
     */
    public void setCreated(Date createdAtIn) {
        this.createdAt = createdAtIn;
    }

    /**
     *  Sets the creator login
     *
     * @param creatorLoginIn login name of the creator
     */
    public void setCreatorLogin(String creatorLoginIn) {
        this.creatorLogin = creatorLoginIn;
    }

    /**
     *  Get the creator login
     *
     * @return creatorLoginIn
     */
    public String getCreatorLogin() {
        return this.creatorLogin;
    }

    /**
     * @return the type of the schedule
     */
    public String getType() {
        return this.type;
    }

    /**
     * Sets the type.
     *
     * @param typeIn the type
     */
    public void setType(String typeIn) {
        type = typeIn;
    }

    /**
     * @return the set of states
     */
    public Set<StateConfigJson> getStates() {
        return this.states;
    }

    /**
     * Sets the states
     *
     * @param statesIn the states
     */
    public void setStates(Set<StateConfigJson> statesIn) {
        this.states = statesIn;
    }
}

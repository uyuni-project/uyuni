/**
 * Copyright (c) 2015 SUSE LLC
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

import java.util.List;
import java.util.Map;

/**
 * JSON representation of the Recurring State Scheduling
 */
public class RecurringStateScheduleJson {

    /** Mionion ids */
    private List<Long> minionIds;

    /** Mionion Names */
    private List<String> minionNames;

    /** Name of the schedule */
    private String scheduleName;

    /** The schedule type */
    private String type;

    /** The schedule target Type.
     * Either minion, group, selected minions or organization
     */
    private String targetType;

    /** Array containing Quartz information */
    private Map<String, String> cronTimes;

    /** Cron format string */
    private String cron;

    /** Is test run */
    private boolean test = false;

    /**
     * @return the minion ids
     */
    public List<Long> getMinionIds() {return minionIds;}

    /**
     * @return the minion names
     */
    public List<String> getMinionNames() {return minionNames;}

    /**
     * @return the name of the schedule
     */
    public String getScheduleName() {return scheduleName;}

    /**
     * @return the type of the schedule
     */
    public String getType() {return type;}

    /**
     * @return the target type of the schedule
     */
    public String getTargetType() {return targetType;}

    /**
     * @return the Array containing Quartz information
     */
    public Map<String, String> getCronTimes() {return cronTimes;}

    /**
     * @return the Array containing Quartz information
     */
    public String getCron() {return cron;}

    /**
     * @return the Array containing Quartz information
     */
    public boolean isTest() {return test;}
}

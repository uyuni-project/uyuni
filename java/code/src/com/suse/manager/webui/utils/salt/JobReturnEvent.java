/**
 * Copyright (c) 2016 SUSE LLC
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
package com.suse.manager.webui.utils.salt;

import com.suse.saltstack.netapi.datatypes.Event;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Representation of job return events fired each time a minion returns data for a job.
 */
public class JobReturnEvent {

    private static final Pattern PATTERN =
            Pattern.compile("^salt/job/([^/]+)/ret/([^/]+)$");

    private final String jobId;
    private final String minionId;
    private final Map<String, Object> data;

    /**
     * Creates a new JobReturnEvent
     * @param jobIdIn the id of the job
     * @param minionIdIn the id of the minion returning the job
     * @param dataIn data containing more information about this event
     */
    public JobReturnEvent(String jobIdIn, String minionIdIn, Map<String, Object> dataIn) {
        this.jobId = jobIdIn;
        this.minionId = minionIdIn;
        this.data = dataIn;
    }

    /**
     * The id of the job
     *
     * @return job id
     */
    public String getJobId() {
        return jobId;
    }

    /**
     * The id of the minion that returned the ob
     *
     * @return the minion id
     */
    public String getMinionId() {
        return minionId;
    }

    /**
     * The event data containing more information about this event
     *
     * @return the event data
     */
    public Map<String, Object> getData() {
        return data;
    }

    /**
     * Utility method to parse e generic event to a more specific one
     * @param event the generic event to parse
     * @return an option containing the parsed value or non if it could not be parsed
     */
    public static Optional<JobReturnEvent> parse(Event event) {
        Matcher matcher = PATTERN.matcher(event.getTag());
        if (matcher.matches()) {
            JobReturnEvent result = new JobReturnEvent(matcher.group(1), matcher.group(2),
                    event.getData());
            return Optional.of(result);
        }
        else {
            return Optional.empty();
        }
    }
}

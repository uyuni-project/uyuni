/**
 * Copyright (c) 2019 SUSE LLC
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
package com.redhat.rhn.taskomatic.task.checkin;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.taskomatic.task.TaskConstants;

import org.apache.log4j.Logger;

import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Class for finding candidates to perform a check-in.
 */
public class CheckinCandidatesResolver {

    private static final Logger LOG = Logger.getLogger(CheckinCandidatesResolver.class);

    // Properties used for generating random checkin thresholds
    private int thresholdMax;
    protected int thresholdMin;
    private double mean;
    private double stddev;

    private String findCheckinCandidatesQuery;

    /**
     * Constructor for CheckinCandidatesResolver.
     *
     * @param findCheckinCandidatesQueryIn the name of the query to use to find checkin candidates
     */
    public CheckinCandidatesResolver(String findCheckinCandidatesQueryIn) {
        this.findCheckinCandidatesQuery = findCheckinCandidatesQueryIn;
        this.thresholdMax = Config.get().getInt(ConfigDefaults.SYSTEM_CHECKIN_THRESHOLD) * 86400;
        this.thresholdMin = Math.round(this.thresholdMax / 2);
        this.mean = this.thresholdMax;
        this.stddev = this.thresholdMax / 6;
    }

    /**
     * Retrieves the systems who has not checked-in in the last 'thresholdMin' minutes,
     * prioritizing those with the oldest check-in date, and according to a Gauss normal distribution.
     *
     * @return the list of systems
     */
    public List<SystemSummary> getCheckinCandidates() {
        List<SystemSummary> candidates = new LinkedList<>();

        Calendar cal = Calendar.getInstance();
        long currentTimestamp = cal.getTime().getTime() / 1000;

        for (SystemSummary s : this.findCheckinCandidates()) {
            // Last checkin timestamp in seconds
            long lastCheckin = s.getLastCheckin().getTime() / 1000;

            // Determine random threshold in [t/2,t] using t as the mean and stddev as defined above.
            long randomThreshold = SystemCheckinUtils.getRandomThreshold(mean, stddev, thresholdMin, thresholdMax);
            long compareValue = currentTimestamp - randomThreshold;

            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Candidate: %s, Last check-in: %s, Random threshold in hours: %s ",
                        s.getName(), s.getLastCheckin().toString(), SystemCheckinUtils.toHours(randomThreshold)));
            }

            if (lastCheckin < compareValue) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Contacting system for checkin: " + s.getName());
                }
                candidates.add(s);
            }
        }
        return candidates;
    }

    /**
     * Run query to find checkin candidates to consider (last_checkin < now - t/2).
     *
     * @return list of checkin candidates
     */
    @SuppressWarnings("unchecked")
    private DataResult<SystemSummary> findCheckinCandidates() {
        SelectMode select = ModeFactory.getMode(TaskConstants.MODE_NAME, this.findCheckinCandidatesQuery);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("checkin_threshold", this.thresholdMin);
        return select.execute(params);
    }
}

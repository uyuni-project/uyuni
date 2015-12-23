package com.suse.manager.webui.utils.salt;

import com.suse.saltstack.netapi.datatypes.Event;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JobReturnEvent {

    private static final Pattern PATTERN =
            Pattern.compile("^salt/job/([^/]+)/ret/([^/]+)$");

    private final String jobId;
    private final String minionId;
    private final Map<String, Object> data;

    /**
     * Creates a new JobReturnEvent
     * @param jobId the id of the job
     * @param minionId the id of the minion returning the job
     * @param data data containing more information about this event
     */
    public JobReturnEvent(String jobId, String minionId, Map<String, Object> data) {
        this.jobId = jobId;
        this.minionId = minionId;
        this.data = data;
    }

    /**
     * The id of the job
     *
     * @return job id
     */
    public String getJobId() { return jobId; }

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
        } else {
            return Optional.empty();
        }
    }
}

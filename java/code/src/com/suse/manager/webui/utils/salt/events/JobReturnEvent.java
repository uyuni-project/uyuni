/**
 * CHECKSTYLE:OFF
 */
package com.suse.manager.webui.utils.salt.events;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.suse.manager.reactor.utils.LocalDateTimeISOAdapter;
import com.suse.manager.reactor.utils.OptionalTypeAdapterFactory;
import com.suse.manager.reactor.utils.ZonedDateTimeISOAdapter;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
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
    private final Data data;

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeISOAdapter())
            .registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeISOAdapter())
            .registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
            .create();

    public static class Data {
        @SerializedName("_stamp")
        private String timestamp;
        private String cmd;
        private String fun;
        @SerializedName("fun_args")
        private Object funArgs;
        private String id;
        private String jid;
        private Optional<String> schedule = Optional.empty();
        private String out;
        private int retcode = 0;
        private boolean success = false;
        //FIXUP: make metadata getter the same as result
        private Optional<JsonElement> metadata = Optional.empty();
        @SerializedName("return")
        private JsonElement result;

        public String getTimestamp() {
            return timestamp;
        }

        public String getCmd() {
            return cmd;
        }

        public String getFun() {
            return fun;
        }

        public Object getFunArgs() {
            return funArgs;
        }

        public String getId() {
            return id;
        }

        public String getJid() {
            return jid;
        }

        public String getOut() {
            return out;
        }

        public int getRetcode() {
            return retcode;
        }

        public boolean isSuccess() {
            return success;
        }

        public Object getResult() {
            return gson.fromJson(result, Object.class);
        }

        public <R> R getResult(Class<R> dataType) {
            return gson.fromJson(result, dataType);
        }

        public <R> R getResult(TypeToken<R> dataType) {
            return gson.fromJson(result, dataType.getType());
        }

        public Optional<Object> getMetadata() {
            return metadata.flatMap(md -> {
                try {
                    return Optional.ofNullable(gson.fromJson(md, Object.class));
                }
                catch (JsonSyntaxException ex) {
                    return Optional.empty();
                }
            });
        }

        public <R> Optional<R> getMetadata(Class<R> dataType) {
            return metadata.flatMap(md -> {
                try {
                    return Optional.ofNullable(gson.fromJson(md, dataType));
                }
                catch (JsonSyntaxException ex) {
                   return Optional.empty();
                }
            });
        }

        public <R> Optional<R> getMetadata(TypeToken<R> dataType) {
            return metadata.flatMap(md -> {
                try {
                    return Optional.ofNullable(gson.fromJson(md, dataType.getType()));
                }
                catch (JsonSyntaxException ex) {
                    return Optional.empty();
                }
            });
        }

        public Optional<String> getSchedule() {
            return schedule;
        }
    }

    /**
     * Creates a new JobReturnEvent
     * @param jobIdIn the id of the job
     * @param minionIdIn the id of the minion returning the job
     * @param dataIn data containing more information about this event
     */
    private JobReturnEvent(String jobIdIn, String minionIdIn, Data dataIn) {
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
     * @return the minion id
     */
    public String getMinionId() {
        return minionId;
    }

    /**
     * @return the event data
     */
    public Data getData() {
        return data;
    }

    /**
     * Utility method to parse a generic event into a more specific one.
     * @param event the generic event to parse
     * @return an option containing the parsed value or non if it could not be parsed
     */
    public static Optional<JobReturnEvent> parse(Event event) {
        Matcher matcher = PATTERN.matcher(event.getTag());
        if (matcher.matches()) {
            Data data = event.getData(Data.class);
            JobReturnEvent result = new JobReturnEvent(matcher.group(1), matcher.group(2),
                    data);
            return Optional.of(result);
        }
        else {
            return Optional.empty();
        }
    }
}

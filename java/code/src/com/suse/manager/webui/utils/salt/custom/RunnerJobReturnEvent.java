package com.suse.manager.webui.utils.salt.custom;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.suse.salt.netapi.datatypes.Event;
import com.suse.salt.netapi.parser.JsonParser;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by matei on 11/14/16.
 */
public class RunnerJobReturnEvent {

    private static final Pattern PATTERN =
            Pattern.compile("^salt/run/([^/]+)/ret$");

    private final String jobId;
    private final Data data;

    private static final Gson GSON = JsonParser.GSON;

    /**
     * Data object of the job return event
     */
    public static class Data {

        @SerializedName("_stamp")
        private String timestamp;
        private String fun;
        private String jid;
        private int retcode = 0;
        private boolean success = false;
        //FIXUP: make metadata getter the same as result
        private Optional<JsonElement> metadata = Optional.empty();
        @SerializedName("return")
        private JsonElement result;
        private String user;

        public String getTimestamp() {
            return timestamp;
        }

        public String getFun() {
            return fun;
        }

        public String getJid() {
            return jid;
        }

        public int getRetcode() {
            return retcode;
        }

        public boolean isSuccess() {
            return success;
        }

        public Object getResult() {
            return GSON.fromJson(result, Object.class);
        }

        public <R> R getResult(Class<R> dataType) {
            return GSON.fromJson(result, dataType);
        }

        public <R> R getResult(TypeToken<R> dataType) {
            return GSON.fromJson(result, dataType.getType());
        }

        public String getUser() {
            return user;
        }

        public Optional<Object> getMetadata() {
            return metadata.flatMap(md -> {
                try {
                    return Optional.ofNullable(GSON.fromJson(md, Object.class));
                } catch (JsonSyntaxException ex) {
                    return Optional.empty();
                }
            });
        }

        public <R> Optional<R> getMetadata(Class<R> dataType) {
            return metadata.flatMap(md -> {
                try {
                    return Optional.ofNullable(GSON.fromJson(md, dataType));
                } catch (JsonSyntaxException ex) {
                    return Optional.empty();
                }
            });
        }

        public <R> Optional<R> getMetadata(TypeToken<R> dataType) {
            return metadata.flatMap(md -> {
                try {
                    return Optional.ofNullable(GSON.fromJson(md, dataType.getType()));
                } catch (JsonSyntaxException ex) {
                    return Optional.empty();
                }
            });
        }

    }


    public RunnerJobReturnEvent(String jobId, Data data) {
        this.jobId = jobId;
        this.data = data;
    }

    public String getJobId() {
        return jobId;
    }

    public Data getData() {
        return data;
    }

    public static Optional<RunnerJobReturnEvent> parse(Event event) {
        Matcher matcher = PATTERN.matcher(event.getTag());
        if (matcher.matches()) {
            Data data = event.getData(Data.class);
            RunnerJobReturnEvent result = new RunnerJobReturnEvent(matcher.group(1), data);
            return Optional.of(result);
        } else {
            return Optional.empty();
        }
    }

}
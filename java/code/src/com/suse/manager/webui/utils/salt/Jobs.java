/**
 * CHECKSTYLE:OFF
 */
package com.suse.manager.webui.utils.salt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.suse.manager.reactor.utils.LocalDateTimeISOAdapter;
import com.suse.manager.reactor.utils.OptionalTypeAdapterFactory;
import com.suse.manager.reactor.utils.ZonedDateTimeISOAdapter;
import com.suse.salt.netapi.calls.RunnerCall;
import com.suse.salt.netapi.datatypes.StartTime;
import com.suse.salt.netapi.results.Result;

import static com.suse.salt.netapi.utils.ClientUtils.parameterizedType;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

public class Jobs {

    private Jobs() { }

    public static class ListJobsEntry {
        @SerializedName("Function")
        private String function;

        @SerializedName("StartTime")
        private StartTime startTime;

        @SerializedName("Arguments")
        private List<Object> arguments;

        @SerializedName("User")
        private String user;

        @SerializedName("Target")
        private Object target;

        public String getFunction() {
            return function;
        }

        public Date getStartTime(TimeZone tz) {
            return startTime == null ? null : startTime.getDate(tz);
        }

        public Date getStartTime() {
            return startTime == null ? null : startTime.getDate();
        }

        public List<Object> getArguments() {
            return arguments;
        }

        public String getUser() {
            return user;
        }

        public Object getTarget() {
            return target;
        }
    }

    public static class ListJobResult {

        private static final Gson GSON = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeISOAdapter())
                .registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeISOAdapter())
                .registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
                .create();

        @SerializedName("Function")
        private String function;

        @SerializedName("StartTime")
        private StartTime startTime;

        @SerializedName("Arguments")
        private List<Object> arguments;

        @SerializedName("User")
        private String user;

        @SerializedName("Target")
        private Object target;

        @SerializedName("Minions")
        private List<String> minions;

        @SerializedName("Target-type")
        private String targetType;

        private String jid;

        @SerializedName("Result")
        private Map<String, JsonElement> result;

        public String getFunction() {
            return function;
        }

        public StartTime getStartTime() {
            return startTime;
        }

        public List<Object> getArguments() {
            return arguments;
        }

        public String getUser() {
            return user;
        }

        public Object getTarget() {
            return target;
        }

        public List<String> getMinions() {
            return minions;
        }

        public String getTargetType() {
            return targetType;
        }

        public String getJid() {
            return jid;
        }

        public <T> Optional<T> getResult(String minionId, Class<T> type) {
            return Optional.ofNullable(result.get(minionId)).map(result -> {
                Type wrapperType = parameterizedType(null, Result.class, type);
                Result<T> r = GSON.fromJson(result, wrapperType);
                return r.getResult();
            });
        }

        public <T> Optional<T> getResult(String minionId, TypeToken<T> type) {
            return Optional.ofNullable(result.get(minionId)).map(result -> {
                Type wrapperType = parameterizedType(null, Result.class, type.getType());
                Result<T> r = GSON.fromJson(result, wrapperType);
                return r.getResult();
            });
        }

    }

    public static RunnerCall<Map<String, ListJobsEntry>> listJobs(Object searchMetadata) {
        LinkedHashMap<String, Object> args = new LinkedHashMap<>();
        args.put("search_metadata", searchMetadata);
        return new RunnerCall<>("jobs.list_jobs", Optional.of(args),
                new TypeToken<Map<String, ListJobsEntry>>() { });
    }

    public static RunnerCall<ListJobResult> listJob(String jid) {
        LinkedHashMap<String, Object> args = new LinkedHashMap<>();
        args.put("jid", jid);
        return new RunnerCall<>("jobs.list_job", Optional.of(args),
                new TypeToken<ListJobResult>(){});
    }

}

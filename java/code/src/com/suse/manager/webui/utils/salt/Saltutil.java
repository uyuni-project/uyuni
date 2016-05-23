/**
 * CHECKSTYLE:OFF
 */
package com.suse.manager.webui.utils.salt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.suse.manager.reactor.utils.OptionalTypeAdapterFactory;
import com.suse.salt.netapi.calls.LocalCall;

import java.util.List;
import java.util.Optional;

public class Saltutil {

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
            .create();

    private Saltutil() { }

    public static class RunningInfo {
        private String jid;
        private String fun;
        private int pid;
        private String target;
        @SerializedName("tgt_type")
        private String targetType;
        private String user;
        private Optional<JsonElement> metadata = Optional.empty();

        public <R> Optional<R> getMetadata(Class<R> type) {
            return metadata.map(json -> gson.fromJson(json, type));
        }

        public <R> Optional<R> getMetadata(TypeToken<R> type) {
            return metadata.map(json -> gson.fromJson(json, type.getType()));
        }

        public String getJid() {
            return jid;
        }

        public String getFun() {
            return fun;
        }

        public int getPid() {
            return pid;
        }

        public String getTarget() {
            return target;
        }

        public String getTargetType() {
            return targetType;
        }

        public String getUser() {
            return user;
        }
    }

    public static LocalCall<List<RunningInfo>> running() {
        return new LocalCall<>("saltutil.running",
                Optional.empty(), Optional.empty(), new TypeToken<List<RunningInfo>>() {
        });
    }
}

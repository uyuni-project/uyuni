package com.suse.manager.webui.utils.salt;

import com.google.gson.reflect.TypeToken;
import com.suse.saltstack.netapi.calls.LocalCall;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class Schedule {

    private Schedule(){
    }

    public static class Result {

        private String comment;
        private boolean result;

        public Result(String comment, boolean result) {
            this.comment = comment;
            this.result = result;
        }

        public boolean getResult() {
            return result;
        }

        public String getComment() {
            return comment;
        }

    }

    public static class Splay {



    }

    public static LocalCall<Result> delete(String name) {
        LinkedHashMap<String, Object> args = new LinkedHashMap<>();
        args.put("name", name);
        return new LocalCall<>("schedule.delete", Optional.empty(), Optional.of(args),
                new TypeToken<Result>(){ });
    }

    public static LocalCall<Result> add(String name, LocalCall<?> call, int seconds, boolean enabled, int maxrunning, Map<String, ?> metadata, boolean jidInclude) {
        LinkedHashMap<String, Object> args = new LinkedHashMap<>();
        Map<String, Object> payload = call.getPayload();
        args.put("function", payload.get("fun"));
        args.put("job_args", payload.get("arg"));
        args.put("job_kwargs", payload.get("kwarg"));

        args.put("name", name);
        args.put("seconds", seconds);
        args.put("enabled", enabled);
        args.put("maxrunning", maxrunning);
        args.put("metadata", metadata);
        args.put("jid_include", jidInclude);
        return new LocalCall<>("schedule.add", Optional.empty(), Optional.of(args),
                new TypeToken<Result>(){ });
    }

}

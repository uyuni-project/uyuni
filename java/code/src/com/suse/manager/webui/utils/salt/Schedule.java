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

import com.google.gson.reflect.TypeToken;
import com.suse.saltstack.netapi.calls.LocalCall;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * salt.modules.schedule
 */
public class Schedule {

    private Schedule() {
    }

    /**
     * Common result structure for scheduling functions
     */
    public static class Result {

        private String comment;
        private boolean result;

        /**
         * Construct a new Result
         * @param commentIn Human readable comment
         * @param resultIn boolean indicating success
         */
        public Result(String commentIn, boolean resultIn) {
            this.comment = commentIn;
            this.result = resultIn;
        }

        /**
         * boolean indicating success
         * @return boolean indicating success
         */
        public boolean getResult() {
            return result;
        }

        /**
         * Human readable comment
         * @return Human readable comment
         */
        public String getComment() {
            return comment;
        }

    }

    /**
     * Delete a schedule entry
     * @param name job name
     * @return the result
     */
    public static LocalCall<Result> delete(String name) {
        LinkedHashMap<String, Object> args = new LinkedHashMap<>();
        args.put("name", name);
        return new LocalCall<>("schedule.delete", Optional.empty(), Optional.of(args),
                new TypeToken<Result>() { });
    }

    /**
     * Schedule a salt call for later execution on the minion
     * @param name job name
     * @param call salt call schedule
     * @param once when to execute it once
     * @param metadata additional metadata
     * @return call object to execute via the client
     */
    public static LocalCall<Result> add(String name, LocalCall<?> call,
                                        LocalDateTime once, Map<String, ?> metadata) {
        LinkedHashMap<String, Object> args = new LinkedHashMap<>();
        Map<String, Object> payload = call.getPayload();
        args.put("function", payload.get("fun"));
        args.put("job_args", payload.get("arg"));
        args.put("job_kwargs", payload.get("kwarg"));

        args.put("name", name);
        args.put("once", once.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        args.put("metadata", metadata);
        return new LocalCall<>("schedule.add", Optional.empty(), Optional.of(args),
                new TypeToken<Result>() { });
    }

}

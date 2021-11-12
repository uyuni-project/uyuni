/**
 * Copyright (c) 2018 SUSE LLC
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
package com.redhat.rhn.domain.action.salt;

import com.suse.manager.webui.utils.YamlHelper;

import java.util.Formatter;
import java.util.Map;

/**
 * StateResult - Class for parsing and printing State Apply Action Results
 */
public class StateResult {

    private String id;
    private String name;
    private String function;
    private String comment;
    private boolean result = false;
    private String changes;
    private String sls;
    private String startTime;
    private double duration;
    private double runNum;

    /**
     * Constructor
     * @param e map
     */
    public StateResult(Map.Entry<String, Map<String, Object>> e) {
        String[] arr = e.getKey().split("_|-");
        function = arr[0] + "." + arr[arr.length - 1];

        e.getValue().forEach((key, val) -> {
            String value = val != null ? val.toString() : "";
            switch (key) {
            case "__id__":
                id = value;
                break;
            case "__run_num__":
                try {
                    runNum = Double.parseDouble(value);
                }
                catch (NumberFormatException e2) {
                    runNum = 0.0;
                }
                break;
            case "__sls__":
                sls = value;
                break;
            case "changes":
                if (value.equals("{}")) {
                    changes = value;
                }
                else {
                    changes = YamlHelper.INSTANCE.dump(val);
                    changes = changes.replaceAll("\\\\n", "\n");
                }
                break;
            case "comment":
                comment = value;
                break;
            case "name":
                name = value;
                break;
            case "start_time":
                startTime = value;
                break;
            case "result":
                result = Boolean.parseBoolean(value);
                break;
            case "duration":
                try {
                    duration = Double.parseDouble(value);
                }
                catch (NumberFormatException e2) {
                    duration = 0.0;
                }
                break;
            default:
                break;
            }
        });
    }

    /**
     * @return the comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the start time
     */
    public String getStartTime() {
        return startTime;
    }

    /**
     * @return the result
     */
    public boolean isResult() {
        return result;
    }

    /**
     * @return the duration
     */
    public double getDuration() {
        return duration;
    }

    /**
     * @return the order number
     */
    public double getRunNum() {
        return runNum;
    }

    /**
     * @return the SLS
     */
    public String getSls() {
        return sls;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @return the changes
     */
    public String getChanges() {
        StringBuilder retval = new StringBuilder();
        String indentation = new String(new char[14]).replace("\0", " ");
        for (int i = 0; i < changes.length(); i++) {
            char c = changes.charAt(i);
            switch (c) {
            case '\n':
                retval.append(c + indentation);
                break;
            default:
                retval.append(c);
                break;
            }
        }
        return retval.toString();
    }

    /**
     * @return the function
     */
    public String getFunction() {
        return function;
    }

    @Override
    public String toString() {
        StringBuilder retval = new StringBuilder();
        Formatter form = new Formatter(retval);
        form.format("----------\n");
        form.format("%1$12s: %2$s\n", "ID", getId());
        form.format("%1$12s: %2$s\n", "Function", getFunction());
        form.format("%1$12s: %2$s\n", "Name", getName());
        form.format("%1$12s: %2$s\n", "Result", isResult());
        form.format("%1$12s: %2$s\n", "Comment", getComment());
        form.format("%1$12s: %2$s\n", "Started", getStartTime());
        form.format("%1$12s: %2$s\n", "Duration", getDuration());
        form.format("%1$12s: %2$s\n", "SLS", getSls());
        form.format("%1$12s: %2$s\n", "Changed", getChanges());
        form.close();
        return retval.toString();
    }
}

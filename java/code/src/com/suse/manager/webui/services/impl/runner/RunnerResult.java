/**
 * Copyright (c) 2020 SUSE LLC
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

package com.suse.manager.webui.services.impl.runner;

import com.google.gson.JsonElement;

import java.util.Map;

public class RunnerResult {

    private Map<String, JsonElement> data;
    private String outputter;
    private int retcode;

    /**
     * @return data to get
     */
    public Map<String, JsonElement> getData() {
        return data;
    }

    /**
     * @param dataIn to set
     */
    public void setData(Map<String, JsonElement> dataIn) {
        this.data = dataIn;
    }

    /**
     * @return outputter to get
     */
    public String getOutputter() {
        return outputter;
    }

    /**
     * @param outputterIn to set
     */
    public void setOutputter(String outputterIn) {
        this.outputter = outputterIn;
    }

    /**
     * @return retcode to get
     */
    public int getRetcode() {
        return retcode;
    }

    /**
     * @param retcodeIn to set
     */
    public void setRetcode(int retcodeIn) {
        this.retcode = retcodeIn;
    }
}

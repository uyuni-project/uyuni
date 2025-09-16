/*
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

package com.redhat.rhn.domain.contentmgmt.modulemd;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Represents a module's available streams in a modular repository
 */
public class ModuleStreams {

    @SerializedName("default")
    private String defaultStream;
    private List<String> streams;

    /**
     * Initialize a new ModuleStreams instance
     * @param defaultStreamIn the name of the default stream
     * @param streamsIn the list of stream names
     */
    public ModuleStreams(String defaultStreamIn, List<String> streamsIn) {
        this.defaultStream = defaultStreamIn;
        this.streams = streamsIn;
    }

    public String getDefaultStream() {
        return defaultStream;
    }

    public List<String> getStreams() {
        return streams;
    }
}

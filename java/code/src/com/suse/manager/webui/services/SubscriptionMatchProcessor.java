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
package com.suse.manager.webui.services;

import com.suse.manager.matcher.MatcherJsonIO;
import com.suse.matcher.json.JsonMessage;
import com.suse.matcher.json.JsonOutput;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Processes data from the matcher to a form that's displayable by the UI.
 */
public class SubscriptionMatchProcessor {

    /**
     * Gets UI-ready data.
     *
     * @return the data
     */
    public Object getData() {
        Optional<JsonOutput> output = new MatcherJsonIO().getMatcherOutput();
        if (output.isPresent()) {
            return output.get();
        }
        else {
            return new JsonOutput(
                new Date(),
                new LinkedList<>(),
                new LinkedList<JsonMessage>() { { add(
                    new JsonMessage("no_matcher_data", new HashMap<String, String>())
                ); } }
            );
        }
    }
}

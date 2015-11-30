/**
 * Copyright (c) 2015 SUSE LLC
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

import com.suse.saltstack.netapi.datatypes.target.Target;

/**
 * Matcher based on salt grains
 */
public class Grains implements Target<String> {

    private final String grain;
    private final String value;
    private final String target;

    /**
     * Creates a grains matcher
     *
     * @param inGrain the grain name
     * @param inValue the value to match
     */
    public Grains(String inGrain, String inValue) {
        this.grain = inGrain;
        this.value = inValue;
        this.target = inGrain + ":" + inValue;
    }

    /**
     * @return the grain name of this matcher
     */
    public String getGrain() {
        return grain;
    }

    /**
     * @return the value to match the grain
     */
    public String getValue() {
        return value;
    }

    @Override
    public String getTarget() {
        return target;
    }

    @Override
    public String getType() {
        return "grain";
    }
}

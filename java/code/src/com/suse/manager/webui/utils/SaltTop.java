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

package com.suse.manager.webui.utils;

import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonMap;

/**
 * YAML generator for top files.
 */
public class SaltTop implements SaltState {

    private List<String> top;

    /**
     * Constructor.
     * @param topIn top content
     */
    public SaltTop(List<String> topIn) {
        this.top = topIn;
    }

    @Override
    public Map<String, Object> getData() {
        return singletonMap("base",
                singletonMap("*", top));
    }
}

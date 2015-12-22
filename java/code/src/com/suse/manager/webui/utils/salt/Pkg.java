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

import com.suse.saltstack.netapi.calls.LocalCall;

import com.google.gson.reflect.TypeToken;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * TODO: Merge this method into the Pkg class in saltstack-netapi-client.
 * salt.modules.pkg
 */
public class Pkg {

    private Pkg() { }

    public static LocalCall<Map<String, Object>> install(boolean refresh, List<String> pkgs) {
        LinkedHashMap<String, Object> kwargs = new LinkedHashMap<>();
        kwargs.put("refresh", refresh);
        kwargs.put("pkgs", pkgs);
        return new LocalCall<>("pkg.install", Optional.empty(), Optional.of(kwargs),
                new TypeToken<Map<String, Object>>(){});
    }
}

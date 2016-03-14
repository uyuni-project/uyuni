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
import com.suse.salt.netapi.calls.LocalCall;

import java.util.LinkedHashMap;
import java.util.Optional;

/**
 * TODO This should be removed once it has been integrated into salt-netapi-client
 */
public class SaltUtil {

    private SaltUtil() { }

    /**
     * Call 'saltutil.refresh_pillar'
     * @param refresh put 'refresh=True' in args
     * @param saltenv the saltenv in args
     * @return a {@link LocalCall}
     */
    public static LocalCall<Boolean> refreshPillar(
            Optional<Boolean> refresh, Optional<String> saltenv) {
        LinkedHashMap<String, Object> args = syncArgs(refresh, saltenv);
        return new LocalCall<>("saltutil.refresh_pillar", Optional.empty(),
                Optional.of(args), new TypeToken<Boolean>() {
        });
    }

    private static LinkedHashMap<String, Object> syncArgs(
            Optional<Boolean> refresh, Optional<String> saltenv) {
        LinkedHashMap<String, Object> args = new LinkedHashMap<>();
        refresh.ifPresent(value -> args.put("refresh", value));
        saltenv.ifPresent(value -> args.put("saltenv", value));
        return args;
    }

}

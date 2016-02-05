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

import com.suse.salt.netapi.calls.LocalCall;

import com.google.gson.reflect.TypeToken;

import java.util.LinkedHashMap;
import java.util.Optional;

/**
 * salt.modules.system
 *
 * https://docs.saltstack.com/en/latest/ref/modules/all/salt.modules.system.html
 */
public class System {

    /**
     * salt.modules.system.reboot
     * @return the result
     */
    public static LocalCall<String> reboot(Optional<Integer> at_time) {
        LinkedHashMap<String, Object> args = new LinkedHashMap<>();
        at_time.ifPresent(t -> {
            args.put("at_time", t);
        });
        return new LocalCall<>("system.reboot", Optional.empty(), Optional.of(args),
                new TypeToken<String>() { });
    }
}

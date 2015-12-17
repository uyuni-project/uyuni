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

import com.google.gson.reflect.TypeToken;
import com.suse.saltstack.netapi.calls.LocalCall;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

/**
 * salt.modules.saltutil
 */
public class SaltUtil {

    private SaltUtil() { }
    /**
     * Create a call to 'saltutil.sync_grains'
     * @return a {@link LocalCall} to pass to
     * {@link com.suse.saltstack.netapi.client.SaltStackClient}
     */
    public static LocalCall<List<String>> syncGrains() {
        LinkedHashMap<String, Object> args = new LinkedHashMap<>();
        return new LocalCall("saltutil.sync_grains", Optional.empty(),
                Optional.of(args), new TypeToken<List<String>>() {
                });
    }

}

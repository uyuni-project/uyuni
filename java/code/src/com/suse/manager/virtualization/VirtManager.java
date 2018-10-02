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
package com.suse.manager.virtualization;

import com.google.gson.reflect.TypeToken;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.salt.netapi.calls.LocalCall;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service providing utility functions to handle virtual machines.
 */
public class VirtManager {

    private static SaltService saltService = SaltService.INSTANCE;

    /**
     * Query virtual machine definition
     *
     * @param minionId the host minion ID
     * @param domainName the domain name to look for
     * @return the XML definition or an empty Optional
     */
    public static Optional<GuestDefinition> getGuestDefinition(String minionId, String domainName) {
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("vm_", domainName);
        LocalCall<String> call =
                new LocalCall<>("virt.get_xml", Optional.empty(), Optional.of(args), new TypeToken<String>() { });

        Optional<String> result = saltService.callSync(call, minionId);
        return result.map(xml -> GuestDefinition.parse(xml));
    }

    /**
     * @param saltServiceIn to set for tests
     */
    public static void setSaltService(SaltService saltServiceIn) {
        saltService = saltServiceIn;
    }

    private VirtManager() {
    }
}

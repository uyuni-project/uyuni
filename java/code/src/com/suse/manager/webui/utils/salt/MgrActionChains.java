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

package com.suse.manager.webui.utils.salt;

import com.google.gson.reflect.TypeToken;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.State;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Convenience methods to crete LocalCall objects for the mgractoinchains custom module.
 */
public class MgrActionChains {

    private MgrActionChains() { }

    /**
     * Create a LocalCall for mgractionchains.start.
     * @param actionChainId the id of the action chain to start
     * @return a LocalCall object
     */
    public static LocalCall<Map<String, State.ApplyResult>> start(long actionChainId) {
        List<String> args = new ArrayList<>(1);
        args.add(Long.toString(actionChainId));
        return new LocalCall("mgractionchains.start",
                Optional.of(args), Optional.empty(), new TypeToken<Map<String, State.ApplyResult>>() { });
    }

    /**
     * Create a LocalCall for mgractionchains.get_pending_resume.
     * @return a LocalCall object
     */
    public static LocalCall<Map<String, String>> getPendingResume() {
        return new LocalCall("mgractionchains.get_pending_resume",
                Optional.empty(), Optional.empty(), new TypeToken<Map<String, String>>() { });
    }

    /**
     * Create a LocalCall for mgractionchains.resume.
     * @return a LocalCall object
     */
    public static LocalCall<Map<String, State.ApplyResult>> resume() {
        return new LocalCall("mgractionchains.resume",
                Optional.empty(), Optional.empty(), new TypeToken<Map<String, State.ApplyResult>>() { });
    }

    /**
     * Create a LocalCall for mgractionchains.clean.
     * @return a LocalCall object
     */
    public static LocalCall<Map<String, Boolean>> clean() {
        return new LocalCall("mgractionchains.clean",
                Optional.empty(), Optional.empty(), new TypeToken<Map<String, Boolean>>() { });
    }

}

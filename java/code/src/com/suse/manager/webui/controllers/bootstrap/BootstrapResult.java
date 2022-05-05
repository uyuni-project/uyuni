/*
 * Copyright (c) 2022 SUSE LLC
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
package com.suse.manager.webui.controllers.bootstrap;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Representation of the status of bootstrap and possibly error messages.
 */
public class BootstrapResult {

    private final boolean success;
    private final String[] messages;
    private final Optional<String> contactMethod;

    /**
     * @param successIn       success
     * @param contactMethodIn contact method
     * @param messagesIn      messages
     */
    public BootstrapResult(boolean successIn, Optional<String> contactMethodIn,
                           String... messagesIn) {
        this.success = successIn;
        this.messages = messagesIn;
        this.contactMethod = contactMethodIn;
    }

    /**
     * @return success
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * @return messages
     */
    public String[] getMessages() {
        return messages;
    }

    /**
     * @return contactMethod
     */
    public Optional<String> getContactMethod() {
        return contactMethod;
    }

    /**
     * @return bootstrap result converted to a map
     */
    public Map<String, Object> asMap() {
        Map<String, Object> ret = new LinkedHashMap<>();
        ret.put("success", success);
        ret.put("messages", messages);
        return ret;
    }
}

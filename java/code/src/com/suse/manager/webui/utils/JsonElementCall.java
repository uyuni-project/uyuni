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

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.suse.salt.netapi.calls.LocalCall;

import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;

/**
 * Manipulate a given {@link LocalCall} object to return a {@link JsonElement} instead
 * of the specified return type.
 */
public class JsonElementCall extends LocalCall<JsonElement> {

    /**
     * Constructor.
     * @param call the call to manipulate.
     */
    @SuppressWarnings("unchecked")
    public JsonElementCall(LocalCall<?> call) {
        super((String) call.getPayload().get("fun"),
                ofNullable((List<?>) call.getPayload().get("arg")),
                ofNullable((Map<String, ?>) call.getPayload().get("kwarg")),
                new TypeToken<JsonElement>() { });
    }
}

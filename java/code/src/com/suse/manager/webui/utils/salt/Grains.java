/**
 * Copyright (c) 2019 SUSE LLC
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

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Optional;

/**
 * salt.modules.grains
 */
public class Grains {

    private Grains() { }


    /**
     * Helper method to get the specified grains and return result parsed into the the given type
     * @param sanitize sanitize
     * @param type  type result should be parsed into
     * @param items list of names of grains
     * @param <T> type used for TypeToken
     * @return Results parsed in the given type
     */
    public static <T> LocalCall<T>  item(boolean sanitize,
                                         TypeToken<T> type, String... items) {
        LinkedHashMap<String, Object> args = new LinkedHashMap<>();
        args.put("sanitize", sanitize);
        return new LocalCall<T>("grains.item", Optional.of(Arrays.asList(items)),
                Optional.of(args), type);
    }
}

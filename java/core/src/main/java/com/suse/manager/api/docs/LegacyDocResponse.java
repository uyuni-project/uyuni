/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.manager.api.docs;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Describes how an OpenAPI response should be rendered in the legacy API documentation.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface LegacyDocResponse {

    /**
     * @return legacy response type
     */
    String type() default "";

    /**
     * @return legacy response name
     */
    String name() default "";

    /**
     * @return optional response body type to use when rendering legacy API documentation
     */
    Class<?> responseClass() default Void.class;

    /**
     * Tells that the legacy documentation renders the return value as plain text.
     *
     * The doclet passes a return value documented without a macro through as it was written,
     * carrying neither the type role a documented one carries nor a label of its own. A return
     * value has no name in the specification, so the parsers otherwise label it with the
     * operation name, which such a return value does not show.
     *
     * @return whether the legacy documentation renders the return value as plain text
     */
    boolean plainText() default false;
}

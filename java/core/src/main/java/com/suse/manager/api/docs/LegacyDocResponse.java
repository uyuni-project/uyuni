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
}

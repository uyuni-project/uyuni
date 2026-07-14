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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import spark.route.HttpMethod;

/**
 * Annotation to provide API documentation for handler methods.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiEndpointDoc {

    /**
     * @return short endpoint summary
     */
    String summary() default "";

    /**
     * @return detailed endpoint description
     */
    String description() default "";

    /**
     * @return HTTP method exposed for the endpoint
     */
    HttpMethod method() default HttpMethod.post;

    /**
     * @return request body type
     */
    Class<?> requestClass() default Void.class;

    /**
     * @return request body description
     */
    String requestDescription() default "";

    /**
     * @return response body type
     */
    Class<?> responseClass() default Void.class;

    /**
     * @return optional legacy documentation response metadata
     */
    LegacyDocResponse legacyDocResponse() default @LegacyDocResponse;

    /**
     * @return response description
     */
    String responseDescription() default "";

    /**
     * @return whether the response is the standard integer wrapper
     */
    boolean isIntegerResponse() default false;
}

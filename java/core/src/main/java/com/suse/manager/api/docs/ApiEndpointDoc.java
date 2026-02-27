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

import io.swagger.models.HttpMethod;

/**
 * Annotation to provide API documentation for handler methods.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiEndpointDoc {

    String summary() default "";
    String description() default "";
    HttpMethod method() default HttpMethod.POST;
    Class<?> requestClass() default Void.class;
    String requestDescription() default "";
    Class<?> responseClass() default Void.class;
    String responseDescription() default "";
    boolean isIntegerResponse() default false;
}

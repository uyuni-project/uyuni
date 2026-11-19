/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

package com.suse.manager.errors;

import com.redhat.rhn.common.localization.LocalizationService;

import com.suse.common.errors.UyuniError;
import com.suse.common.errors.UyuniErrorCategory;

/**
 * A reusable kind of error: a category plus a localized message, so the same error isn't written out again
 * wherever it occurs. The message receives the offending value as {0} and the field as {1}.
 * <p>
 * Common templates are declared here; a domain can declare its own, e.g.
 * {@code new UyuniErrorTemplate(UyuniErrorCategory.ALREADY_EXISTS, "edit.channel.invalidchannellabel.labelinuse")}.
 */
public final class UyuniErrorTemplate {

    /** A required field or object is missing: "{1} is required" */
    public static final UyuniErrorTemplate REQUIRED =
            new UyuniErrorTemplate(UyuniErrorCategory.INVALID_ARGUMENT, "common.error.required");

    /** A value is not acceptable (format, length, range): "Invalid value for {1}: {0}" */
    public static final UyuniErrorTemplate ILLEGAL_VALUE =
            new UyuniErrorTemplate(UyuniErrorCategory.INVALID_ARGUMENT, "common.error.illegal_value");

    /** A referenced entity does not exist: "{1} {0} not found" */
    public static final UyuniErrorTemplate NOT_FOUND =
            new UyuniErrorTemplate(UyuniErrorCategory.NOT_FOUND, "common.error.not_found");

    /** The entity to create already exists: "{1} {0} already exists" */
    public static final UyuniErrorTemplate ALREADY_EXISTS =
            new UyuniErrorTemplate(UyuniErrorCategory.ALREADY_EXISTS, "common.error.already_exists");

    /** The entity can't be changed or deleted because others depend on it: "{1} {0} is still in use" */
    public static final UyuniErrorTemplate IN_USE =
            new UyuniErrorTemplate(UyuniErrorCategory.FAILED_PRECONDITION, "common.error.in_use");

    private final UyuniErrorCategory category;
    private final String messageKey;

    /**
     * Constructor
     * @param categoryIn the category of the errors created from this template
     * @param messageKeyIn the localization key of the message; {0} = value, {1} = field
     */
    public UyuniErrorTemplate(UyuniErrorCategory categoryIn, String messageKeyIn) {
        this.category = categoryIn;
        this.messageKey = messageKeyIn;
    }

    /**
     * Creates an error about a field, without a value (e.g. a missing field)
     * @param field the field or object the error applies to
     * @return the error, with the message localized for the current user
     */
    public UyuniError error(String field) {
        return error(field, null);
    }

    /**
     * Creates an error about a field and its offending value
     * @param field the field or object the error applies to
     * @param value the offending value, can be null
     * @return the error, with the message localized for the current user
     */
    public UyuniError error(String field, Object value) {
        // as text, so MessageFormat doesn't format ids as numbers ("1,000,010,001")
        String text = value == null ? null : String.valueOf(value);
        String message = LocalizationService.getInstance().getMessage(messageKey, text, field);
        return new UyuniError(category, field, message);
    }
}

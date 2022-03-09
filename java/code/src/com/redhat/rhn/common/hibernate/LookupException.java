/*
 * Copyright (c) 2009--2012 Red Hat, Inc.
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

package com.redhat.rhn.common.hibernate;

import com.redhat.rhn.common.db.DatabaseException;
import com.redhat.rhn.common.localization.LocalizationService;

/**
 * Thrown if the the object couldn't be found, or it multiple objects were found
 * <p>

 *
 * @version definition($Rev: 76724 $)/template($Rev: 67725 $)
 */
public class LookupException extends DatabaseException  {

    private final String localizedTitle;
    private final String localizedReason1;
    private final String localizedReason2;

    /////////////////////////
    // Constructors
    /////////////////////////
    /**
     * Constructor
     * @param message exception message
     */
    public LookupException(String message) {
        this(message, null, null, null, null);
    }

    /**
     * Constructor
     * @param message exception message
     * @param localizedTitleIn the localized title
     * @param localizedReason1In the localized first reason
     * @param localizedReason2In the localized second reason
     */
    public LookupException(String message,
                           String localizedTitleIn, String localizedReason1In, String localizedReason2In) {
        this(message, null, localizedTitleIn, localizedReason1In, localizedReason2In);
    }

    /**
     * Constructor
     * @param message exception message
     * @param cause the cause (which is saved for later retrieval
     * by the Throwable.getCause() method). (A null value is
     * permitted, and indicates that the cause is nonexistent or
     * unknown.)
     * @param localizedTitleIn the localized title
     * @param localizedReason1In the localized first reason
     * @param localizedReason2In the localized second reason
     */
    public LookupException(String message, Throwable cause,
                           String localizedTitleIn, String localizedReason1In, String localizedReason2In) {
        super(message, cause);
        localizedTitle = localizedTitleIn;
        localizedReason1 = localizedReason1In;
        localizedReason2 = localizedReason2In;
    }

    /**
     * @return Returns the localizedReason1.
     */
    public String getLocalizedReason1() {
        return localizedReason1 != null ?
                localizedReason1 : LocalizationService.getInstance().getMessage("lookup.default.reason1");
    }


    /**
     * @return Returns the localizedReason2.
     */
    public String getLocalizedReason2() {
        return localizedReason2 != null ?
                localizedReason2 : LocalizationService.getInstance().getMessage("lookup.default.reason2");
    }


    /**
     * @return Returns the localizedTitle.
     */
    public String getLocalizedTitle() {
        return localizedTitle != null ?
                localizedTitle : LocalizationService.getInstance().getMessage("lookup.default.title");
    }
}

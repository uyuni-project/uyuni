/*
 * Copyright (c) 2021 SUSE LLC
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
package com.suse.manager.errata;

import com.redhat.rhn.domain.errata.Errata;

import java.net.URI;

/**
 * Class to parse the fields of the errata and extract vendor specific information.
 */
public interface VendorSpecificErrataParser {

    /**
     * Retrieve the URI of the original vendor advisory represented by the given errata.
     *
     * @param errata the errata.
     * @return a URI representing the http address of the advisory.
     *
     * @throws ErrataParsingException if the required pieces of information are missing in the errata object.
     */
    URI getAdvisoryUri(Errata errata) throws ErrataParsingException;

    /**
     * Retrieve the vendor announcement id which can differ from the id used internally.
     *
     * @param errata the errata.
     * @return a string defining the id of the advisory announcement for the vendor.

     * @throws ErrataParsingException if the required pieces of information are missing in the errata object.
     */
    String getAnnouncementId(Errata errata) throws ErrataParsingException;
}

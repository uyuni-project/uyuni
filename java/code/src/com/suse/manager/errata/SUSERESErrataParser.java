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

import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;

/**
 * Errata parser for SUSE Expanded Support.
 */
public class SUSERESErrataParser implements VendorSpecificErrataParser {

    /**
     * {@inheritDoc}
     */
    @Override
    public URI getAdvisoryUri(Errata errata) throws ErrataParsingException {
        if (StringUtils.isEmpty(errata.getAdvisory())) {
            throw new ErrataParsingException("No advisory id found for errata");
        }

        if (errata.getAdvisory().startsWith("RH")) {
            try {
                return new URI(MessageFormat.format(RedhatErrataParser.URL_FORMAT, errata.getAdvisory()));
            }
            catch (URISyntaxException ex) {
                throw new ErrataParsingException("Unable generate vendor link for errata", ex);
            }
        }

        throw new ErrataParsingException("Unsupported advisory " + errata.getAdvisory());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAnnouncementId(Errata errata) throws ErrataParsingException {
        if (StringUtils.isEmpty(errata.getAdvisory())) {
            throw new IllegalArgumentException("No advisory id found for errata");
        }

        return errata.getAdvisory();
    }
}

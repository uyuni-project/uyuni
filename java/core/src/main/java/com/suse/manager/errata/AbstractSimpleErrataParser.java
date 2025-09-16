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
 * Base errata parser that uses a simple format where only the advisory code is substituted in the url.
 */
abstract class AbstractSimpleErrataParser implements VendorSpecificErrataParser {

    private final String urlFormat;

    /**
     * Constructor to specify the url format. The string must be valid for {@link MessageFormat#format(Object)}. Only
     * the string parameter {0} will be substituted.
     *
     * @param urlFormatIn The url of the advisory containing a single text parameter for substitution.
     */
    protected AbstractSimpleErrataParser(String urlFormatIn) {
        this.urlFormat = urlFormatIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final URI getAdvisoryUri(Errata errata) throws ErrataParsingException {
        if (StringUtils.isEmpty(errata.getAdvisory())) {
            throw new ErrataParsingException("No advisory id found for errata");
        }

        try {
            return new URI(MessageFormat.format(urlFormat, getAdvisoryCode(errata.getAdvisory())));
        }
        catch (URISyntaxException ex) {
            throw new ErrataParsingException("Unable generate vendor link for errata", ex);
        }
    }

    /**
     * Extract the advisory code from the errata. The advisory code returned by this method is used as {0} parameter in
     * the url format string.
     *
     * @param errataAdvisory The errata object. Guaranteed to be not null and not empty.
     *
     * @return the string to be used as the {0} parameter for building the url.
     */
    protected String getAdvisoryCode(String errataAdvisory) {
        return errataAdvisory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAnnouncementId(Errata errata) throws ErrataParsingException {
        if (StringUtils.isEmpty(errata.getAdvisory())) {
            throw new ErrataParsingException("No advisory id found for errata");
        }

        // Default implementation returns the internal advisory id after checking it
        return errata.getAdvisory();
    }
}

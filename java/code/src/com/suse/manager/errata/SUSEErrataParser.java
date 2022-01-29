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
import com.redhat.rhn.domain.errata.ErrataFactory;

import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.time.Year;
import java.time.ZoneOffset;
import java.util.Date;

/**
 * Parser specific for SUSE errata.
 */
public class SUSEErrataParser implements VendorSpecificErrataParser {

    private static final String ANNOUNCEMENT_FORMAT = "SUSE-{0}-{1,number,0000}:{2,number,0000}-{3,number,0}";

    private static final String URL_FORMAT = "https://www.suse.com/support/update/announcement/" +
                    "{0,number,0000}/suse-{1}-{0,number,0000}{2,number,0000}-{3,number,0}/";

    private static final String ADVISORY_CODE_SECURITY = "su";

    private static final String ADVISORY_CODE_RECOMMENDED = "ru";

    private static final int MINIMUM_PARSABLE_YEAR = 2019;

    /**
     * {@inheritDoc}
     */
    @Override
    public URI getAdvisoryUri(Errata errata) throws ErrataParsingException {
        final int year = extractYear(errata.getIssueDate());
        final int advisoryId = extractId(errata.getAdvisory());
        final String advisoryCode = extractCode(errata.getAdvisoryType());
        final long version = extractVersion(errata.getAdvisoryRel());

        try {
            return new URI(MessageFormat.format(URL_FORMAT, year, advisoryCode, advisoryId, version));
        }
        catch (URISyntaxException ex) {
            throw new ErrataParsingException("Unable generate vendor link for errata", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAnnouncementId(Errata errata) throws ErrataParsingException {
        final int year = extractYear(errata.getIssueDate());
        final int advisoryId = extractId(errata.getAdvisory());
        final String advisoryCode = extractCode(errata.getAdvisoryType()).toUpperCase();
        final long version = extractVersion(errata.getAdvisoryRel());

        return MessageFormat.format(ANNOUNCEMENT_FORMAT, advisoryCode, year, advisoryId, version);
    }

    private long extractVersion(Long advisoryRelease) throws ErrataParsingException {
        // Extract the release number of the errata
        if (advisoryRelease == null || advisoryRelease <= 0L) {
            throw new ErrataParsingException("Invalid advisory release number " + advisoryRelease);
        }

        return advisoryRelease;
    }

    private String extractCode(String advisoryType) throws ErrataParsingException {
        if (StringUtils.isEmpty(advisoryType)) {
            throw new ErrataParsingException("Advisory type is null");
        }

        // Extract the code used in the url from advisory type.
        switch (advisoryType) {
            case ErrataFactory.ERRATA_TYPE_SECURITY:
                return ADVISORY_CODE_SECURITY;
            case ErrataFactory.ERRATA_TYPE_BUG:
                return ADVISORY_CODE_RECOMMENDED;

            // TODO Optional and Feature codes are currently not supported since we have "Enhancement" for both
            default:
                throw new ErrataParsingException("Unsupported advisory type " + advisoryType);
        }

    }

    private int extractId(String advisory) throws ErrataParsingException {
        // The id is the last part of the advisoryId i.e. SUSE-15-SP3-2021-3411 or avahi-13947
        final int lastDash = advisory.lastIndexOf('-');
        if (lastDash == -1) {
            throw new ErrataParsingException("Unable to parse advisory id from " + advisory);
        }

        try {
            return Integer.parseInt(advisory.substring(lastDash + 1));
        }
        catch (NumberFormatException ex) {
            throw new ErrataParsingException("Unable to parse the advisory id number from " + advisory);
        }
    }

    private int extractYear(Date issueDate) throws ErrataParsingException {
        if (issueDate == null) {
            throw new ErrataParsingException("Issue date is null");
        }

        // Extract the year from the issue date and not from the advisory id because old ids do not include
        // the date
        final int year = Year.from(issueDate.toInstant().atZone(ZoneOffset.systemDefault())).getValue();

        // Do not parse advisory issued before 2019 because the number in the id we have in the database does not
        // match with the advisory id used in the url thus all urls we generate for those advisories do not exist.
        if (year < MINIMUM_PARSABLE_YEAR) {
            throw new ErrataParsingException("Unable to parse an advisory issued before " + MINIMUM_PARSABLE_YEAR);
        }

        return year;
    }
}

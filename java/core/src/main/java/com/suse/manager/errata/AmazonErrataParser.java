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
 * Parser specific for Amazon linux errata.
 */
public class AmazonErrataParser implements VendorSpecificErrataParser {

    private static final String ID_PREFIX = "ALAS";

    private static final String ANNOUNCEMENT_FORMAT = ID_PREFIX + "-{0}-{1}";

    private static final String URL_FORMAT = "https://alas.aws.amazon.com{0}/" + ID_PREFIX + "-{1}-{2}.html";

    private static final String VERSION_CODE = "AL";

    /**
     * {@inheritDoc}
     */
    @Override
    public URI getAdvisoryUri(Errata errata) throws ErrataParsingException {
        final ErrataParameters data = parse(errata.getAdvisory());

        try {
            return new URI(MessageFormat.format(URL_FORMAT, data.getVersionUriPrefix(), data.getYear(), data.getId()));
        }
        catch (URISyntaxException e) {
            throw new ErrataParsingException("Unable generate vendor link for errata", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAnnouncementId(Errata errata) throws ErrataParsingException {
        final ErrataParameters data = parse(errata.getAdvisory());

        return MessageFormat.format(ANNOUNCEMENT_FORMAT, data.getYear(), data.getId());
    }


    private static ErrataParameters parse(String advisoryId) throws ErrataParsingException {
        if (StringUtils.isEmpty(advisoryId)) {
            throw new ErrataParsingException("No advisory id found for errata");
        }

        final String[] parts = advisoryId.split("-");

        if (parts.length != 3) {
            throw new ErrataParsingException("Unsupported advisory format " + advisoryId);
        }

        final String versionUriPrefix;
        if (ID_PREFIX.equals(parts[0])) {
            versionUriPrefix = StringUtils.EMPTY;
        }
        else if (parts[0].startsWith(ID_PREFIX)) {
            versionUriPrefix = "/" + VERSION_CODE + parts[0].substring(4);
        }
        else {
            throw new ErrataParsingException("Unsupported advisory prefix " + parts[0]);
        }

        return new ErrataParameters(versionUriPrefix, parts[1], parts[2]);
    }

    /**
     * POJO to describe the parameters of the advisory id
     */
    private static class ErrataParameters {
        private final String versionUriPrefix;

        private final String year;

        private final String id;

        ErrataParameters(String versionUriPrefixIn, String yearIn, String idIn) {
            this.versionUriPrefix = versionUriPrefixIn;
            this.year = yearIn;
            this.id = idIn;
        }

        public String getId() {
            return id;
        }

        public String getYear() {
            return year;
        }

        public String getVersionUriPrefix() {
            return versionUriPrefix;
        }
    }
}

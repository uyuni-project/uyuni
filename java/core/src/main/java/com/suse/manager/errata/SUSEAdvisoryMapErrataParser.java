/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 *
 */
package com.suse.manager.errata;

import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.manager.errata.ErrataManager;


import com.suse.manager.errata.model.errata.ErrataAdvisoryMap;
import com.suse.manager.errata.model.errata.ErrataAdvisoryMapFactory;

import java.net.URI;
import java.net.URISyntaxException;

public class SUSEAdvisoryMapErrataParser implements VendorSpecificErrataParser {

    /**
     * default constructor
     */
    public SUSEAdvisoryMapErrataParser() {
        this(new ErrataAdvisoryMapFactory());
    }

    /**
     * constructor
     *
     * @param advisoryMapFactoryIn an instance of ErrataAdvisoryMapFactory.
     */
    public SUSEAdvisoryMapErrataParser(ErrataAdvisoryMapFactory advisoryMapFactoryIn) {
        this.advisoryMapFactory = advisoryMapFactoryIn;
    }

    private final ErrataAdvisoryMapFactory advisoryMapFactory;

    /**
     * {@inheritDoc}
     */
    @Override
    public URI getAdvisoryUri(Errata errata) throws ErrataParsingException {
        try {
            String uri = errata.getChannels().stream()
                .map(ch -> ErrataManager.getPatchId(errata, ch))
                .flatMap(s -> advisoryMapFactory.lookupByPatchId(s).stream())
                .map(ErrataAdvisoryMap::getAdvisoryUri)
                .findFirst()
                .orElseThrow(() -> new ErrataParsingException(
                        "Unable generate announcement vendor link for errata: " + errata.getAdvisory()));
            return new URI(uri);
        }
        catch (URISyntaxException ex) {
            throw new ErrataParsingException("Unable to generate vendor link for errata " + errata.getAdvisory(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAnnouncementId(Errata errata) throws ErrataParsingException {
        return errata.getChannels().stream()
                .map(ch -> ErrataManager.getPatchId(errata, ch))
                .flatMap(s -> advisoryMapFactory.lookupByPatchId(s).stream())
                .map(ErrataAdvisoryMap::getAnnouncementId)
                .findFirst()
                .orElseThrow(() -> new ErrataParsingException(
                        "Unable to generate announcement id for errata: " + errata.getAdvisory()));
    }
}

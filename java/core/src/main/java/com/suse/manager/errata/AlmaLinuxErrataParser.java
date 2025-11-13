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

/**
 * Parser specific for AlmaLinux errata.
 */
public class AlmaLinuxErrataParser extends AbstractSimpleErrataParser {

    /*
        TODO For now version 8 is hardcoded since it's not possible to figure out the distribution version.
         The errata currently does not contain any information about that. When a new version of AlmaLinux is
         released we need to review how they handle it and how we can detect the version of the distribution.
     */
    private static final String URL_FORMAT = "https://errata.almalinux.org/8/{0}.html";

    /**
     * Default constructor.
     */
    public AlmaLinuxErrataParser() {
        super(URL_FORMAT);
    }

    // AlmaLinux is using '-' in place of ':' in the url
    @Override
    protected String getAdvisoryCode(String errataAdvisory) {
        return errataAdvisory.replaceAll(":", "-");
    }
}

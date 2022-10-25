/*
 * Copyright (c) 2018 Red Hat, Inc.
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
package com.redhat.rhn.domain.channel;

import java.util.Date;

/**
 *
 *
 */
public class Modules extends RepoMetadata {
    /**
     * Create a new empty {@link Modules} instance
     */
    public Modules() { }

    /**
     * Create a new {@link Modules} instance
     * @param relativeFilename the relative filename of the module metadata file
     * @param lastModified the 'last modified date' of the module metadata file
     */
    public Modules(String relativeFilename, Date lastModified) {
        this.setRelativeFilename(relativeFilename);
        this.setLastModified(lastModified);
    }
}

/*
 * Copyright (c) 2017 SUSE LLC
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
package com.redhat.rhn.manager.content.test;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Collection;

/**
 * A DTO of an expected product, as per expected_products.csv.
 */
class ExpectedProductDto {

    /** The name. */
    private String name;

    /** The version. */
    private String version;

    /** The arch. */
    private String arch;

    /** The base. */
    private boolean base;

    /** The channel labels. */
    private Collection<String> channelLabels;

    /**
     * Instantiates a new expected product.
     *
     * @param nameIn the name
     * @param versionIn the version
     * @param archIn the arch
     * @param baseIn whether this is a base product or not
     * @param channelLabelsIn the channel labels
     */
    ExpectedProductDto(String nameIn, String versionIn, String archIn, boolean baseIn,
            Collection<String> channelLabelsIn) {
        super();
        name = nameIn;
        version = versionIn;
        arch = archIn;
        base = baseIn;
        channelLabels = channelLabelsIn;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the version.
     *
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Gets the arch.
     *
     * @return the arch
     */
    public String getArch() {
        return arch;
    }

    /**
     * Checks if is base.
     *
     * @return true, if is base
     */
    public boolean isBase() {
        return base;
    }

    /**
     * Gets the channel labels.
     *
     * @return the channel labels
     */
    public Collection<String> getChannelLabels() {
        return channelLabels;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("name", name)
            .append("version", version)
            .append("arch", arch)
            .toString();
    }
}

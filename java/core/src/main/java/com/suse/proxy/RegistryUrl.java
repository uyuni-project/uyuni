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
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.suse.proxy;

import static com.suse.utils.Predicates.isProvided;

import com.redhat.rhn.common.RhnRuntimeException;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.net.URI;
import java.net.URISyntaxException;

public class RegistryUrl {

    private final URI url;
    private String tag;

    /**
     * Constructor to create a RegistryUrlHandler instance.
     *
     * @param urlIn the input registry URL
     * @throws URISyntaxException if the URL is invalid
     */
    public RegistryUrl(String urlIn) throws URISyntaxException, RhnRuntimeException {
        if (!isProvided(urlIn)) {
            throw new RhnRuntimeException("url not provided");
        }

        this.url = new URI(normalizeRegistryUrl(urlIn));

        int colonIndex = url.getPath().lastIndexOf(":");
        if (colonIndex > 0) {
            this.tag = url.getPath().substring(colonIndex + 1);
        }
    }

    /**
     * Constructor to create a RegistryUrlHandler instance.
     *
     * @param urlIn the input registry URL
     * @param tagIn the tag
     * @throws URISyntaxException  if the URL is invalid
     * @throws RhnRuntimeException if the URL is not provided
     */
    public RegistryUrl(String urlIn, String tagIn) throws URISyntaxException, RhnRuntimeException {
        if (!isProvided(urlIn)) {
            throw new RhnRuntimeException("url not provided");
        }

        this.url = new URI(normalizeRegistryUrl(urlIn));
        this.tag = tagIn;
    }

    /**
     * Normalizes the input registry URL by:
     * - Trimming whitespace
     * - Adding "https://" if no protocol is set
     * - Removing trailing "/"
     *
     * @param registryUrlIn the input URL
     * @return the normalized URL as a string
     */
    private String normalizeRegistryUrl(String registryUrlIn) {
        registryUrlIn = registryUrlIn.trim();

        if (!registryUrlIn.matches("^[a-zA-Z][a-zA-Z0-9+.-]*://.*")) {
            registryUrlIn = "https://" + registryUrlIn;
        }

        if (registryUrlIn.endsWith("/")) {
            registryUrlIn = registryUrlIn.substring(0, registryUrlIn.length() - 1);
        }

        return registryUrlIn;
    }

    public String getDomain() {
        return url.getHost();
    }

    public String getPath() {
        return url.getPath();
    }

    public String getRegistry() {
        return url.getHost() + url.getPath();
    }

    public String getCatalogUrl() {
        return url.getScheme() + "://" + url.getHost() + "/v2/_catalog";
    }

    public String getTagListUrl() {
        return url.getScheme() + "://" + url.getHost() + "/v2" + url.getPath() + "/tags/list";
    }

    public String getUrl() {
        return url.toString();
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tagIn) {
        tag = tagIn;
    }

    @Override
    public String toString() {
        return "RegistryUrl{" +
                "url=" + url +
                ", tag='" + tag + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object oIn) {
        if (this == oIn) {
            return true;
        }

        if (oIn == null || getClass() != oIn.getClass()) {
            return false;
        }

        RegistryUrl that = (RegistryUrl) oIn;

        return new EqualsBuilder().append(url, that.url).append(tag, that.tag).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(url).append(tag).toHashCode();
    }
}

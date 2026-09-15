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

package com.suse.proxy.model;

/**
 * Class representing the proxy configuration image
 */
public class ProxyConfigImage {

    private String url;
    private String tag;

    /**
     * Default constructor
     */
    public ProxyConfigImage() {
    }

    /**
     * Constructor
     * @param urlIn the URL
     * @param tagIn the tag
     */
    public ProxyConfigImage(String urlIn, String tagIn) {
        url = urlIn;
        tag = tagIn;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String urlIn) {
        url = urlIn;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tagIn) {
        tag = tagIn;
    }
}

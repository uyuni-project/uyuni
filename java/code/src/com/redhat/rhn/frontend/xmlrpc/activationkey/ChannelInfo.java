/**
 * Copyright (c) 2016 SUSE LLC
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
package com.redhat.rhn.frontend.xmlrpc.activationkey;

/**
 * Channel info containing label, name, url and a token for
 * accessing the channel.
 */
public class ChannelInfo {

    private final String label;
    private final String name;
    private final String url;
    private final String token;

    /**
     * Constuctor
     *
     * @param labelIn channel label
     * @param nameIn channel name
     * @param urlIn channel url
     * @param tokenIn channel access token
     */
    public ChannelInfo(String labelIn, String nameIn, String urlIn, String tokenIn) {
        this.label = labelIn;
        this.name = nameIn;
        this.url = urlIn;
        this.token = tokenIn;
    }

    /**
     * Getter for channel label
     *
     * @return the channel label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Getter for channel name
     *
     * @return the channel name
     */
    public String getName() {
        return name;
    }

    /**
     * Getter for channel url
     *
     * @return the channel url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Getter for channel token
     *
     * @return the channel token
     */
    public String getToken() {
        return token;
    }
}

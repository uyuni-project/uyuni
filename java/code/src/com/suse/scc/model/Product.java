/**
 * Copyright (c) 2014 SUSE
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
package com.suse.scc.model;

/**
 * This is a SUSE product as parsed from JSON coming in from SCC.
 */
@SuppressWarnings("unused")
public class Product {

    private int id;
    private String name;
    private String identifier;
    private String version;
    private String release_type;
    private String arch;
    private String friendly_name;

    /**
     * No-args constructor
     */
    public Product() {
    }
}

/*
 * Copyright (c) 2023 SUSE LLC
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

package com.suse.oval.cpe;

/**
 * A simple and minimal CPE parser to parse CPE URI bindings included in OVAL files.
 */
public class SimpleCpeParser {


    /**
     * CPE Examples:
     * <br><br>
     * CPE: <b>cpe:/o:opensuse:leap:15.4</b>
     * <br>
     * PART: <b>o</b> <i>(it's an operating system's CPE)</i><br>
     * VENDOR: <b>opensuse</b> <i>(CPE describes a product manufactured by openSUSE)</i><br>
     * PRODUCT: <b>leap</b> <i>(The product name is leap)</i><br>
     * VERSION: <b>15.4</b> <i>(The product version is 15.4)</i><br>
     * <br><br>
     * CPE: <b>cpe:/o:suse:sle_hpc:15:sp4</b>
     * <br>
     * PART: <b>o</b> <i>(it's an operating system's CPE)</i><br>
     * VENDOR: <b>suse</b> <i>(CPE describes a product manufactured by SUSE)</i><br>
     * PRODUCT: <b>sle_hpc</b> <i>(The product name is sle_hpc)</i><br>
     * VERSION: <b>15</b> <i>(The product version is 15)</i><br>
     * UPDATE: <b>sp4</b> <i>(The product update is sp4)</i><br>
     *
     * @param cpeURI The raw CPE URI to parse
     * @return the corresponding {@link Cpe} object of the given {@code cpeURI}
     */
    public Cpe parse(String cpeURI) {
        if (!cpeURI.startsWith("cpe:/o:")) {
            throw new IllegalArgumentException("CPE is expected to be in URI format and for operating systems");
        }

        cpeURI = cpeURI.replace("cpe:/o:", "");

        String[] parts = cpeURI.split(":");

        if (parts.length < 3) {
            throw new IllegalArgumentException("CPE is expected to at least have vendor, product and version");
        }

        Cpe cpe = new Cpe();
        cpe.setVendor(parts[0]);
        cpe.setProduct(parts[1]);
        cpe.setVersion(parts[2]);

        if (parts.length > 3) {
            cpe.setUpdate(parts[3]);
        }

        return cpe;
    }
}

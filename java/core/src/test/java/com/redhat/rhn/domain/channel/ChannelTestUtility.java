/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.redhat.rhn.domain.channel;

import com.redhat.rhn.domain.rhnpackage.Package;

public class ChannelTestUtility {
    private ChannelTestUtility() {
        //Utility classes should not have a public or default constructor
    }

    public static void testAddPackage(Channel ch, Package packageIn) {
        //this method can be reached from here, since it's package-private
        ch.testAddPackage(packageIn);
    }
}

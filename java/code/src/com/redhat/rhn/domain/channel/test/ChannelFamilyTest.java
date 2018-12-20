/**
 * Copyright (c) 2009--2014 Red Hat, Inc.
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
package com.redhat.rhn.domain.channel.test;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFamily;
import com.redhat.rhn.domain.channel.ChannelFamilyFactory;
import com.redhat.rhn.domain.channel.PrivateChannelFamily;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import java.util.Set;

/**
 * ChannelFamilyTest
 */
public class ChannelFamilyTest extends BaseTestCaseWithUser {

    public void testChannelFamily() throws Exception {

        ChannelFamily cfam = ChannelFamilyFactory.
                            lookupOrCreatePrivateFamily(user.getOrg());

        //add a channel
        Channel c = ChannelFactoryTest.createTestChannel(user);
        c.setChannelFamily(cfam);

        assertEquals(c.getChannelFamilies().size(), 1);
        ChannelFamilyFactory.save(cfam);

        ChannelFamily cfam2 = c.getChannelFamily();
        assertEquals(cfam, cfam2);

        ChannelFamily cfam3 = ChannelFamilyFactory.lookupById(cfam.getId());

        assertEquals(cfam.getId(), cfam3.getId());
        assertEquals(cfam.getLabel(), cfam3.getLabel());
        assertEquals(cfam.getName(), cfam3.getName());
        assertEquals(cfam.getOrg(), cfam3.getOrg());
    }

    /**
     * Utility method that ensures that a channel family exists.
     * @param user the user
     * @param label the label
     * @return the channel family
     * @throws Exception the exception if anything goes wrong
     */
    public static ChannelFamily ensureChannelFamilyExists(User user, String label)
        throws Exception {
        ChannelFamily cf = ChannelFamilyFactory.lookupByLabel(label, null);
        if (cf == null) {
            cf = ChannelFamilyFactoryTest.createTestChannelFamily(user, true,
                    TestUtils.randomString());
            cf.setName(label);
            cf.setLabel(label);
            ChannelFamilyFactory.save(cf);
        }
        return cf;
    }

    /**
     * Utility method that ensures that a channel family has maximum members.
     * @param channelFamily the channel family
     */
    public static void ensurePrivateChannelFamilyExists(User user, ChannelFamily channelFamily) {
        Set<PrivateChannelFamily> families = channelFamily.getPrivateChannelFamilies();

        if (families.size() == 0) {
            PrivateChannelFamily privateChannelFamily = new PrivateChannelFamily();
            privateChannelFamily.setOrg(user.getOrg());
            privateChannelFamily.setChannelFamily(channelFamily);
            channelFamily.addPrivateChannelFamily(privateChannelFamily);

            HibernateFactory.getSession().save(privateChannelFamily);

        }
    }
}

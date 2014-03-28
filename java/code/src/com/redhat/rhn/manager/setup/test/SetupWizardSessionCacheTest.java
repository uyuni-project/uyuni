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

package com.redhat.rhn.manager.setup.test;

import com.redhat.rhn.manager.setup.MirrorCredentialsDto;
import com.redhat.rhn.manager.setup.SetupWizardSessionCache;
import com.redhat.rhn.manager.setup.SubscriptionDto;
import com.redhat.rhn.testing.RhnMockStrutsTestCase;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Tests for {@link SetupWizardSessionCache}.
 */
public class SetupWizardSessionCacheTest extends RhnMockStrutsTestCase {

    // Internally used credentials and subscriptions
    private MirrorCredentialsDto creds = getTestCredentials();
    private MirrorCredentialsDto creds2 = getTestCredentials2();
    private List<SubscriptionDto> subs = getTestSubscriptions();

    public void testProxyStatus() {
        SetupWizardSessionCache.storeProxyStatus(true, request);
        assertTrue(SetupWizardSessionCache.getProxyStatus(false, request));
        SetupWizardSessionCache.storeProxyStatus(false, request);
        assertFalse(SetupWizardSessionCache.getProxyStatus(false, request));
    }

    public void testGetSubscriptionsNull() {
        assertNull(SetupWizardSessionCache.getSubscriptions(creds, request));
    }

    public void testCredentialsStatusTrue() {
        assertTrue(SetupWizardSessionCache.credentialsStatusUnknown(creds, request));
    }

    public void testCredentialsStatusFalse() {
        SetupWizardSessionCache.storeSubscriptions(subs, creds, request);
        assertFalse(SetupWizardSessionCache.credentialsStatusUnknown(creds, request));
    }

    public void testGetSubscriptions() {
        SubscriptionDto subscription = subs.get(0);
        SetupWizardSessionCache.storeSubscriptions(subs, creds, request);
        List<SubscriptionDto> cachedSubs = SetupWizardSessionCache.getSubscriptions(creds, request);
        assertEquals(1, cachedSubs.size());
        assertEquals(cachedSubs.get(0), subscription);
    }

    public void testClearSubscriptions() {
        // Store subscriptions for different credentials
        SetupWizardSessionCache.storeSubscriptions(subs, creds, request);
        SetupWizardSessionCache.storeSubscriptions(subs, creds2, request);

        // Retrieve subscriptions
        List<SubscriptionDto> subs = SetupWizardSessionCache.getSubscriptions(creds, request);
        assertNotNull(subs);
        subs = SetupWizardSessionCache.getSubscriptions(creds2, request);
        assertNotNull(subs);

        // Clear only for creds
        SetupWizardSessionCache.clearSubscriptions(creds, request);
        assertNull(SetupWizardSessionCache.getSubscriptions(creds, request));
        assertNotNull(SetupWizardSessionCache.getSubscriptions(creds2, request));
    }

    public void testClearAllSubscriptions() {
        // Store subscriptions for different credentials
        SetupWizardSessionCache.storeSubscriptions(subs, creds, request);
        SetupWizardSessionCache.storeSubscriptions(subs, creds2, request);

        // Retrieve subscriptions
        List<SubscriptionDto> subs = SetupWizardSessionCache.getSubscriptions(creds, request);
        assertNotNull(subs);
        subs = SetupWizardSessionCache.getSubscriptions(creds2, request);
        assertNotNull(subs);

        // Clear all
        SetupWizardSessionCache.clearAllSubscriptions(request);
        assertNull(SetupWizardSessionCache.getSubscriptions(creds, request));
        assertNull(SetupWizardSessionCache.getSubscriptions(creds2, request));
    }

    private MirrorCredentialsDto getTestCredentials() {
        return new MirrorCredentialsDto("foo", "foo", "foo");
    }

    private MirrorCredentialsDto getTestCredentials2() {
        return new MirrorCredentialsDto("bar", "bar", "bar");
    }

    private List<SubscriptionDto> getTestSubscriptions() {
        List<SubscriptionDto> ret = new ArrayList<SubscriptionDto>();
        ret.add(getTestSubscriptionDto());
        return ret;
    }

    private SubscriptionDto getTestSubscriptionDto() {
        SubscriptionDto ret = new SubscriptionDto();
        ret.setName("foobar-subscription");
        ret.setStartDate(new Date());
        ret.setEndDate(new Date());
        return ret;
    }
}

/*
 * Copyright (c) 2014 SUSE LLC
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

    /**
     * Tests getProxyStatus().
     */
    public void testProxyStatus() {
        SetupWizardSessionCache.storeProxyStatus(true, request);
        assertTrue(SetupWizardSessionCache.getProxyStatus(false, request));
        SetupWizardSessionCache.storeProxyStatus(false, request);
        assertFalse(SetupWizardSessionCache.getProxyStatus(false, request));
    }

    /**
     * Tests getSubscriptions().
     */
    public void testGetSubscriptionsNull() {
        assertNull(SetupWizardSessionCache.getSubscriptions(creds, request));
    }

    /**
     * Tests credentialsStatusUnknown().
     */
    public void testCredentialsStatusTrue() {
        assertTrue(SetupWizardSessionCache.credentialsStatusUnknown(creds, request));
    }

    /**
     * Tests storeSubscriptions().
     */
    public void testCredentialsStatusFalse() {
        SetupWizardSessionCache.storeSubscriptions(subs, creds, request);
        assertFalse(SetupWizardSessionCache.credentialsStatusUnknown(creds, request));
    }

    /**
     * Tests getSubscriptions().
     */
    public void getSubscriptions() {
        SubscriptionDto subscription = subs.get(0);
        SetupWizardSessionCache.storeSubscriptions(subs, creds, request);
        List<SubscriptionDto> cachedSubs =
                SetupWizardSessionCache.getSubscriptions(creds, request);
        assertEquals(1, cachedSubs.size());
        assertEquals(cachedSubs.get(0), subscription);
    }

    /**
     * Tests clearSubscriptions().
     */
    public void testClearSubscriptions() {
        // Store subscriptions for different credentials
        SetupWizardSessionCache.storeSubscriptions(subs, creds, request);
        SetupWizardSessionCache.storeSubscriptions(subs, creds2, request);

        // Retrieve subscriptions
        List<SubscriptionDto> subs2 =
                SetupWizardSessionCache.getSubscriptions(creds, request);
        assertNotNull(subs2);
        subs2 = SetupWizardSessionCache.getSubscriptions(creds2, request);
        assertNotNull(subs2);

        // Clear only for creds
        SetupWizardSessionCache.clearSubscriptions(creds, request);
        assertNull(SetupWizardSessionCache.getSubscriptions(creds, request));
        assertNotNull(SetupWizardSessionCache.getSubscriptions(creds2, request));
    }

    /**
     * Tests clearAllSubscriptions().
     */
    public void testClearAllSubscriptions() {
        // Store subscriptions for different credentials
        SetupWizardSessionCache.storeSubscriptions(subs, creds, request);
        SetupWizardSessionCache.storeSubscriptions(subs, creds2, request);

        // Retrieve subscriptions
        List<SubscriptionDto> subs2 =
                SetupWizardSessionCache.getSubscriptions(creds, request);
        assertNotNull(subs2);
        subs2 = SetupWizardSessionCache.getSubscriptions(creds2, request);
        assertNotNull(subs2);

        // Clear all
        SetupWizardSessionCache.clearAllSubscriptions(request);
        assertNull(SetupWizardSessionCache.getSubscriptions(creds, request));
        assertNull(SetupWizardSessionCache.getSubscriptions(creds2, request));
    }

    /**
     * Gets the test credentials.
     *
     * @return the test credentials
     */
    private MirrorCredentialsDto getTestCredentials() {
        return new MirrorCredentialsDto("foo", "foo");
    }

    /**
     * Gets the test credentials2.
     *
     * @return the test credentials2
     */
    private MirrorCredentialsDto getTestCredentials2() {
        return new MirrorCredentialsDto("bar", "bar");
    }

    /**
     * Gets the test subscriptions.
     *
     * @return the test subscriptions
     */
    private List<SubscriptionDto> getTestSubscriptions() {
        List<SubscriptionDto> ret = new ArrayList<>();
        ret.add(getTestSubscriptionDto());
        return ret;
    }

    /**
     * Gets the test subscription dto.
     *
     * @return the test subscription dto
     */
    private SubscriptionDto getTestSubscriptionDto() {
        SubscriptionDto ret = new SubscriptionDto();
        ret.setName("foobar-subscription");
        ret.setStartDate(new Date());
        ret.setEndDate(new Date());
        return ret;
    }
}

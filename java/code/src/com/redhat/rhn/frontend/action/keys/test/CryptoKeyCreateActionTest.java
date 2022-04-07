/*
 * Copyright (c) 2009--2012 Red Hat, Inc.
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
package com.redhat.rhn.frontend.action.keys.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.kickstart.crypto.CryptoKey;
import com.redhat.rhn.domain.kickstart.crypto.test.CryptoTest;
import com.redhat.rhn.frontend.action.keys.CryptoKeyCreateAction;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.testing.RhnPostMockStrutsTestCase;
import com.redhat.rhn.testing.TestUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * CryptoKeyCreateActionTest
 */
public class CryptoKeyCreateActionTest extends RhnPostMockStrutsTestCase {

    @BeforeEach
    public void setUp() throws Exception {
        TestUtils.disableLocalizationLogging();
        super.setUp();
    }

    @Test
    public void testExecute() throws Exception {
        setRequestPathInfo("/keys/CryptoKeyCreate");
        addRequestParameter(CryptoKeyCreateAction.SUBMITTED, Boolean.FALSE.toString());
        actionPerform();
        assertNotNull(request.getAttribute(CryptoKeyCreateAction.KEY));
        assertNotNull(request.getAttribute(CryptoKeyCreateAction.TYPES));
    }

    @Test
    public void testCreateSubmit() throws Exception {
        setRequestPathInfo("/keys/CryptoKeyCreate");
        addRequestParameter(CryptoKeyCreateAction.SUBMITTED, Boolean.TRUE.toString());
        addRequestParameter(CryptoKeyCreateAction.DESCRIPTION, "somedesc");
        addRequestParameter(CryptoKeyCreateAction.TYPE,
                KickstartFactory.KEY_TYPE_GPG.getLabel());
        addUploadedFile(CryptoKeyCreateAction.CONTENTS, "somekey", "");
        actionPerform();
        assertNotNull(request.getAttribute(CryptoKeyCreateAction.KEY));
        String[] keys = {"crypto.key.nokey"};
        verifyActionErrors(keys);
    }

    @Test
    public void testEdit() throws Exception {
        setRequestPathInfo("/keys/CryptoKeyEdit");
        addRequestParameter(CryptoKeyCreateAction.SUBMITTED, Boolean.TRUE.toString());
        addRequestParameter(CryptoKeyCreateAction.SUBMITTED, Boolean.TRUE.toString());
        addRequestParameter(CryptoKeyCreateAction.DESCRIPTION, "somedesc");
        addRequestParameter(CryptoKeyCreateAction.TYPE,
                KickstartFactory.KEY_TYPE_GPG.getLabel());
        CryptoKey key = CryptoTest.createTestKey(user.getOrg());
        KickstartFactory.saveCryptoKey(key);
        TestUtils.flushAndEvict(key);
        addRequestParameter(RequestContext.KEY_ID,
                key.getId().toString());
        addUploadedFile(CryptoKeyCreateAction.CONTENTS, "somekey", "test key content");
        actionPerform();
        verifyNoActionErrors();
    }
}


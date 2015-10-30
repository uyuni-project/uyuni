package com.suse.manager.webui.controllers.test;

import com.mockobjects.servlet.MockHttpServletRequest;
import com.mockobjects.servlet.MockHttpSession;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.test.PackageTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.TestUtils;
import com.suse.manager.webui.controllers.DownloadController;

import static com.redhat.rhn.testing.ErrataTestUtils.createTestChannel;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestPackage;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestUser;

/**
 * Created by duncan on 10/30/15.
 */
public class DownloadControllerTest extends RhnBaseTestCase {


    public void testFoo() throws Exception {
        assertTrue(true);

        User user = createTestUser();
        Channel channel = createTestChannel(user);
        Package pkg = createTestPackage(user, channel, "noarch");

        assertEquals("", pkg.getFile());

        /*
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(new MockHttpSession());
        request.setupGetRequestURI("http://localhost:8080");
        request.setupGetMethod("POST");

        PackageTest.createTestPackage()


        DownloadController.downloadPackage()
        */
    }
}

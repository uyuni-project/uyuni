/**
 * Copyright (c) 2017 SUSE LLC
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
package com.redhat.rhn.frontend.xmlrpc.image.test;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.image.ImageInfo;
import com.redhat.rhn.domain.image.ImageOverview;
import com.redhat.rhn.domain.image.ImageProfile;
import com.redhat.rhn.domain.image.ImageStore;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.ScheduledAction;
import com.redhat.rhn.frontend.xmlrpc.image.ImageInfoHandler;
import com.redhat.rhn.frontend.xmlrpc.image.profile.test.ImageProfileHandlerTest;
import com.redhat.rhn.frontend.xmlrpc.test.BaseHandlerTestCase;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.testing.TestUtils;

import org.apache.commons.lang.RandomStringUtils;

import java.util.List;


public class ImageInfoHandlerTest extends BaseHandlerTestCase {

    private ImageInfoHandler handler = new ImageInfoHandler();

    public final void testScheduleImageBuild() throws Exception {
        MinionServer server = MinionServerFactoryTest.createTestMinionServer(admin);
        SystemManager.entitleServer(server, EntitlementManager.CONTAINER_BUILD_HOST);
        ImageStore store = ImageProfileHandlerTest.createImageStore("registry.reg", admin);
        ActivationKey ak = ImageProfileHandlerTest.createActivationKey(admin);
        ImageProfile prof = ImageProfileHandlerTest.createImageProfile(admin, store, ak);

        DataResult dr = ActionManager.recentlyScheduledActions(admin, null, 30);
        int preScheduleSize = dr.size();

        long ret = handler.scheduleImageBuild(admin, prof.getLabel(), "1.0.0",
                server.getId(), getNow());
        assertTrue(ret > 0);

        dr = ActionManager.recentlyScheduledActions(admin, null, 30);
        assertEquals(1, dr.size() - preScheduleSize);
        assertEquals("Build an Image Profile", ((ScheduledAction)dr.get(0)).getTypeName());
    }

    public final void testListImages() throws Exception {
        ImageStore store = ImageProfileHandlerTest.createImageStore("registry.reg", admin);
        ImageInfo inf1 = createImageInfo(admin, "1.0.0", store);
        ImageInfo inf2 = createImageInfo(admin, "2.0.0", store);

        List<ImageInfo> listInfo = handler.listImages(admin);
        assertEquals(2, listInfo.size());
    }

    public final void testGetImageDetails() throws Exception {
        ImageStore store = ImageProfileHandlerTest.createImageStore("registry.reg", admin);
        ImageInfo inf1 = createImageInfo(admin, "1.0.0", store);

        ImageOverview imageOverview = handler.getDetails(admin, inf1.getId());
        assertEquals(inf1.getVersion(), imageOverview.getVersion());
    }

    public static ImageInfo createImageInfo(User user, String version, ImageStore store) {
        ImageInfo inf = new ImageInfo();
        inf.setName("image-" + RandomStringUtils.randomAscii(10));
        inf.setVersion(version);
        inf.setChecksum(null);
        inf.setImageArch(ServerFactory.lookupServerArchByLabel("x86_64-redhat-linux"));
        inf.setOrg(user.getOrg());
        inf.setStore(store);
        return TestUtils.saveAndReload(inf);
    }
}

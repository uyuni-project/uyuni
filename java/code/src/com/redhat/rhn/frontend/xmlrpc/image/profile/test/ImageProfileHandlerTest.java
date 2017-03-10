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
package com.redhat.rhn.frontend.xmlrpc.image.profile.test;

import com.redhat.rhn.domain.image.ImageProfile;
import com.redhat.rhn.frontend.xmlrpc.image.profile.ImageProfileHandler;
import com.redhat.rhn.frontend.xmlrpc.test.BaseHandlerTestCase;

import java.util.List;

public class ImageProfileHandlerTest extends BaseHandlerTestCase {

    private ImageProfileHandler handler = new ImageProfileHandler();

    public void testListImageProfileTypes() throws Exception {
        List<String> types = handler.listImageProfileTypes(admin);
        assertFalse("No image profile types found", types.isEmpty());
        assertEquals(ImageProfile.TYPE_DOCKERFILE, types.get(0));
    }

    public void testCreateImageProfileFailed() throws Exception {
        try {
            handler.create(admin, "newprofile", "container", "mystore",
                    "/path/to/dockerfile/", "1-conti");
            fail("Invalid type provided");
        }
        catch (IllegalArgumentException ignore) {
            assertEquals("type does not exist.", ignore.getMessage());
        }

        try {
            handler.create(admin, "newprofile", "dockerfile", "mystore",
                    "/path/to/dockerfile/", "1-conti");
            fail("Invalid store provided");
        }
        catch (IllegalArgumentException ignore) {
            assertEquals("image store does not exist.", ignore.getMessage());
        }
        
        
}
}

/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.frontend.action.kickstart.tree;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.manager.kickstart.tree.TreeCreateOperation;
import com.redhat.rhn.testing.ChannelTestUtils;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;

import org.apache.struts.action.DynaActionForm;
import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Tests the install type resolution of the tree create/edit form: an
 * unknown or missing install type label must be reported as a validation
 * error instead of silently leaving the tree without an install type
 * (which used to NPE later in the base-path validation).
 */
public class BaseTreeActionTest extends JMockBaseTestCaseWithUser {

    private static final String LABEL = "installtypeguardtree";
    private static final String BASE_PATH = "/tmp/kickstart/installtypeguardtree";
    private static final String INVALID_INSTALL_TYPE = "bogus-install-type";

    private BaseTreeAction action;
    private DynaActionForm form;
    private HttpServletRequest request;

    @BeforeEach
    public void setUp() throws Exception {
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
        action = new TreeCreateAction();
        form = mock(DynaActionForm.class);
        request = mock(HttpServletRequest.class);
        Channel channel = ChannelTestUtils.createTestChannel(user);
        checking(new Expectations() {{
            allowing(form).getString(BaseTreeAction.LABEL);
            will(returnValue(LABEL));
            allowing(form).getString(BaseTreeAction.BASE_PATH);
            will(returnValue(BASE_PATH));
            allowing(form).get(BaseTreeAction.CHANNEL_ID);
            will(returnValue(channel.getId()));
            allowing(form).getString(BaseTreeAction.KERNEL_OPTS);
            will(returnValue(""));
            allowing(form).getString(BaseTreeAction.POST_KERNEL_OPTS);
            will(returnValue(""));
        }});
    }

    /**
     * An unknown install type label is rejected with a validation error
     * and the tree is left without an install type.
     */
    @Test
    public void testUnknownInstallTypeRejected() {
        checking(new Expectations() {{
            allowing(form).getString(BaseTreeAction.INSTALL_TYPE);
            will(returnValue(INVALID_INSTALL_TYPE));
        }});

        ValidatorError error = action.processCommandSetters(
                new TreeCreateOperation(user), form, request);

        assertEquals("kickstart.tree.invalidinstalltype", error.getKey());
    }

    /**
     * A missing install type form value is rejected like an unknown label.
     */
    @Test
    public void testMissingInstallTypeRejected() {
        checking(new Expectations() {{
            allowing(form).getString(BaseTreeAction.INSTALL_TYPE);
            will(returnValue(null));
        }});

        ValidatorError error = action.processCommandSetters(
                new TreeCreateOperation(user), form, request);

        assertEquals("kickstart.tree.invalidinstalltype", error.getKey());
    }

    /**
     * A valid install type label is resolved and set on the tree together
     * with the other form values.
     */
    @Test
    public void testValidInstallTypeAccepted() {
        checking(new Expectations() {{
            allowing(form).getString(BaseTreeAction.INSTALL_TYPE);
            will(returnValue("opensuse16.1"));
            allowing(request).getLocalName();
            will(returnValue("localhost"));
        }});

        TreeCreateOperation operation = new TreeCreateOperation(user);
        ValidatorError error = action.processCommandSetters(operation, form, request);

        assertNull(error);
        assertEquals("opensuse16.1",
                operation.getTree().getInstallType().getLabel());
        assertEquals(LABEL, operation.getTree().getLabel());
        assertEquals(BASE_PATH, operation.getTree().getBasePath());
    }
}

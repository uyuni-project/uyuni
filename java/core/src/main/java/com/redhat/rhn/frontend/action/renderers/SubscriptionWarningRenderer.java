/*
 * Copyright (c) 2022 SUSE LLC
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

package com.redhat.rhn.frontend.action.renderers;

import com.redhat.rhn.domain.notification.types.SubscriptionWarning;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.listview.PageControl;

import javax.servlet.http.HttpServletRequest;

/**
 * Renders YourRhn fragment for tasks
 *
 */
public class SubscriptionWarningRenderer extends BaseFragmentRenderer {

    private static final String SUBSCRIPTION_WARNING = "subscriptionwarning";
    private SubscriptionWarning sw = new SubscriptionWarning();

    /**
     * {@inheritDoc}
     */
    @Override
    protected void render(User user, PageControl pc, HttpServletRequest request) {
        request.setAttribute(SUBSCRIPTION_WARNING, sw.expiresSoon());
        RendererHelper.setTableStyle(request, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPageUrl() {
        return "/WEB-INF/pages/common/fragments/yourrhn/subwarn.jsp";
    }
}

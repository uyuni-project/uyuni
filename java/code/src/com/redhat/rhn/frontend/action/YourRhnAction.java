/*
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
package com.redhat.rhn.frontend.action;

import com.redhat.rhn.domain.notification.types.SubscriptionWarning;
import com.redhat.rhn.domain.user.Pane;
import com.redhat.rhn.domain.user.PaneFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.action.renderers.CriticalSystemsRenderer;
import com.redhat.rhn.frontend.action.renderers.FragmentRenderer;
import com.redhat.rhn.frontend.action.renderers.InactiveSystemsRenderer;
import com.redhat.rhn.frontend.action.renderers.LatestErrataRenderer;
import com.redhat.rhn.frontend.action.renderers.PendingActionsRenderer;
import com.redhat.rhn.frontend.action.renderers.RecentSystemsRenderer;
import com.redhat.rhn.frontend.action.renderers.SubscriptionWarningRenderer;
import com.redhat.rhn.frontend.action.renderers.SystemGroupsRenderer;
import com.redhat.rhn.frontend.action.renderers.TasksRenderer;
import com.redhat.rhn.frontend.listview.PageControl;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.frontend.struts.RhnHelper;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * YourRhnAction
 */
public class YourRhnAction extends RhnAction {

    public static final String ANY_LISTS_SELECTED = "anyListsSelected";


    /**
     * No-arg constructor
     */
    public YourRhnAction() {
        Map renderers = new HashMap();

        SubscriptionWarning sw = new SubscriptionWarning();
        List tasks = Arrays.asList(Pane.ALL_PANES);
        for (Object taskIn : tasks) {
            String key = (String) taskIn;
            FragmentRenderer renderer = null;
            if (key.equals(Pane.TASKS)) {
                renderer = new TasksRenderer();
            }
            else if (key.equals(Pane.CRITICAL_SYSTEMS)) {
                renderer = new CriticalSystemsRenderer();
            }
            else if (key.equals(Pane.INACTIVE_SYSTEMS)) {
                renderer = new InactiveSystemsRenderer();
            }
            else if (key.equals(Pane.LATEST_ERRATA)) {
                renderer = new LatestErrataRenderer();
            }
            else if (key.equals(Pane.PENDING_ACTIONS)) {
                renderer = new PendingActionsRenderer();
            }
            else if (key.equals(Pane.RECENTLY_REGISTERED_SYSTEMS)) {
                renderer = new RecentSystemsRenderer();
            }
            else if (key.equals(Pane.SYSTEM_GROUPS)) {
                renderer = new SystemGroupsRenderer();
            }
            else if (key.equals(Pane.TASKS)) {
                renderer = new TasksRenderer();
            }
            else if (key.equals(Pane.SUBSCRIPTION_WARNING) && sw.expiresSoon()) {
                renderer = new SubscriptionWarningRenderer();
            }
            if (renderer != null) {
                renderers.put(key, renderer);
            }
        }
    }

    /** {@inheritDoc} */
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response) {
        RequestContext ctx = new RequestContext(request);
        User user = ctx.getCurrentUser();
        Map panes = getDisplayPanes(user);
        boolean anyListsSelected = false;

        PageControl pc = new PageControl();
        pc.setStart(1);
        pc.setPageSize(5);

        if (panes != null && panes.size() > 0) {
            anyListsSelected = true;
            for (Object oIn : panes.keySet()) {
                String key = (String) oIn;
                key = formatKey(key);
                request.setAttribute(key, "y");
            }
        }
        request.setAttribute(ANY_LISTS_SELECTED, anyListsSelected);
        request.setAttribute("legends", "yourrhn");
        return mapping.findForward(RhnHelper.DEFAULT_FORWARD);
    }

    private String formatKey(String key) {
        String[] parts = key.split("\\-");
        if (parts.length < 2) {
            return key;
        }
        StringBuilder buf = new StringBuilder();
        buf.append(parts[0].toLowerCase());
        for (int x = 1; x < parts.length; x++) {
            String s = parts[x].substring(0, 1);
            String r = parts[x].substring(1);
            buf.append(s.toUpperCase()).append(r.toLowerCase());
        }
        return buf.toString();
    }

    private Map getDisplayPanes(User user) {
        Map panes = PaneFactory.getAllPanes();
        Set hiddenPanes = user.getHiddenPanes();
        Map mergedPanes = new HashMap();

        for (Object oIn : panes.values()) {
            Pane pane = (Pane) oIn;
            if (!hiddenPanes.contains(pane)) {
                Pane actualPane = (Pane) panes.get(pane.getLabel());
                if (actualPane.isValidFor(user)) {
                    mergedPanes.put(actualPane.getLabel(), actualPane);
                }
            }
        }
        return mergedPanes;
    }
}

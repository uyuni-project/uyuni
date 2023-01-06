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

import com.redhat.rhn.domain.user.Pane;
import com.redhat.rhn.domain.user.PaneFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.listview.PageControl;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.frontend.struts.RhnHelper;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * YourRhnAction
 */
public class YourRhnAction extends RhnAction {

    public static final String ANY_LISTS_SELECTED = "anyListsSelected";


    /** {@inheritDoc} */
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest request, HttpServletResponse response) {
        RequestContext ctx = new RequestContext(request);
        User user = ctx.getCurrentUser();
        Map<String, Pane> panes = getDisplayPanes(user);
        boolean anyListsSelected = false;

        PageControl pc = new PageControl();
        pc.setStart(1);
        pc.setPageSize(5);

        if (!panes.isEmpty()) {
            anyListsSelected = true;
            for (String key : panes.keySet()) {
                request.setAttribute(formatKey(key), "y");
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

    private Map<String, Pane> getDisplayPanes(User user) {
        Map<String, Pane> panes = PaneFactory.getAllPanes();
        Set<Pane> hiddenPanes = user.getHiddenPanes();
        Map<String, Pane> mergedPanes = new HashMap<>();

        for (Pane pane : panes.values()) {
            if (!hiddenPanes.contains(pane)) {
                Pane actualPane = panes.get(pane.getLabel());
                if (actualPane.isValidFor(user)) {
                    mergedPanes.put(actualPane.getLabel(), actualPane);
                }
            }
        }
        return mergedPanes;
    }
}

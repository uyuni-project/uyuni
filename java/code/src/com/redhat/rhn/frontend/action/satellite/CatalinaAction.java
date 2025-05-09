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

package com.redhat.rhn.frontend.action.satellite;

import com.redhat.rhn.common.util.FileUtils;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.frontend.struts.RhnHelper;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Struts action class showing logfile in web UI.
 */
public class CatalinaAction extends RhnAction {

    private static final String LOGFILE_PATH = "/var/log/rhn/rhn_web_ui.log";

    /** {@inheritDoc} */
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm formIn,
                                 HttpServletRequest request, HttpServletResponse response) {
        request.setAttribute("logfile_path", LOGFILE_PATH);
        String contents = FileUtils.getTailOfFile(LOGFILE_PATH, 1000);
        contents = StringEscapeUtils.escapeHtml4(contents);
        request.setAttribute("contents", contents);
        return mapping.findForward(RhnHelper.DEFAULT_FORWARD);
    }
}

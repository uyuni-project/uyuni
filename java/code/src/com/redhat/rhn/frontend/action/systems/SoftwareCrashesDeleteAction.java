/**
 * Copyright (c) 2013--2014 Red Hat, Inc.
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
package com.redhat.rhn.frontend.action.systems;

import com.redhat.rhn.domain.server.Crash;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.manager.rhnpackage.PackageManager;
import com.redhat.rhn.manager.system.CrashManager;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * SoftwareCrashesDeleteAction
 * @version $Rev$
 */
public class SoftwareCrashesDeleteAction extends RhnAction {

    public static final String CRASH_ID = "crid";
    public static final String CRASH = "crash";
    public static final String CRASH_NEVRA = "crash_nevra";
    public static final String SID = "sid";


    /** {@inheritDoc} */
    public ActionForward execute(ActionMapping mapping,
                                  ActionForm formIn,
                                  HttpServletRequest request,
                                  HttpServletResponse response) {

        RequestContext ctx = new RequestContext(request);
        User user = ctx.getCurrentUser();
        ctx.lookupAndBindServer();
        ctx.copyParamToAttributes(SID);

        Long crashId = ctx.getParamAsLong(CRASH_ID);
        request.setAttribute(CRASH_ID, crashId);
        Crash crash = CrashManager.lookupCrashByUserAndId(user, crashId);
        request.setAttribute(CRASH, crash);
        request.setAttribute(CRASH_NEVRA,
                PackageManager.buildPackageNevra(crash.getPackageNameId(),
                        crash.getPackageEvrId(), crash.getPackageArchId()));

        if (ctx.isSubmitted()) {
            CrashManager.deleteCrash(user, crashId);
            createSuccessMessage(request, "message.crashdeleted", crash.getCrash());
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("sid", crash.getServer().getId());
            return getStrutsDelegate().forwardParams(mapping.findForward("delete"),
                    params);
        }

        return mapping.findForward(RhnHelper.DEFAULT_FORWARD);
    }
}

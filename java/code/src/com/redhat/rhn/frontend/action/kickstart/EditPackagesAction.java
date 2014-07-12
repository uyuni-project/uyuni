/**
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
package com.redhat.rhn.frontend.action.kickstart;

import com.redhat.rhn.domain.kickstart.KickstartData;
import com.redhat.rhn.domain.kickstart.KickstartPackage;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.rhnpackage.PackageName;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.manager.kickstart.KickstartEditCommand;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Manages displaying/updating package names
 * associated with Kickstarts
 *
 * @version $Rev $
 */
public class EditPackagesAction extends RhnAction {

    private static final String PACKAGE_LIST = "packageList";
    private static final String NO_BASE = "noBase";
    private static final String IGNORE_MISSING = "ignoreMissing";

    /**
     * {@inheritDoc}
     */
    public ActionForward execute(ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        DynaActionForm dynaForm = (DynaActionForm) form;
        RequestContext ctx = new RequestContext(request);
        KickstartEditCommand cmd = new KickstartEditCommand(
                ctx.getRequiredParam(RequestContext.KICKSTART_ID),
                ctx.getCurrentUser());
        KickstartData ksdata = cmd.getKickstartData();
        request.setAttribute(RequestContext.KICKSTART, ksdata);
        if (isSubmitted(dynaForm)) {
            ActionForward returnForward = save(mapping, dynaForm, request, response,
                    ctx, ksdata);
            addMessage(request, "kickstart.edit.pkgs.updated");
            return returnForward;
        }
        return display(mapping, dynaForm, request, response, ctx, ksdata);
    }

    /**
     * Handles form submission and database update
     * @param mapping from Struts
     * @param form from Struts
     * @param request from Struts
     * @param response from Struts
     * @param ctx RequestContext corresponding to the request
     * @param ksdata KickstartData
     * @return pointer to jsp page
     * @throws Exception signalling error
     */
    public ActionForward save(ActionMapping mapping,
            DynaActionForm form,
            HttpServletRequest request,
            HttpServletResponse response,
            RequestContext ctx,
            KickstartData ksdata) throws Exception {
        transferEdits(ksdata, form,  ctx);
        StringBuilder redirectUrl = new StringBuilder();
        redirectUrl.append(request.getContextPath());
        redirectUrl.append(mapping.getPath());
        redirectUrl.append(".do?ksid=").append(form.get("ksid"));
        response.sendRedirect(redirectUrl.toString());
        return null;
    }

    /**
     * Handles display
     * @param mapping from Struts
     * @param form from Struts
     * @param request from Struts
     * @param response from Struts
     * @param ctx RequestContext corresponding to the request
     * @param ksdata KickstartData
     * @return pointer to jsp page
     * @throws Exception signalling error
     */
    public ActionForward display(ActionMapping mapping,
            DynaActionForm form,
            HttpServletRequest request,
            HttpServletResponse response,
            RequestContext ctx,
            KickstartData ksdata) throws Exception {
        prepareForm(ksdata, form);
        return mapping.findForward("display");
    }

    private void prepareForm(KickstartData ksdata, DynaActionForm form) {
        Set ksPackages = ksdata.getKsPackages();
        if (ksPackages != null && ksPackages.size() > 0) {
            StringBuilder buf = new StringBuilder();
            for (Iterator iter = ksPackages.iterator(); iter.hasNext();) {
                KickstartPackage pn = (KickstartPackage)iter.next();
                buf.append(pn.getPackageName().getName());
                buf.append("\n");
            }
            form.set(PACKAGE_LIST, buf.toString());
        }

        form.set(NO_BASE, ksdata.getNoBase());
        form.set(IGNORE_MISSING, ksdata.getIgnoreMissing());
        form.set("submitted", Boolean.TRUE);
    }

    private void transferEdits(KickstartData ksdata, DynaActionForm form,
            RequestContext ctx) {

        ksdata.setNoBase(Boolean.TRUE.equals(form.get(NO_BASE)));
        ksdata.setIgnoreMissing(Boolean.TRUE.equals(form.get(IGNORE_MISSING)));

        // first clear the kickstart packages set
        ksdata.clearKsPackages();
        Set ksPackages = ksdata.getKsPackages();

        String newPackages = form.getString(PACKAGE_LIST);
        if (newPackages != null && newPackages.length() > 0) {
            Boolean first = Boolean.TRUE;
            for (StringTokenizer strtok = new StringTokenizer(newPackages, "\n");
                    strtok.hasMoreTokens();) {

                // This is a hack but I can't think of a better way to do it
                String pkg = null;
                if (first && (!ksdata.getNoBase())) {
                    pkg = "@ Base";
                    PackageName pn = PackageFactory.lookupOrCreatePackageByName(pkg);
                    KickstartPackage kp = new KickstartPackage(ksdata, pn);
                    if (KickstartFactory.lookupKsPackageByKsDataAndPackageName(
                                                            ksdata, pn).isEmpty()) {
                        ksdata.addKsPackage(kp);
                    }
                }
                first = false;

                pkg = strtok.nextToken();
                pkg = pkg.trim();
                if (pkg.length() == 0) {
                    continue;
                }

                // if noBase is checked and the current package is @base, ignore it
                PackageName pn = PackageFactory.lookupOrCreatePackageByName(pkg);
                if ((pn.getName().toLowerCase().replaceAll("\\s", "")
                        .equals("@base")) && (ksdata.getNoBase())) {
                    continue;
                }

                KickstartPackage kp = new KickstartPackage(ksdata, pn);
                if (KickstartFactory.lookupKsPackageByKsDataAndPackageName(
                                                        ksdata, pn).isEmpty()) {
                    ksdata.addKsPackage(kp);
                }
            }
        }
        KickstartFactory.saveKickstartData(ksdata);
    }
}

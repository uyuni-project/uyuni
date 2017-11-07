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
package com.redhat.rhn.frontend.action.rhnpackage;

import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.rhnpackage.PackageSource;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.action.common.BadParameterException;
import com.redhat.rhn.frontend.dto.PackageListItem;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.manager.download.DownloadManager;
import com.redhat.rhn.manager.rhnpackage.PackageManager;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * ChannelPackagesAction
 * @version $Rev$
 */
public class PackageDetailsAction extends RhnAction {

    private final String PACKAGE_NAME = "package_name";
    private final String PACKAGE_KEY = "package_key";

    /** {@inheritDoc} */
    public ActionForward execute(ActionMapping mapping,
            ActionForm formIn,
            HttpServletRequest request,
            HttpServletResponse response) {

        RequestContext requestContext = new RequestContext(request);
        User user = requestContext.getCurrentUser();

        //If this is an easy one and we have the pid
        if (request.getParameter("pid") != null) {
            long pid = requestContext.getRequiredParam("pid");
            Package pkg = PackageFactory.lookupByIdAndUser(pid, user);

            // show permission error if pid is invalid like we did before
            if (pkg == null) {
                throw new PermissionException("Invalid pid");
            }

            request.setAttribute("type", "rpm");
            request.setAttribute(PACKAGE_NAME, pkg.getFilename());
            if (!pkg.getPackageKeys().isEmpty()) {
                request.setAttribute(PACKAGE_KEY, pkg.getPackageKeys().iterator().next()
                        .getKey());
            }
            boolean isDebugPackage = pkg.getPackageName().getName().contains("debuginfo") ||
                    pkg.getPackageName().getName().contains("debugsource");

            request.setAttribute("isDebugPackage", isDebugPackage);
            if (!isDebugPackage) {
                Package debugInfoPkg = PackageManager.findDebugInfo(user, pkg);
                String ftpUrl = PackageManager.generateFtpDebugPath(pkg);
                if (debugInfoPkg != null) {
                    request.setAttribute("debugInfoUrl", DownloadManager
                            .getPackageDownloadPath(debugInfoPkg, user));
                }
                else if (ftpUrl != null) {
                    request.setAttribute("debugInfoUrl", ftpUrl);
                    request.setAttribute("debugInfoFtp", true);
                }
                Package debugSourcePkg = PackageManager.findDebugSource(user, pkg);
                if (debugSourcePkg != null) {
                    request.setAttribute("debugSourceUrl", DownloadManager
                            .getPackageDownloadPath(debugSourcePkg, user));
                }
            }


            if (DownloadManager.isFileAvailable(pkg.getPath())) {
                request.setAttribute("url",
                        DownloadManager.getPackageDownloadPath(pkg, user));
            }

            List<PackageSource> src = PackageFactory.lookupPackageSources(pkg);

            if (!src.isEmpty() && DownloadManager.isFileAvailable(src.get(0).getPath())) {
                request.setAttribute("srpm_url",
                   DownloadManager.getPackageSourceDownloadPath(pkg, src.get(0), user));
                request.setAttribute("srpm_path", src.get(0).getFile());
            }

            // remove references to channels we can't see
            Set<Channel> channels = new HashSet<Channel>(pkg.getChannels());
            channels.retainAll(ChannelFactory.getAccessibleChannelsByOrg(user.getOrg()
                    .getId()));
            request.setAttribute("channels", channels);

            request.setAttribute("pack", pkg);
            // description can be null.
            if (pkg.getDescription() != null) {
                String description = StringEscapeUtils.escapeHtml(pkg.getDescription());
                request.setAttribute("description", description.replace("\n", "<BR>\n"));
            }
            else {
                request.setAttribute("description",
                        pkg.getDescription());
            }
            request.setAttribute("packArches",
                    PackageFactory.findPackagesWithDifferentArch(pkg));
            request.setAttribute("pid", pid);

            request.setAttribute("erratumEmpty", pkg.getPublishedErrata().isEmpty());
            request.setAttribute("erratum", pkg.getPublishedErrata());

            return mapping.findForward(RhnHelper.DEFAULT_FORWARD);
        }
        PackageListItem item = PackageListItem.parse(request.getParameter("id_combo"));
        Package pkg;
        long nameId = item.getIdOne();
        long evrId = item.getIdTwo();
        long archId = 0;
        if (item.getIdThree() != null) {
            archId = item.getIdThree();
        }

        Long cid = requestContext.getParamAsLong("cid");
        Long sid = requestContext.getParamAsLong("sid");
        if (cid != null) {
            pkg = PackageManager.guestimatePackageByChannel(
               cid, nameId, evrId, user.getOrg());

        }
        else if (sid != null) {
            pkg = PackageManager.guestimatePackageBySystem(
               sid, nameId, evrId, archId, user.getOrg());

        }
        else {
            throw new BadParameterException("pid, cid, or sid");
        }

        // show permission error if pid is invalid like we did before
        if (pkg == null) {
            throw new PermissionException("Invalid id_combo and cid/sid");
        }

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("pid", pkg.getId());
        return getStrutsDelegate().forwardParams(mapping.findForward("package"),
                params);
    }
}


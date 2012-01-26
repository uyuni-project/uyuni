/**
 * Copyright (c) 2012 Novell
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
package com.redhat.rhn.frontend.action.systems.images;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.image.Image;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.action.renderers.ImagesRenderer;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.frontend.taglibs.list.ListTagHelper;
import com.redhat.rhn.frontend.taglibs.list.helper.ListHelper;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.system.SystemManager;

/**
 * This action will present the user with a list of available images
 * and allow one to be selected for provisioning.
 */
public class ScheduleImageDeploymentAction extends RhnAction {

    private static final String SUCCESS_KEY = "studio.deployment.scheduled";

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public ActionForward execute(ActionMapping actionMapping,
                                 ActionForm actionForm,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
        throws Exception {
        // The id of the current server and submitted flag
        Long sid = null;
        Boolean submitted = false;

        // Form parameters
        Long vcpus = null;
        Long memkb = null;
        String bridge = null;
        String proxyServer = null;
        String proxyUser = null;
        String proxyPass = null;

    	// Read parameters from the form
        if (actionForm instanceof DynaActionForm) {
            DynaActionForm form = (DynaActionForm) actionForm;
            sid = (Long) form.get("sid");

            // Read submitted
            submitted = (Boolean) form.get("submitted");
            submitted = submitted ==  null ? false : submitted;

            if (submitted) {
                // Get all form parameters
                vcpus = (Long) form.get("vcpus");
                memkb = (Long) form.get("mem_mb") * 1024;
                bridge = (String) form.getString("bridge");
                proxyServer = (String) form.getString("proxy_server");
                proxyUser = (String) form.getString("proxy_user");
                proxyPass = (String) form.getString("proxy_pass");
            }
        }

        // Get the current user
        RequestContext ctx = new RequestContext(request);
        User user = ctx.getLoggedInUser();

        ActionForward forward;
        if (submitted) {
            // Schedule image deployment
            String buildId = ListTagHelper.getRadioSelection(ListHelper.LIST, request);

            // Get the images from the session and find the selected one
            List<Image> images = (List<Image>) request.getSession().getAttribute(
                    ImagesRenderer.IMAGES_LIST);
            request.getSession().removeAttribute(ImagesRenderer.IMAGES_LIST);
            Image image = null;
            for (Image i : images) {
                if (i.getBuildId().equals(new Long(buildId))) {
                    image = i;
                    break;
                }
            }

            // Set up the proxy configuration
            ProxyConfig proxy = null;
            if (proxyServer != null) {
                proxy = new ProxyConfig(proxyServer, proxyUser, proxyPass);
            }

        	// Create the action and store it
            Action deploy = ActionManager.createDeployImageAction(
                    user, image, vcpus, memkb, bridge, proxy);
            ActionManager.addServerToAction(sid, deploy);
            ActionManager.storeAction(deploy);
            // Put a success message to the request
            createSuccessMessage(request, SUCCESS_KEY, image.getName());

            // Forward the sid as a request parameter
            Map forwardParams = makeParamMap(request);
            forwardParams.put("sid", sid);
            forward = getStrutsDelegate().forwardParams(
                    actionMapping.findForward("success"), forwardParams);
        } else {
            // Put the server to the request (needed for system header)
            Server server = SystemManager.lookupByIdAndUser(sid, user);
            request.setAttribute("system", server);

            forward = actionMapping.findForward("default");
        }
        return forward;
    }
}

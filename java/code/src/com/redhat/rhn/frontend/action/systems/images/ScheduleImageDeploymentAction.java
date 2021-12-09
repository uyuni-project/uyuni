/**
 * Copyright (c) 2012 SUSE LLC
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

import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.image.ProxyConfig;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.system.SystemManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This action will schedule image deployment for an image given by URL.
 */
public class ScheduleImageDeploymentAction extends RhnAction {

    private static final String SUCCESS_KEY = "images.message.success.scheduled";

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public ActionForward execute(ActionMapping actionMapping,
            ActionForm actionForm, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        // Get the current user
        RequestContext ctx = new RequestContext(request);
        User user = ctx.getCurrentUser();

        // Put the server object to the request (for system header)
        Long sid = Long.valueOf(request.getParameter(RequestContext.SID));
        Server server = SystemManager.lookupByIdAndUser(sid, user);
        request.setAttribute("system", server);

        ActionForward forward;
        if (request.getParameter(RequestContext.DISPATCH) != null) {
            // Read the form parameters
            DynaActionForm form = (DynaActionForm) actionForm;
            Long vcpus = (Long) form.get("vcpus");
            Long memkb = (Long) form.get("mem_mb") * 1024;
            String bridge = form.getString("bridge");
            String proxyServer = form.getString("proxy_server");
            String proxyUser = form.getString("proxy_user");
            String proxyPass = form.getString("proxy_pass");

            // Set up the proxy configuration
            ProxyConfig proxy = null;
            if (StringUtils.isNotEmpty(proxyServer)) {
                proxy = new ProxyConfig(proxyServer, proxyUser, proxyPass);
            }

            // Put defaults for deployment parameters
            if (vcpus <= 0) {
                vcpus = Long.valueOf(1);
            }
            if (memkb <= 0) {
                memkb = Long.valueOf(524288);
            }

            // Create the action and store it
            String imageUrl = form.getString("image_url");

            if (StringUtils.isEmpty(imageUrl)) {
                createErrorMessage(request, "images.jsp.error.noimage", null);
                forward = actionMapping.findForward(RhnHelper.DEFAULT_FORWARD);
            }
            else {
                Action action = ActionManager.createDeployImageAction(user, imageUrl,
                        vcpus, memkb, bridge, proxy);
                ActionManager.addServerToAction(sid, action);
                ActionManager.storeAction(action);
                createSuccessMessage(request, SUCCESS_KEY, imageUrl);

                // Forward the sid as a request parameter
                Map<String, Object> forwardParams = makeParamMap(request);
                forwardParams.put(RequestContext.SID, sid);
                forward = getStrutsDelegate().forwardParams(
                        actionMapping.findForward("submitted"), forwardParams);
            }
        }
        else {
            forward = actionMapping.findForward(RhnHelper.DEFAULT_FORWARD);
        }
        return forward;
    }
}

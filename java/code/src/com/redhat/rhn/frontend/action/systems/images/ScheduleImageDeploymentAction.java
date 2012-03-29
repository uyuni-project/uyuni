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

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.image.Image;
import com.redhat.rhn.domain.image.ProxyConfig;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.action.renderers.ImagesRenderer;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.frontend.taglibs.list.ListTagHelper;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.system.SystemManager;

/**
 * This action will present the user with a list of available images
 * and allow one to be selected for provisioning.
 */
public class ScheduleImageDeploymentAction extends RhnAction {

    private static final String SUCCESS_KEY = "images.message.success.scheduled";

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public ActionForward execute(ActionMapping actionMapping,
            ActionForm actionForm, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        // Deployment parameters
        Long vcpus = null;
        Long memkb = null;
        String bridge = null;
        String proxyServer = null;
        String proxyUser = null;
        String proxyPass = null;

        // Read form parameters if we are 'dispatched'
        Boolean dispatched = request.getParameter(RequestContext.DISPATCH) != null;
        if (dispatched && actionForm instanceof DynaActionForm) {
            DynaActionForm form = (DynaActionForm) actionForm;
            vcpus = (Long) form.get("vcpus");
            memkb = (Long) form.get("mem_mb") * 1024;
            bridge = (String) form.getString("bridge");
            proxyServer = (String) form.getString("proxy_server");
            proxyUser = (String) form.getString("proxy_user");
            proxyPass = (String) form.getString("proxy_pass");
        }

        // Get the current user
        RequestContext ctx = new RequestContext(request);
        User user = ctx.getLoggedInUser();

        // Put the server to the request (needed for system header)
        Long sid = new Long(request.getParameter(RequestContext.SID));
        Server server = SystemManager.lookupByIdAndUser(sid, user);
        request.setAttribute("system", server);

        // Can be used to detect if we are currently clearing the filter
        Boolean filterCleared = request.getParameter("image_id") != null;

        ActionForward forward;
        if (dispatched) {
            String imageId = request.getParameter("image_id");
            Image image = findImage(new Long(imageId), request);

            // Set up the proxy configuration
            ProxyConfig proxy = null;
            if (StringUtils.isNotEmpty(proxyServer)) {
                proxy = new ProxyConfig(proxyServer, proxyUser, proxyPass);
            }

            // Put defaults for deployment parameters
            if (StringUtils.isEmpty(bridge)) {
                bridge = "br0";
            }
            if (vcpus <= 0) {
                vcpus = Long.valueOf(1);
            }
            if (memkb <= 0) {
                memkb = Long.valueOf(524288);
            }

            // Create the action and store it
            Action action = ActionManager.createDeployImageAction(user, image,
                    vcpus, memkb, bridge, proxy);
            ActionManager.addServerToAction(sid, action);
            ActionManager.storeAction(action);
            createSuccessMessage(request, SUCCESS_KEY, image.getName());

            // Forward the sid as a request parameter
            Map forwardParams = makeParamMap(request);
            forwardParams.put("sid", sid);
            forward = getStrutsDelegate().forwardParams(
                    actionMapping.findForward("submitted"), forwardParams);
        }
        else if (ctx.isSubmitted() || filterCleared) {
            // The "parentUrl" is needed for rl:listset
            request.setAttribute(ListTagHelper.PARENT_URL,
                    request.getRequestURI());
            forward = actionMapping.findForward("default");
        }
        else {
            // The page is called for the first time
            forward = actionMapping.findForward("first");
        }
        return forward;
    }

    /**
     * Get the list of images from the session and find the selected one.
     * @param imageId
     * @param request
     * @return
     */
    private Image findImage(Long imageId, HttpServletRequest request) {
        @SuppressWarnings("unchecked")
        List<Image> images = (List<Image>) request.getSession().getAttribute(
                ImagesRenderer.ATTRIB_IMAGES_LIST);
        Image image = null;
        for (Image i : images) {
            if (i.getId().equals(imageId)) {
                image = i;
                break;
            }
        }
        // Clear images from memory if image was found
        if (image != null) {
            request.getSession().removeAttribute(ImagesRenderer.ATTRIB_IMAGES_LIST);
        }
        return image;
    }
}

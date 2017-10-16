/**
 * Copyright (c) 2017 SUSE LLC
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

package com.suse.manager.webui.controllers;

import com.redhat.rhn.domain.rhnset.RhnSet;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.struts.StrutsDelegate;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;
import com.redhat.rhn.manager.rhnset.RhnSetManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.suse.manager.webui.controllers.utils.ContactMethodUtil;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.utils.FlashScopeHelper;
import com.suse.manager.webui.utils.gson.JsonResult;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import spark.Request;
import spark.Response;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.suse.manager.webui.utils.SparkApplicationHelper.json;

/**
 * Controller class providing backend code for the systems page.
 */
public class SystemsController {

    private SystemsController() { }

    // Logger for this class
    private static final Logger LOG = Logger.getLogger(SystemsController.class);

    /**
     * Deletes a system.
     * @param request the request
     * @param response the reposen
     * @param user the user
     * @return the json response
     */
    public static String delete(Request request, Response response, User user) {
        String sidStr = request.params("sid");
        String noclean = request.queryParams("nocleanup");
        long sid;
        try {
            sid = Long.parseLong(sidStr);
        }
        catch (NumberFormatException e) {
            return json(response, HttpStatus.SC_BAD_REQUEST, JsonResult.success());
        }
        Server server = ServerFactory.lookupById(sid);

        boolean sshPush = Stream.of(
                ServerFactory.findContactMethodByLabel(ContactMethodUtil.SSH_PUSH),
                ServerFactory.findContactMethodByLabel(ContactMethodUtil.SSH_PUSH_TUNNEL)
        ).anyMatch(cm -> server.getContactMethod().equals(cm));

        if (server.asMinionServer().isPresent() && sshPush) {
            if (!Boolean.parseBoolean(noclean)) {
                Optional<List<String>> cleanupErr =
                        SaltService.INSTANCE.
                                cleanupSSHMinion(server.asMinionServer().get(), 300);
                if (cleanupErr.isPresent()) {
                    return json(response, JsonResult.error(cleanupErr.get()));
                }
            }
        }

        if (server.hasEntitlement(EntitlementManager.MANAGEMENT)) {
            // But what if this system is in some other user's RhnSet???
            RhnSet set = RhnSetDecl.SYSTEMS.get(user);

            // Remove from SSM if required
            if (set.getElementValues().contains(sid)) {
                set.removeElement(sid);
                RhnSetManager.store(set);
            }
        }

        try {
            // Now we can remove the system
            SystemManager.deleteServer(user, sid);
            createSuccessMessage(request.raw(), "message.serverdeleted.param",
                    Long.toString(sid));
        }
        catch (RuntimeException e) {
            if (e.getMessage().contains("cobbler")) {
                createErrorMessage(request.raw(), "message.servernotdeleted_cobbler",
                        Long.toString(sid));
            }
            else {
                createErrorMessage(request.raw(),
                        "message.servernotdeleted", Long.toString(sid));
                throw e;
            }
        }
        FlashScopeHelper.flash(request, "Deleted successfully");
        return json(response, JsonResult.success());
    }

    protected static void createSuccessMessage(HttpServletRequest req, String msgKey,
                                        String param1) {
        ActionMessages msg = new ActionMessages();
        Object[] args = new Object[1];
        args[0] = StringEscapeUtils.escapeHtml4(param1);
        msg.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(msgKey, args));
        StrutsDelegate.getInstance().saveMessages(req, msg);
    }

    protected static void createErrorMessage(HttpServletRequest req, String beanKey,
                                      String param) {
        ActionErrors errs = new ActionErrors();
        String escParam = StringEscapeUtils.escapeHtml4(param);
        errs.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(beanKey, escParam));
        StrutsDelegate.getInstance().saveMessages(req, errs);
    }



}

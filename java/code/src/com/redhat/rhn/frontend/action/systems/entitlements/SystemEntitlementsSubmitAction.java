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
package com.redhat.rhn.frontend.action.systems.entitlements;

import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.validator.ValidatorResult;
import com.redhat.rhn.domain.entitlement.Entitlement;
import com.redhat.rhn.domain.rhnset.RhnSet;
import com.redhat.rhn.domain.rhnset.RhnSetElement;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.action.common.BaseSetOperateOnSelectedItemsAction;
import com.redhat.rhn.frontend.dto.SystemOverview;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.frontend.struts.StrutsDelegate;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.action.DynaActionForm;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Class representing the submit action of the System Entitlements page
 */
public class SystemEntitlementsSubmitAction extends
                BaseSetOperateOnSelectedItemsAction {

    private static Logger log = LogManager.getLogger(SystemEntitlementsSubmitAction.class);

    public static final String KEY_ADD_ENTITLED =
        "systementitlements.jsp.add_entitlement";
    public static final String KEY_REMOVE_ENTITLED =
        "systementitlements.jsp.remove_entitlement";

    private static SystemEntitlementManager systemEntitlementManager = GlobalInstanceHolder.SYSTEM_ENTITLEMENT_MANAGER;

    /**
     * {@inheritDoc}
     */
    @Override
    protected RhnSetDecl getSetDecl() {
        return RhnSetDecl.SYSTEM_ENTITLEMENTS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataResult<SystemOverview> getDataResult(User user,
            ActionForm formIn,
            HttpServletRequest request) {
        return SystemManager.getSystemEntitlements(user, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void processMethodKeys(Map<String, String> map) {
        map.put(KEY_ADD_ENTITLED, "processAdd");
        map.put(KEY_REMOVE_ENTITLED, "processRemove");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void processParamMap(ActionForm formIn, HttpServletRequest request,
            Map<String, Object> params) {
    }

    private Entitlement findAddOnEntitlement(ActionForm formIn) {
        DynaActionForm form = (DynaActionForm) formIn;
        String entType = form.getString(SystemEntitlementsSetupAction.ADDON_ENTITLEMENT);

        Entitlement ent = EntitlementManager.getByName(entType);
        return (ent == null || ent.isBase()) ? null : ent;
    }

    /**
     *
     * @param mapping ActionMapping
     * @param formIn ActionForm
     * @param request ServletRequest
     * @param response ServletResponse
     * @return The ActionForward to go to next.
     */
    public ActionForward processAdd(ActionMapping mapping, ActionForm formIn,
            HttpServletRequest request, HttpServletResponse response) {
        return operateAddOnEntitlements(mapping, formIn, request, response, true);
    }

    /**
     *
     * @param mapping ActionMapping
     * @param formIn ActionForm
     * @param request ServletRequest
     * @param response ServletResponse
     * @return The ActionForward to go to next.
     */
    public ActionForward processRemove(ActionMapping mapping,
            ActionForm formIn, HttpServletRequest request,
            HttpServletResponse response) {
        return operateAddOnEntitlements(mapping, formIn, request, response, false);
    }


    /**
     * Execute some operation on the set of selected items.  Forwards
     * to the "default"
     * NOTE:  Must define StringResource for failure and success messages:
     * getSetName() + ".success" for providing a parameterized
     * message to the UI that would say "2 ServerProbe Suite(s) deleted."
     *
     * @param mapping ActionMapping
     * @param formIn ActionForm
     * @param request ServletRequest
     * @param response ServletResponse
     * @param add Add/remove entitlement..
     * @return The ActionForward to go to next.
     */
    public ActionForward operateAddOnEntitlements(ActionMapping mapping,
            ActionForm formIn,
            HttpServletRequest request,
            HttpServletResponse response,
            boolean add) {
        log.debug("operateAddOnEntitlements");

        RhnSet set = updateSet(request);

        //if they chose no probe suites, return to the same page with a message
        if (set.isEmpty()) {
            return handleEmptySelection(mapping, formIn, request);
        }

        Map<String, Object> params = makeParamMap(formIn, request);
        RequestContext rctx = new RequestContext(request);
        User user = rctx.getCurrentUser();

        int successCount = 0;
        int failureCount = 0;

        StrutsDelegate strutsDelegate = getStrutsDelegate();

        Entitlement ent = findAddOnEntitlement(formIn);

        //Go through the set of systems to which we should add the entitlement
        for (RhnSetElement element : set.getElements()) {
            Long sid = element.getElement();
            Server server = ServerFactory.lookupByIdAndOrg(sid, user.getOrg());
            //We are adding the add on entitlement
            if (add) {
                //if the system already has the entitlement, do nothing
                //  if so, neither success nor failure count will be updated.
                if (!server.hasEntitlement(ent)) {
                    if (systemEntitlementManager.canEntitleServer(server, ent)) {
                        log.debug("we can entitle.  Lets entitle to : {}", ent);
                            ValidatorResult vr = systemEntitlementManager.addEntitlementToServer(server, ent);
                        log.debug("entitleServer.VE: {}", vr.getMessage());
                            if (vr.getErrors().size() > 0) {
                                failureCount++;
                            }
                            else {
                                successCount++;
                            }
                        }
                        else {
                            log.debug("canEntitleServer returned false.");
                            //invalid entitlement
                            failureCount++;
                        }
                } //if has entitlement
            } //if add
            //We are removing the add on entitlement
            else {
                if (server.hasEntitlement(ent)) {
                    log.debug("removing entitlement");
                    systemEntitlementManager.removeServerEntitlement(server, ent);
                    successCount++;
                }
            } //else

        } //for

        ActionMessages msg = new ActionMessages();

        String prefix = getSetDecl().getLabel() + ".addon";
        if (!add) {
            log.debug("adding remove success message");
            //Create the remove success message.
            Object[] args = new Object[] {String.valueOf(successCount),
                    ent.getHumanReadableLabel()};
            ActionMessage m = new ActionMessage(prefix + ".removed.success", args);
            msg.add(ActionMessages.GLOBAL_MESSAGE, m);
        }
        else {
            if (log.isDebugEnabled()) {
                log.debug("successCount: {} failureCount: {}", successCount, failureCount);
            }
            //Create the 'added entitlements' success message
            if (successCount > 0 && failureCount == 0) {
                log.debug("adding success msg");
                Object[] args = new Object[] {String.valueOf(successCount),
                        ent.getHumanReadableLabel()};
                ActionMessage m = new ActionMessage(prefix + ".success", args);
                msg.add(ActionMessages.GLOBAL_MESSAGE, m);
            }

            //Create the 'invalid entitlement' failure message
            if (failureCount > 0) {
                Object[] args = new Object[] {String.valueOf(failureCount),
                        ent.getHumanReadableLabel()};
                ActionMessage m = new ActionMessage(prefix + ".error", args);
                msg.add(ActionMessages.GLOBAL_MESSAGE, m);
            }
        }

        strutsDelegate.saveMessages(request, msg);
        return strutsDelegate.forwardParams(mapping.findForward(
                RhnHelper.DEFAULT_FORWARD), params);
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    protected void processMessage(ActionMessages msg,
            String methodName,
            long successCount,
            long failureCount) {

        Object[] args = new Object [] {String.valueOf(successCount),
                String.valueOf(failureCount)
        };

        if (failureCount > 0) {
            addToMessage(msg, methodName, false, args);
        }
        else  if (successCount > 0) {
            addToMessage(msg, methodName, true, args);
        }
    }

    /**
     *
     * @return  empty selection message
     */
    @Override
    protected ActionMessage getEmptySelectionMessage() {
        return new ActionMessage("system_entitlements.emptyselectionerror");
    }
}

/*
 * Copyright (c) 2010--2014 Red Hat, Inc.
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
package com.redhat.rhn.frontend.action.systems.duplicate;

import static com.redhat.rhn.manager.user.UserManager.ensureRoleBasedAccess;

import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.access.Namespace;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.frontend.dto.SystemCompareDto;
import com.redhat.rhn.frontend.dto.SystemOverview;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.frontend.taglibs.list.helper.ListSessionSetHelper;
import com.redhat.rhn.frontend.taglibs.list.helper.Listable;
import com.redhat.rhn.manager.system.SystemManager;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * DuplicateSystemsCompareAction
 */
public class DuplicateSystemsCompareAction extends RhnAction implements Listable<SystemOverview> {
    public static final String KEY = "key";
    public static final String KEY_TYPE = "key_type";
    private static final int MAX_LIMIT = 3;

    private final SystemManager systemManager;

    /**
     * Constructor
     */
    public DuplicateSystemsCompareAction() {
        systemManager = GlobalInstanceHolder.SYSTEM_MANAGER;
    }

    /**
     * Constructor
     *
     * @param systemManagerIn the system manager
     */
    public DuplicateSystemsCompareAction(SystemManager systemManagerIn) {
        systemManager = systemManagerIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm formIn,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {
        RequestContext context = new RequestContext(request);

        Map<String, Object> params = new HashMap<>();
        params.put(KEY, context.getRequiredParamAsString(KEY));
        params.put(KEY_TYPE, context.getRequiredParamAsString(KEY_TYPE));
        request.setAttribute("maxLimit", MAX_LIMIT);
        ListSessionSetHelper helper = new ListSessionSetHelper(this, request, params);

        if (!context.isSubmitted()) {
            List<SystemOverview> result = getResult(context);
            Set<String> preSelect = new HashSet<>();
            for (int i = 0; i < Math.min(MAX_LIMIT, result.size()); i++) {
                preSelect.add(result.get(i).getId().toString());
            }
            helper.preSelect(preSelect);
        }

        helper.execute();
        if (context.isSubmitted()) {
            boolean resync = false;
            for (Iterator<String> itr = helper.getSet().iterator(); itr.hasNext();) {
                String sid = itr.next();
                if (request.getParameter("btn" + sid) != null) {
                    ensureRoleBasedAccess(context.getCurrentUser(), "systems.details.delete", Namespace.AccessMode.W);
                    Long id = Long.valueOf(sid);
                    Server server = SystemManager.lookupByIdAndUser(id,
                                                    context.getCurrentUser());
                    String name = server.getName();
                    server = null;
                    systemManager.deleteServer(context.getCurrentUser(), id);
                    getStrutsDelegate().saveMessage("message.serverdeleted.param",
                                                    new String[] {name}, request);
                    itr.remove();
                    resync = true;
                }
                else if (sid.equals(context
                        .getParam("removedServerId", false))) {
                    itr.remove();
                    resync = true;
                }
            }
            if (resync) {
                helper.execute();
            }
        }
        if (helper.getSet().size() > MAX_LIMIT) {
            LocalizationService ls = LocalizationService.getInstance();
            ActionErrors errors = new ActionErrors();
            getStrutsDelegate().addError(errors,
                            "duplicate.compares.max_limit.message",
                    String.valueOf(MAX_LIMIT),  ls.getMessage("Refresh Comparison"));
            getStrutsDelegate().saveMessages(request, errors);
        }
        else {
            List<Long> sids = new LinkedList<>();
            for (String sid : helper.getSet()) {
                sids.add(Long.valueOf(sid));
            }
            List<Server> systems = SystemManager.
            hydrateServerFromIds(sids, context.getCurrentUser());
            request.setAttribute("systems",
                    new SystemCompareDto(systems, context.getCurrentUser()));
        }


        return mapping.findForward(RhnHelper.DEFAULT_FORWARD);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SystemOverview> getResult(RequestContext contextIn) {
        String key = contextIn.getRequiredParamAsString(KEY);
        String keyType = contextIn.getRequiredParamAsString(KEY_TYPE);
        if (DuplicateSystemsAction.HOSTNAME.equals(keyType)) {
            return SystemManager.listDuplicatesByHostname
                                (contextIn.getCurrentUser(), key);
        }
        else if (DuplicateSystemsAction.MAC_ADDRESS.equals(keyType)) {
            return SystemManager.listDuplicatesByMac(contextIn.getCurrentUser(), key);
        }
        else if (DuplicateSystemsAction.IPV6.equals(keyType)) {
            return SystemManager.listDuplicatesByIPv6(contextIn.getCurrentUser(), key);
        }
        return SystemManager.listDuplicatesByIP(contextIn.getCurrentUser(), key);
    }
}

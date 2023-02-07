/*
 * Copyright (c) 2009--2018 Red Hat, Inc.
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
/*
 * Copyright (c) 2010 SUSE LLC
 */
package com.redhat.rhn.frontend.action.errata;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.errata.Bug;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.ErrataFactory;
import com.redhat.rhn.domain.errata.Severity;
import com.redhat.rhn.domain.rhnset.RhnSet;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.action.common.RhnSetAction;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnValidationHelper;
import com.redhat.rhn.frontend.struts.StrutsDelegate;
import com.redhat.rhn.manager.errata.ErrataManager;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.action.DynaActionForm;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * CreateAction
 */
public class CreateAction extends RhnSetAction {

    /** This class reuses code in the channel assignment page */
    private ChannelAction channelAction = new ChannelAction();

    /**
     * {@inheritDoc}
     */
    public ActionForward create(ActionMapping mapping,
                                ActionForm formIn,
                                HttpServletRequest request,
                                HttpServletResponse response) {
        DynaActionForm form = (DynaActionForm) formIn;

        RequestContext requestContext = new RequestContext(request);
        StrutsDelegate strutsDelegate = getStrutsDelegate();

        //Validate the form to make sure everything was filled out correctly
        ActionErrors errors = RhnValidationHelper.validateDynaActionForm(this, form);

        User user = requestContext.getCurrentUser();
        String advisoryNameFromForm = form.getString("advisoryName");
        //Make sure advisoryName is unique
        if (!ErrataManager.advisoryNameIsUnique(null, advisoryNameFromForm,
                user.getOrg())) {
            errors.add(ActionMessages.GLOBAL_MESSAGE,
                       new ActionMessage("errata.edit.error.uniqueAdvisoryName"));
        }
        // Make sure advisoryName does not begin with RH
        if (advisoryNameFromForm.toUpperCase().startsWith("RH")) {
            errors.add(ActionMessages.GLOBAL_MESSAGE,
                       new ActionMessage("errata.edit.error.rhAdvisoryName"));
        }
        //Make sure at least one channel is selected
        RhnSet set = updateSet(request);
        if (set.isEmpty()) {
            errors.add(ActionMessages.GLOBAL_MESSAGE,
                    new ActionMessage("errata.publish.nochannelsselected"));
        }

        if (!errors.isEmpty()) { // We've got errors. Forward to failure mapping.
            addErrors(request, errors);
            return mapping.findForward("failure");
        }

        //Create a new errata
        Errata e = new Errata();
        e.setSynopsis(form.getString("synopsis"));
        e.setAdvisoryName(form.getString("advisoryName"));
        e.setAdvisoryRel(Long.valueOf(form.getString("advisoryRelease")));
        e.setAdvisoryType(form.getString("advisoryType"));
        e.setProduct(form.getString("product"));
        e.setErrataFrom(form.getString("errataFrom"));

        //Advisory = advisoryName-advisoryRelease
        e.setAdvisory(form.getString("advisoryName") + "-" +
                      form.getString("advisoryRelease"));

        //create a bug and add it to the set
        Bug bug = createBug(form);
        if (bug != null) {
            e.addBug(bug);
        }
        e.setTopic(form.getString("topic"));
        e.setDescription(form.getString("description"));
        e.setSolution(form.getString("solution"));
        if (ErrataFactory.ERRATA_TYPE_SECURITY.equals(e.getAdvisoryType())) {
            e.setSeverity(Severity.getById((Integer)form.get("advisorySeverity")));
        }


        //add keywords... split on commas and add separately to list
        String keywordsField = form.getString("keywords");
        if (keywordsField != null) {
            List keywords = Arrays.asList(keywordsField.split(","));
            for (Object keywordIn : keywords) {
                String keyword = (String) keywordIn;
                keyword = keyword.trim();
                if (keyword != null && !keyword.isEmpty()) {
                    e.addKeyword(keyword);
                }
            }
        }
        e.setRefersTo(form.getString("refersTo"));
        e.setNotes(form.getString("notes"));

        //Set issueDate to now
        Date date = new Date(System.currentTimeMillis());
        e.setIssueDate(date);
        e.setUpdateDate(date);

        //Set the org for the errata to the logged in user's org
        e.setOrg(user.getOrg());

        ErrataFactory.save(e);
        ErrataManager.addToChannels(e, channelAction.getChannelIdsFromRhnSet(set), user);

        ActionMessages msgs = new ActionMessages();
        msgs.add(ActionMessages.GLOBAL_MESSAGE,
                 new ActionMessage("errata.created",
                                   e.getAdvisoryName(),
                                   e.getAdvisoryRel().toString()));
        saveMessages(request, msgs);
        return strutsDelegate.forwardParam(mapping.findForward("success"),
                                      "eid",
                                      e.getId().toString());
    }

    /**
     * Helper method to create a new bug from a form
     * @param form the form containing the bug items
     * @return Returns a new bug.
     */
    private Bug createBug(DynaActionForm form) {
        //if id and summary are not null, we can create a new bug, otherwise return null
        if (!form.getString("buglistId").isEmpty() &&
                !form.getString("buglistSummary").isEmpty()) {
            Long id = Long.valueOf(form.getString("buglistId"));
            String summary = form.getString("buglistSummary");
            String url = form.getString("buglistUrl");
            return ErrataFactory.createBug(id, summary, url);
        }
        return null;
    }

    @Override
    protected void processMethodKeys(Map<String, String> map) {
        map.put("errata.create.jsp.createerrata", "create");
    }

    @Override
    protected RhnSetDecl getSetDecl() {
        return channelAction.getSetDecl();
    }

    @Override
    protected DataResult getDataResult(User user, ActionForm formIn, HttpServletRequest request) {
        return channelAction.getDataResult(user, formIn, request);
    }

    @Override
    protected void processParamMap(ActionForm form, HttpServletRequest request, Map<String, Object> params) {
        channelAction.processParamMap(form, request, params);
    }
}

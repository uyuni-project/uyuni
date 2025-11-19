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
package com.redhat.rhn.frontend.action.systems;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.frontend.action.BaseSearchAction;
import com.redhat.rhn.frontend.action.common.BadParameterException;
import com.redhat.rhn.frontend.dto.SystemSearchResult;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.frontend.struts.StrutsDelegate;
import com.redhat.rhn.frontend.taglibs.list.helper.ListRhnSetHelper;
import com.redhat.rhn.frontend.taglibs.list.helper.Listable;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.action.DynaActionForm;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.servlet.http.HttpServletRequest;

import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcFault;

/**
 * Action handling the advanced system search page.
 */
public class SystemSearchAction extends BaseSearchAction implements Listable<SystemSearchResult> {

    public static final String DATA_SET = "searchResults";

    private static final Map<String, List<SystemSearchHelperType>> OPT_GROUPS_2 =
     Map.of(
             "systemsearch.jsp.details", SystemSearchHelperType.getDetailsGroup(),
             "systemsearch.jsp.activity", SystemSearchHelperType.getActivityGroup(),
             "systemsearch.jsp.hardware", SystemSearchHelperType.getHardwareGroup(),
             "systemsearch.jsp.devices", SystemSearchHelperType.getDeviceGroup(),
             "systemsearch.jsp.dmiinfo", SystemSearchHelperType.getDmiInfoGroup(),
             "systemsearch.jsp.networkinfo", SystemSearchHelperType.getNetworkInfoGroup(),
             "systemsearch.jsp.packages", SystemSearchHelperType.getPackagesGroup(),
             "systemsearch.jsp.location", SystemSearchHelperType.getLocationGroup()
     );

    private static final List<String> VALID_WHERE_STRINGS =
                    Arrays.asList(WHERE_ALL, WHERE_SSM);

    private static final Logger LOG = LogManager.getLogger(SystemSearchAction.class);

    @Override
    protected void insureFormDefaults(HttpServletRequest request, DynaActionForm form) {
        String search = form.getString(SEARCH_STR).trim();
        String where = form.getString(WHERE_TO_SEARCH);
        String viewMode = form.getString(VIEW_MODE);

        if (where == null || viewMode == null) {
            throw new BadParameterException("An expected form var was null");
        }

        if ("".equals(viewMode)) { // first time viewing page
            viewMode = "systemsearch_name_and_description";
            form.set(VIEW_MODE, viewMode);
            request.setAttribute(VIEW_MODE, viewMode);
        }

        if ("".equals(where) || !VALID_WHERE_STRINGS.contains(where)) {
            form.set(WHERE_TO_SEARCH, "all");
            request.setAttribute(WHERE_TO_SEARCH, "all");
        }

        Boolean fineGrained = (Boolean)form.get(FINE_GRAINED);
        request.setAttribute(FINE_GRAINED, fineGrained == null ? false : fineGrained);

        Boolean invert = (Boolean) form.get(INVERT_RESULTS);
        if (invert == null) {
            invert = Boolean.FALSE;
            form.set(INVERT_RESULTS, invert);
        }

        if (invert) {
            request.setAttribute(INVERT_RESULTS, "on");
        }
        else {
            request.setAttribute(INVERT_RESULTS, "off");
        }

        /* Here we set up a hashmap using the string resources key for the various options
         * group as a key into the hash, and the string resources/database mode keys as
         * the values of the options that are contained within each opt group. The jsp
         * uses this hashmap to setup a dropdown box
         */
        boolean matchingViewModeFound = false;
        Map<String, List<Map<String, String>>> optGroupsMap = new HashMap<>();

        for (Map.Entry<String, List<SystemSearchHelperType>> group : OPT_GROUPS_2.entrySet()) {
            List<Map<String, String>> options = new ArrayList<>();

            for (SystemSearchHelperType type : group.getValue()) {
                options.add(createDisplayMap(LocalizationService.getInstance().getMessage(type.getLabel()),
                        type.getLabel()));
                if (type.equalsMode(viewMode)) {
                    matchingViewModeFound = true;
                }
            }
            optGroupsMap.put(group.getKey(), options);
        }

        if (viewMode != null && !matchingViewModeFound) {
            throw new BadParameterException("Bad viewMode passed in from form");
        }
        request.setAttribute(OPT_GROUPS_MAP, optGroupsMap);
        request.setAttribute(OPT_GROUPS_KEYS, optGroupsMap.keySet());
        request.setAttribute(SEARCH_STR, search);
        request.setAttribute(VIEW_MODE, viewMode);
        request.setAttribute(WHERE_TO_SEARCH, where);
    }

   @Override
   protected ActionForward doExecute(HttpServletRequest request, ActionMapping mapping,
                                     DynaActionForm form) {
        String viewMode = form.getString(VIEW_MODE);
        String searchString = form.getString(SEARCH_STR).trim();

        ActionErrors errs = new ActionErrors();
        if (SystemSearchHelperType.isActionErrorMode(viewMode)) {
                 String regEx = "(\\d)*";
                 Pattern pattern = Pattern.compile(regEx);
                 Matcher matcher = pattern.matcher(searchString);
                 if (!matcher.matches()) {
                     errs.add(ActionMessages.GLOBAL_MESSAGE,
                                 new ActionMessage("systemsearch.errors.numeric"));
                 }
             }

            // TODO: Set up combined-form validator
//              errs.add(RhnValidationHelper.validateDynaActionForm(this, daForm))
        addErrors(request, errs);

        ListRhnSetHelper helper = new ListRhnSetHelper(this, request, RhnSetDecl.SYSTEMS);
        helper.setWillClearSet(false);
        helper.setDataSetName(getDataSetName());
        helper.setListName(getListName());
        helper.execute();

        List results = (List) request.getAttribute(getDataSetName());
       LOG.debug("SystemSearch results.size() = {}", results != null ? results.size() : "null results");
        if ((results != null) && (results.size() == 1)) {
            SystemSearchResult s = (SystemSearchResult) results.get(0);
            return StrutsDelegate.getInstance().forwardParam(mapping.findForward("single"),
                            "sid", s.getId().toString());
        }
        return mapping.findForward(RhnHelper.DEFAULT_FORWARD);
    }

    protected DataResult<SystemSearchResult> performSearch(RequestContext context) {
        HttpServletRequest request = context.getRequest();
        String searchString = (String)request.getAttribute(SEARCH_STR);
        String viewMode = (String)request.getAttribute(VIEW_MODE);
        String whereToSearch = (String)request.getAttribute(WHERE_TO_SEARCH);
        Boolean invertResults = StringUtils.defaultString(
                        (String)request.getAttribute(INVERT_RESULTS)).equals("on");
        Boolean isFineGrained = (Boolean)request.getAttribute(FINE_GRAINED);
        String escapedSearchString = StringEscapeUtils.escapeHtml4(searchString);

        ActionErrors errs = new ActionErrors();
        DataResult<SystemSearchResult> dr = null;
        try {
            dr = SystemSearchHelper.systemSearch(context,
                    searchString,
                    viewMode,
                    invertResults,
                    whereToSearch, isFineGrained);
        }
        catch (MalformedURLException | XmlRpcException e) {
            LOG.error("Caught Exception :{}", e, e);
            errs.add(ActionMessages.GLOBAL_MESSAGE,
                    new ActionMessage("packages.search.connection_error"));
        }
        catch (XmlRpcFault e) {
            LOG.info("Caught Exception :{}, code [{}]", e, e.getErrorCode(), e);
            if (e.getErrorCode() == 100) {
                LOG.error("Invalid search query", e);
                errs.add(ActionMessages.GLOBAL_MESSAGE,
                        new ActionMessage("packages.search.could_not_parse_query",
                                          escapedSearchString));
            }
            else if (e.getErrorCode() == 200) {
                LOG.error("Index files appear to be missing: ", e);
                errs.add(ActionMessages.GLOBAL_MESSAGE,
                        new ActionMessage("packages.search.index_files_missing",
                                          escapedSearchString));
            }
            else {
                errs.add(ActionMessages.GLOBAL_MESSAGE,
                    new ActionMessage("packages.search.could_not_execute_query",
                                      escapedSearchString));
            }
        }
        if (dr == null) {
            ActionMessages messages = new ActionMessages();
            messages.add(ActionMessages.GLOBAL_MESSAGE,
                    new ActionMessage("systemsearch_no_matches_found"));
            getStrutsDelegate().saveMessages(request, messages);
        }
        if (!errs.isEmpty()) {
            addErrors(request, errs);
        }
        return dr;
    }

    /** {@inheritDoc} */
    private String getListName()  {
        return RequestContext.PAGE_LIST;
    }

    /** {@inheritDoc} */
    private String getDataSetName() {
        return DATA_SET;
    }

    /**
     * Creates a Map with the keys display and value
     * @param display the value for display
     * @param value the value for value
     * @return Returns the map.
     */
    private Map<String, String> createDisplayMap(String display, String value) {
        Map<String, String> selection = new HashMap<>();
        selection.put("display", display);
        selection.put("value", value);
        return selection;
    }

    /** {@inheritDoc} */
    @Override
    public List<SystemSearchResult> getResult(RequestContext context) {
        String searchString = (String)context.getRequest().getAttribute(SEARCH_STR);

        if (!StringUtils.isBlank(searchString)) {
            LOG.debug("SystemSearchSetupAction.getResult() calling performSearch()");
            return performSearch(context);
        }
        LOG.debug("SystemSearchSetupAction.getResult() returning Collections.emptyList()");
        return Collections.emptyList();
    }

}

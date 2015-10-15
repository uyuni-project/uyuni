/**
 * Copyright (c) 2013 SUSE LLC
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
package com.redhat.rhn.frontend.action.audit.cve;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;
import org.directwebremoting.util.Logger;

import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.frontend.struts.StrutsDelegate;
import com.redhat.rhn.frontend.taglibs.list.ListTagHelper;
import com.redhat.rhn.frontend.taglibs.list.helper.ListRhnSetHelper;
import com.redhat.rhn.frontend.taglibs.list.helper.Listable;
import com.redhat.rhn.manager.audit.CVEAuditManager;
import com.redhat.rhn.manager.audit.CVEAuditSystem;
import com.redhat.rhn.manager.audit.PatchStatus;
import com.redhat.rhn.manager.audit.UnknownCVEIdentifierException;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;

/**
 * CVEAuditAction
 * @version $Rev$
 */
public class CVEAuditAction extends RhnAction {
    /** CVE prefix */
    private static final String CVE_PREFIX = "CVE";

    /** Attribute used to populate select with years */
    private static final String CVE_YEARS_ATTRIBUTE = "years";

    /** Search field parameter names */
    private static final String CVE_IDENTIFIER_YEAR_PARAMETER = "cveIdentifierYear";
    private static final String CVE_IDENTIFIER_ID_PARAMETER = "cveIdentifierId";

    /** Set to true if the CVE number turns out to be unknown */
    private static final String CVE_IDENTIFIER_UNKNOWN = "cveIdentifierUnknown";

    /** Filter checkbox parameter name prefix */
    private static final String FILTER_PARAMETER_PREFIX = "include";

    /** Redirection forward name */
    private static final String QUERY_STRING_PARAMETERS_FORWARD = "queryStringParameters";

    /** Logger instance */
    private static Logger log = Logger.getLogger(CVEAuditAction.class);

    /** Runs backend CVE Audit queries when requested by ListTag. */
    private class CVEAuditResultListable implements Listable {
        @Override
        public List<CVEAuditSystem> getResult(RequestContext context) {
            log.debug("ResultLister called with context " + context.toString());
            HttpServletRequest request = context.getRequest();
            String cveIdentifierYear = request.getParameter(CVE_IDENTIFIER_YEAR_PARAMETER);
            String cveIdentifierId = request.getParameter(CVE_IDENTIFIER_ID_PARAMETER);
            request.setAttribute(CVE_IDENTIFIER_UNKNOWN, false);

            if (StringUtils.isNotBlank(cveIdentifierId)) {
                try {
                    String cveIdentifier = CVE_PREFIX + "-" + cveIdentifierYear +
                            "-" + cveIdentifierId;
                    return runAudit(request, cveIdentifier);
                }
                catch (UnknownCVEIdentifierException e) {
                    StrutsDelegate strutsDelegate = getStrutsDelegate();
                    ActionErrors errors = new ActionErrors();
                    strutsDelegate.addError("cveaudit.jsp.unknowncveidentifier", errors);
                    strutsDelegate.saveMessages(request, errors);
                    request.setAttribute(CVE_IDENTIFIER_UNKNOWN, true);
                }
            }
            return Collections.emptyList();
        }
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public ActionForward execute(ActionMapping mapping, ActionForm formIn,
            HttpServletRequest request, HttpServletResponse response) {
        DynaActionForm form = (DynaActionForm) formIn;

        // copy parameters in attributes for alphabar, paginator, sorter use
        request.setAttribute(CVE_YEARS_ATTRIBUTE, getYears());
        String cveIdentifierYear = request.getParameter(CVE_IDENTIFIER_YEAR_PARAMETER);
        request.setAttribute(CVE_IDENTIFIER_YEAR_PARAMETER, cveIdentifierYear);
        String cveIdentifierId = request.getParameter(CVE_IDENTIFIER_ID_PARAMETER);
        request.setAttribute(CVE_IDENTIFIER_ID_PARAMETER, cveIdentifierId);

        for (PatchStatus patchStatus : EnumSet.allOf(PatchStatus.class)) {
            String parameterName = getParameterNameFor(patchStatus);
            Boolean parameter = (Boolean) form.get(parameterName);
            if (parameter == null) {
                parameter = false;
            }
            request.setAttribute(parameterName, parameter ? "on" : "off");
        }

        // needed for alphabar
        request.setAttribute(ListTagHelper.PARENT_URL, request.getRequestURI());

        // first request: form not submitted, no parameters. Just pre-init some
        // form fields
        if (!isSubmitted(form) && cveIdentifierId == null) {
            setupForm(request, form);
        }
        // second request: form was submitted (with method POST). Redirect to
        // the same action with query string parameters only (needed for system
        // set selection, page size selector)
        else if (isSubmitted(form)) {
            Map<String, String> forwardParams = makeParamMap(request);
            Enumeration<String> paramNames = request.getParameterNames();
            while (paramNames.hasMoreElements()) {
                String name = (String) paramNames.nextElement();
                if (!SUBMITTED.equals(name)) {
                    forwardParams.put(name, request.getParameter(name));
                }
            }

            return getStrutsDelegate().forwardParams(
                    mapping.findForward(QUERY_STRING_PARAMETERS_FORWARD), forwardParams);
        }
        // third request: form was submitted and got redirected. Now actually
        // compute results.
        else {
            ListRhnSetHelper helper = new ListRhnSetHelper(new CVEAuditResultListable(),
                    request, RhnSetDecl.SYSTEMS);
            helper.setWillClearSet(false);
            helper.execute();
        }

        return getStrutsDelegate()
                .forwardParams(mapping.findForward(RhnHelper.DEFAULT_FORWARD),
                        request.getParameterMap());
    }

    /**
     * Return years from 1999 (first appearance of CVE) up to the current year.
     * @return list of years from 1999 to the current
     */
    private List<HashMap<String, String>> getYears() {
        List<HashMap<String, String>> ret = new ArrayList<HashMap<String, String>>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (Integer year = currentYear; year >= 1999; year--) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("value", year.toString());
            ret.add(map);
        }
        return ret;
    }

    /**
     * Run the back-end CVE search
     *
     * @param request
     * @param cveIdentifier the CVE name string (CVE-YYYY-1234)
     * @throws UnknownCVEIdentifierException if the CVE number is not known to SUSE Manager
     */
    private List<CVEAuditSystem> runAudit(HttpServletRequest request,
            String cveIdentifier) throws UnknownCVEIdentifierException {
        log.debug("runAudit called with parameter: " + cveIdentifier);

        User user = new RequestContext(request).getCurrentUser();
        EnumSet<PatchStatus> patchStatuses = EnumSet.noneOf(PatchStatus.class);

        for (PatchStatus patchStatus : EnumSet.allOf(PatchStatus.class)) {
            String parameterName = getParameterNameFor(patchStatus);
            if ("on".equals(request.getParameter(parameterName))) {
                patchStatuses.add(patchStatus);
            }
        }

        List<CVEAuditSystem> results = CVEAuditManager.listSystemsByPatchStatus(user,
                cveIdentifier, patchStatuses);

        log.debug("runAudit finished with " + results.size() + " results");
        return results;
    }

    /**
     * Set up jsp variables
     *
     * @param request
     * @param form
     */
    private void setupForm(HttpServletRequest request, DynaActionForm form) {
        for (PatchStatus patchStatus : EnumSet.allOf(PatchStatus.class)) {
            if (patchStatus.equals(PatchStatus.AFFECTED_PATCH_APPLICABLE) ||
                    patchStatus.equals(PatchStatus.AFFECTED_PATCH_INAPPLICABLE)) {
                form.set(getParameterNameFor(patchStatus), Boolean.TRUE);
            }
        }
    }

    /**
     * Convenience method to get the parameter name of a PatchStatus filter
     *
     * @param patchStatus
     * @return the patch status filter parameter name
     */
    private String getParameterNameFor(PatchStatus patchStatus) {
        return FILTER_PARAMETER_PREFIX + camelize(patchStatus.toString());
    }

    /**
     * Turn an UNDERSCORE_STRING into a CamelString
     *
     * @param string the original
     * @return the camelized version
     */
    private String camelize(String string) {
        String result = "";
        for (String s : string.split("_")) {
            result += StringUtils.capitalize(StringUtils.lowerCase(s));
        }
        return result;
    }
}

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
package com.redhat.rhn.frontend.action.help;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

import redstone.xmlrpc.XmlRpcClient;
import redstone.xmlrpc.XmlRpcFault;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.validator.ValidatorException;
import com.redhat.rhn.common.validator.ValidatorWarning;
import com.redhat.rhn.frontend.action.BaseSearchAction;
import com.redhat.rhn.frontend.context.Context;
import com.redhat.rhn.frontend.dto.HelpDocumentOverview;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnHelper;

/**
 * DocSearchSetupAction
 * @version $Rev$
 */
public class DocSearchSetupAction extends BaseSearchAction {
    private static Logger log = Logger.getLogger(DocSearchSetupAction.class);

    protected ActionForward doExecute(HttpServletRequest request, ActionMapping mapping,
                    DynaActionForm form)
        throws MalformedURLException, XmlRpcFault {

        RequestContext ctx = new RequestContext(request);
        String searchString = form.getString(SEARCH_STR);
        String viewmode = form.getString(VIEW_MODE);

        List<Map<String, String>> searchOptions = new ArrayList<Map<String, String>>();
        addOption(searchOptions, "docsearch.content_title", OPT_CONTENT_TITLE);
        addOption(searchOptions, "docsearch.free_form", OPT_FREE_FORM);
        addOption(searchOptions, "docsearch.content", OPT_CONTENT_ONLY);
        addOption(searchOptions, "docsearch.title", OPT_TITLE_ONLY);

        request.setAttribute(SEARCH_STR, searchString);
        request.setAttribute(VIEW_MODE, viewmode);
        request.setAttribute(SEARCH_OPT, searchOptions);


        if (!StringUtils.isBlank(searchString)) {
            List results = performSearch(ctx.getWebSession().getId(),
                                         searchString,
                                         viewmode, request);
            log.debug("GET search: " + results);
            request.setAttribute(RequestContext.PAGE_LIST,
                    results != null ? results : Collections.emptyList());
        }
        else {
            request.setAttribute(RequestContext.PAGE_LIST, Collections.emptyList());
        }
        if (isSubmitted(form)) {
            return mapping.findForward("success");
        }
        return mapping.findForward(RhnHelper.DEFAULT_FORWARD);
    }

    private List performSearch(Long sessionId, String searchString,
                               String mode, HttpServletRequest request)
        throws XmlRpcFault, MalformedURLException {

        log.debug("Performing doc search");

        // call search server
        XmlRpcClient client = new XmlRpcClient(
                ConfigDefaults.get().getSearchServerUrl(), true);
        List args = new ArrayList();
        args.add(sessionId);
        args.add("docs");
        args.add(preprocessSearchString(searchString, mode));
        // get lang we are searching in
        Locale l = Context.getCurrentContext().getLocale();
        args.add(l.toString());
        Boolean searchFreeForm = false;
        if (OPT_FREE_FORM.equals(mode)) {
            // adding a boolean of true to signify we want the results to be
            // constrained to closer matches, this will force the Lucene Queries
            // to use a "MUST" instead of the default "SHOULD".  It will not
            // allow fuzzy matches as in spelling errors, but it will allow
            // free form searches to do more advanced options
            //args.add(true);
            searchFreeForm = true;
        }
        args.add(searchFreeForm);
        List results = Collections.emptyList();
        try {
            results = (List)client.invoke("index.search", args);
        }
        catch (XmlRpcFault e) {
            if (e.getErrorCode() == 200) {
                //This is most likely a language error
                //so lets try the search the default language
                //removing the 'lang' from the args
                args.remove(args.size() - 2);
                results = (List)client.invoke("index.search", args);

                List<ValidatorWarning> warnings = new LinkedList<ValidatorWarning>();
                warnings.add(new ValidatorWarning
                        ("packages.search.index_files_missing_for_docs"));

                getStrutsDelegate().saveMessages(request, Collections.EMPTY_LIST, warnings);

            }
            else {
                throw e;
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("results = [" + results + "]");
        }

        if (results.isEmpty()) {
            return Collections.emptyList();
        }

        List<HelpDocumentOverview> docs = new ArrayList<HelpDocumentOverview>();
        for (int x = 0; x < results.size(); x++) {
            HelpDocumentOverview doc = new HelpDocumentOverview();
            Map item = (Map) results.get(x);
            log.debug("SearchServer sent us item [" + item.get("rank") + "], score = " +
                    item.get("score") + ", summary = " + item.get("summary") +
                    ", title = " + item.get("title") + ", url = " + item.get("url"));
            doc.setUrl((String)item.get("url"));
            doc.setTitle((String)item.get("title"));
            doc.setSummary((String)item.get("summary"));
            docs.add(doc);
        }
        return docs;
    }

    private String preprocessSearchString(String searchstring,
                                          String mode) {

        if (!OPT_FREE_FORM.equals(mode) && searchstring.indexOf(':') > 0) {
            throw new ValidatorException("Can't use free form and field search.");
        }

        StringBuilder buf = new StringBuilder(searchstring.length());
        String[] tokens = searchstring.split(" ");
        for (String s : tokens) {
            if (s.trim().equalsIgnoreCase("AND") ||
                s.trim().equalsIgnoreCase("OR") ||
                s.trim().equalsIgnoreCase("NOT")) {

                s = s.toUpperCase();
            }

            buf.append(s);
            buf.append(" ");
        }


        String query = buf.toString().trim();
        // when searching the name field, we also want to include the filename
        // field in case the user passed in version number.
       if (OPT_CONTENT_ONLY.equals(mode)) {
            return "(content:(" + query + "))";
        }
       else if (OPT_TITLE_ONLY.equals(mode)) {
           return "(title:(" + query + "))";
       }
       else if (OPT_CONTENT_TITLE.equals(mode)) {
           return "(content:(" + query + ") title:(" + query + "))";
       }

        // OPT_FREE_FORM send as is.
        return buf.toString();
    }

    @Override
    protected void insureFormDefaults(HttpServletRequest request, DynaActionForm form) {
        String searchString = request.getParameter(SEARCH_STR);
        String viewmode = request.getParameter(VIEW_MODE);

    }

}

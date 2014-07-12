/**
 * Copyright (c) 2012--2014 Red Hat, Inc.
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
package com.redhat.rhn.frontend.action.audit.scap;

import java.net.MalformedURLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import redstone.xmlrpc.XmlRpcClient;
import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcFault;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.manager.audit.ScapManager;

/**
 * XccdfSearchHelper
 */
public class XccdfSearchHelper extends RhnAction {

    private static final String IDENT_INDEX = "xccdfIdent";
    private static final String INDEX_SEARCH = "index.search";
    private static final String SYSTEM_LIST = "system_list";

    /**
     * Perform search over XCCDF
     * @param searchString A string to search within xccdf:Rules
     * @param whereToSearch Where to search for scans, either ssm or all machines
     * @param startDate search scans performed after startDate (null to omit)
     * @param endDate search scans performed before endDate (null to omit)
     * @param ruleResult search rules with given ruleresult label (null to omit)
     * @param returnTestResults return results as list of TestResult (true),
     * or RuleResults (false)
     * @param context A context of current request
     * @return a list of xccdf:rule-results
     * @throws MalformedURLException possibly bad configuration for search server address
     * @throws XmlRpcException in the case of a serialization failure
     * @throws XmlRpcFault bad communication with search server
     */
    public static DataResult performSearch(String searchString, String whereToSearch,
            Date startDate, Date endDate, String ruleResult, boolean returnTestResults,
            RequestContext context)
            throws MalformedURLException, XmlRpcException, XmlRpcFault {
        ArrayList args = new ArrayList();
        args.add(context.getWebSession().getId());
        args.add(IDENT_INDEX);
        args.add(preprocessSearchString(searchString));
        args.add(true); //fine grained
        List searchResult = invokeSearchServer(INDEX_SEARCH, args);
        // searchResult contains id to rhnXccdfIdent relation,
        // while we want to return RuleResults
        List<Long> identIds = new ArrayList<Long>();
        for (int x = searchResult.size() - 1; x >= 0; x--) {
            Map item = (Map) searchResult.get(x);
            Long id = new Long((String)item.get("id"));
            identIds.add(id);
        }

        Map params = new HashMap<String, Object>();
        params.put("user_id", context.getCurrentUser().getId());
        if (SYSTEM_LIST.equals(whereToSearch)) {
            params.put("slabel", SYSTEM_LIST);
        }
        if (startDate != null && endDate != null) {
            params.put("start", new Timestamp(startDate.getTime()));
            params.put("end", new Timestamp(endDate.getTime()));
        }
        if (ruleResult != null) {
            params.put("result", ruleResult);
        }
        return ScapManager.searchByIdentIds(params, identIds, returnTestResults);
    }

    private static String preprocessSearchString(String searchString) {
        return "identifier:(" + keywordsToUpper(searchString) + ")";
    }

    private static String keywordsToUpper(String searchString) {
        StringBuilder buf = new StringBuilder(searchString.length() + 1);
        for (String s : searchString.split(" ")) {
            if (s.trim().equalsIgnoreCase("AND") ||
                    s.trim().equalsIgnoreCase("OR") ||
                    s.trim().equalsIgnoreCase("NOT")) {
                s = s.toUpperCase();
            }
            buf.append(s);
            buf.append(" ");
        }
        return buf.toString().trim();
    }

    private static List invokeSearchServer(String path, List args)
            throws MalformedURLException, XmlRpcException, XmlRpcFault {
        XmlRpcClient client = new XmlRpcClient(
                ConfigDefaults.get().getSearchServerUrl(), true);
        return (List) client.invoke(path, args);
    }
}

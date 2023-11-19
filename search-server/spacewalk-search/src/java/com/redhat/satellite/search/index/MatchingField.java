/*
 * Copyright (c) 2008--2015 Red Hat, Inc.
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

package com.redhat.satellite.search.index;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumberTools;
import org.apache.lucene.index.Term;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Purpose of this class is to guess what field in the Document is responsible
 * for this being flagged as a hit.  We will use the query and terms as hints.
 */
public class MatchingField {
    private static final Logger LOG = LogManager.getLogger(MatchingField.class);
    protected Document doc;
    protected Object[] terms;
    protected String query;
    protected Map<String, Boolean> needNumberToolsAdjust;

    /**
     *
     * @param queryIn  query used in search
     * @param docIn   document results returned as a match
     * @param termsIn terms from a parsed query
     */
    public MatchingField(String queryIn, Document docIn, Set<Term> termsIn) {
        query = queryIn;
        doc = docIn;
        if ((termsIn != null)) {
            terms = termsIn.toArray();
        }
        else {
            terms = new Term[0];
        }
        needNumberToolsAdjust = new HashMap<>();
        needNumberToolsAdjust.put("cpuMHz", true);
        needNumberToolsAdjust.put("cpuBogoMIPS", true);
        needNumberToolsAdjust.put("cpuNumberOfCpus", true);
        needNumberToolsAdjust.put("ram", true);
        needNumberToolsAdjust.put("swap", true);
    }

    /**
     *
     * @return field name most responsible for this document being a match
     */
    public String getFieldName() {
        if (terms.length > 0) {
            return ((Term)terms[0]).field();
        }
        return getFirstFieldName(query);
    }

    /**
     *
     * @return value most responsible for this document being a match
     */
    public String getFieldValue() {
        String fieldName = getFieldName();
        Field f = doc.getField(fieldName);
        if (f == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("[length=").append(terms.length).append(";  ");
            for (Object o : terms) {
                sb.append(o).append(", ");
            }
            sb.append("]");
            LOG.info("Unable to get matchingFieldValue for field : {} with query: {}, and terms = {}",
                    fieldName, query, sb);
            LOG.info("Document = {}", doc);
            return "";
        }
        String value = f.stringValue();
        if (needNumberToolsAdjust.containsKey(fieldName)) {
            long temp = NumberTools.stringToLong(value);
            value = Long.toString(temp);
        }
        return value;
    }

    /**
     *
     * @param queryIn the query
     * @return first term in query, which is a good guess as to being the most
     * important term in the query.
     *
     * */
    public static String getFirstFieldName(String queryIn) {
        // remove first parenthesis, this shows up when you do a
        // Query.toString(), package searches generally have this
        if (queryIn.startsWith("(")) {
            queryIn = queryIn.substring(1);
        }
        StringTokenizer tokens = new StringTokenizer(queryIn, ":");
        return tokens.nextToken();
    }
}

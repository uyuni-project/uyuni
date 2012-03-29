/**
 * Copyright (c) 2011 Novell
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

package com.redhat.rhn.common.logging;

import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * Wrapper around {@link HttpServletRequest} objects. This class is used to make
 * form parameters that were extracted from multipart content transparently
 * available for being logged later on.
 */
public class AuditLogMultipartRequest extends HttpServletRequestWrapper {

    /** Store all multipart form parameters to a separate map */
    private Map multipartParams = new HashMap();

    /**
     * Constructor.
     *
     * @param request request
     */
    public AuditLogMultipartRequest(HttpServletRequest request) {
        super(request);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public Enumeration getParameterNames() {
        // Take parameter names from the original request
        Enumeration baseParams = super.getParameterNames();
        Set allParameterNames = new HashSet();
        while (baseParams.hasMoreElements()) {
            allParameterNames.add(baseParams.nextElement());
        }
        // Add multipart parameters names
        Collection multiParams = this.multipartParams.keySet();
        Iterator iterator = multiParams.iterator();
        while (iterator.hasNext()) {
            allParameterNames.add(iterator.next());
        }
        return Collections.enumeration(allParameterNames);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values == null) {
            values = (String[]) multipartParams.get(name);
        }
        return values;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public Map getParameterMap() {
        Map map = new HashMap(multipartParams);
        Enumeration names = super.getParameterNames();
        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            map.put(name, super.getParameterValues(name));
        }
        return map;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        if (value == null) {
            String[] mValue = (String[]) multipartParams.get(name);
            if ((mValue != null) && (mValue.length > 0)) {
                value = mValue[0];
            }
        }
        return value;
    }

    /**
     * Set the multipart form parameters from the MultipartRequestWrapper object
     * after the stream was parsed in the RequestProcessor's processPopulate()
     * method.
     *
     * @param parameters parameters
     */
    public void setMultipartParameters(Map parameters) {
        this.multipartParams = parameters;
    }
}

/**
 * Copyright (c) 2011 SUSE LLC
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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.upload.MultipartRequestWrapper;

import com.redhat.rhn.frontend.struts.RhnRequestProcessor;

/**
 * Special extension to the {@link RhnRequestProcessor}: Make all of the form
 * parameters that were parsed from multipart content stream available to the
 * audit logging servlet filter.
 */
public class AuditLogRequestProcessor extends RhnRequestProcessor {
    /**
     * {@inheritDoc}
     */
    protected void processPopulate(HttpServletRequest request,
            HttpServletResponse response, ActionForm form, ActionMapping mapping)
            throws ServletException {
        super.processPopulate(request, response, form, mapping);

        // Check if this is a multipart request
        if (request instanceof MultipartRequestWrapper) {
            MultipartRequestWrapper wrapper = (MultipartRequestWrapper) request;
            // Check the wrapped request and remember the parsed parameters
            if (wrapper.getRequest() instanceof AuditLogMultipartRequest) {
                ((AuditLogMultipartRequest) wrapper.getRequest())
                        .setMultipartParameters(wrapper.getParameterMap());
            }
        }
    }
}

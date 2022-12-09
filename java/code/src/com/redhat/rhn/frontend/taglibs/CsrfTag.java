/*
 * Copyright (c) 2009--2011 Red Hat, Inc.
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
package com.redhat.rhn.frontend.taglibs;

import com.redhat.rhn.common.security.CSRFTokenValidator;

import org.apache.struts.taglib.html.HiddenTag;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;

/**
 * HiddenTag
 * <p>
 * Renders {@literal < />}
 *
 */
public class CsrfTag extends HiddenTag {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 7202597580376186072L;

    /**
     * {@inheritDoc}
     */
    @Override
    public int doStartTag() throws JspException {
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        HttpSession session = request.getSession(true);

        this.setProperty("csrf_token");
        this.setValue(CSRFTokenValidator.getToken(session));

        super.doStartTag();
        return SKIP_BODY;
    }

}

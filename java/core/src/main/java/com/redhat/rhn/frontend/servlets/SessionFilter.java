/*
 * Copyright (c) 2009--2013 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.frontend.servlets;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.hibernate.HibernateRuntimeException;
import com.redhat.rhn.common.localization.LocalizationService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * SessionFilter is a simple servlet filter to handle cleaning up the Hibernate
 * Session after each request.
 *
 * See also {@link com.suse.manager.webui.utils.SparkApplicationHelper#setupHibernateSessionFilter()}
 */
public class SessionFilter implements Filter {

    private static final Logger LOG = LogManager.getLogger(SessionFilter.class);

    /** {@inheritDoc} */
    @Override
    public void init(FilterConfig config) {
        // no-op
    }


    /** {@inheritDoc} */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        boolean committed = false;
        try {
            logHere("Calling doFilter");
            // pass up stack
            chain.doFilter(request, response);

            if (HibernateFactory.inTransaction()) {
                HibernateFactory.commitTransaction();
            }
            logHere("Transaction committed");
            committed = true;
        }
        catch (IOException | AssertionError | ServletException e) {
            LOG.error(HibernateFactory.ROLLBACK_MSG, e);
            throw e;
        }
        catch (HibernateException e) {
            LOG.error(HibernateFactory.ROLLBACK_MSG, e);
            throw new HibernateRuntimeException(HibernateFactory.ROLLBACK_MSG, e);
        }
        catch (RuntimeException e) {
            LOG.error(HibernateFactory.ROLLBACK_MSG, e);
            request.setAttribute("exception", LocalizationService.getInstance()
                    .getMessage("errors.unexpected"));
            throw e;
        }
        finally {
            HibernateFactory.rollbackTransactionAndCloseSession(committed);
        }

    }

    private void logHere(final String msg) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(msg);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void destroy() {
        // no-op
    }
}

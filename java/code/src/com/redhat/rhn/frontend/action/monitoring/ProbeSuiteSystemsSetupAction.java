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
package com.redhat.rhn.frontend.action.monitoring;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.monitoring.suite.ProbeSuite;
import com.redhat.rhn.frontend.dto.monitoring.MonitoredServerDto;
import com.redhat.rhn.frontend.listview.PageControl;
import com.redhat.rhn.frontend.struts.BaseSetListAction;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;

/**
 * ProbeSuiteListSetupAction - lists all the Systems assigned to this probesuite
 * @version $Rev: 55183 $
 */
public class ProbeSuiteSystemsSetupAction extends BaseSetListAction {

    /**
     * {@inheritDoc}
     */
    protected DataResult<MonitoredServerDto> getDataResult(RequestContext rctx,
            PageControl pc) {
        return ProbeSuiteHelper.getServersInSuite(rctx.getRequest(), pc);
    }

    /**
     * {@inheritDoc}
     */
    protected void processRequestAttributes(RequestContext rctx) {
        super.processRequestAttributes(rctx);
        ProbeSuite probeSuite = rctx.lookupProbeSuite();
        rctx.getRequest().setAttribute("probeSuite", probeSuite);
    }

    /**
     * {@inheritDoc}
     */
    public RhnSetDecl getSetDecl() {
        return RhnSetDecl.PROBE_SUITE_SYSTEMS;
    }
}

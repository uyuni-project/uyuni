/*
 * Copyright (c) 2021 SUSE LLC
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

package com.redhat.rhn.frontend.xmlrpc.system.monitoring;

import com.redhat.rhn.domain.dto.EndpointInfo;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.manager.formula.FormulaManager;

import com.suse.manager.api.ReadOnly;

import java.util.List;
import java.util.stream.Collectors;

/**
 * SystemMonitoringHandler
 * @xmlrpc.namespace system.monitoring
 * @xmlrpc.doc Provides methods to access information about managed systems, applications and formulas which can be
 * relevant for Prometheus monitoring
 */
public class SystemMonitoringHandler extends BaseHandler {

    private final FormulaManager formulaManager;

    /**
     * Instantiates a new system handler for system.monitoring namespace
     * @param formulaManagerIn instance of formula manager object
     */
    public SystemMonitoringHandler(FormulaManager formulaManagerIn) {
        this.formulaManager = formulaManagerIn;
    }

    /**
     * Get the endpoint details for all Prometheus exporters installed on the systems whose IDs match
     * with the passed systems IDs and all of the groups those systems are member of.
     *
     * @param loggedInUser The current user
     * @param sids The system IDs
     * @return a list containing endpoint details for all Prometheus exporters on the passed system IDs.
     *
     * @xmlrpc.doc Get the list of monitoring endpoint details.
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #array_single("int", "sids")
     * @xmlrpc.returntype
     *   #return_array_begin()
     *     $EndpointInfoSerializer
     *   #array_end()
     */
    @ReadOnly
    public List<EndpointInfo> listEndpoints(User loggedInUser, List<Integer> sids) {
        List<Long> ids = sids.stream()
                .map(Integer::longValue)
                .collect(Collectors.toList());
        return this.formulaManager.listEndpoints(ids);
    }
}

/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.proxy.update;

import static com.redhat.rhn.common.ErrorReportingStrategies.logReportingStrategy;
import static com.redhat.rhn.common.ErrorReportingStrategies.validationReportingStrategy;
import static java.util.Arrays.asList;

import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.system.SystemManager;

import com.suse.manager.webui.utils.gson.ProxyConfigUpdateJson;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for dealing with updates within the proxy configuration scope.
 * The update process is following the Chain of Responsibility pattern, where the process is divided into several steps,
 * each one with a specific responsibility.
 * These steps have a specific order and are executed in sequence.
 * If an error occurs in any of these steps, the process halts and the errors are be reported.
 */
public class ProxyConfigUpdateFacadeImpl implements ProxyConfigUpdateFacade {
    private final List<ProxyConfigUpdateContextHandler> contextHandlerChain = new ArrayList<>();

    /**
     * Constructor
     */
    public ProxyConfigUpdateFacadeImpl() {
        this.contextHandlerChain.addAll(asList(
                new ProxyConfigUpdateAcquisitor(),
                new ProxyConfigUpdateValidation(),
                new ProxyConfigUpdateFileAcquisitor(),
                new ProxyConfigUpdateInitializer(),
                new ProxyConfigUpdateSavePillars(),
                new ProxyConfigUpdateApplySaltState()
        ));
    }

    /**
     * Update the proxy configuration, following the chain of responsibility pattern defined in the constructor.
     *
     * @param request                    the proxy configuration update JSON with the new values
     * @param systemManager              the system manager
     * @param user                       the user
     */
    @Override
    public void update(
            ProxyConfigUpdateJson request,
            SystemManager systemManager,
            User user
    ) {
        ProxyConfigUpdateContext context =
                new ProxyConfigUpdateContext(request, systemManager, user);

        for (ProxyConfigUpdateContextHandler handler : contextHandlerChain) {
            handler.handle(context);
            context.getErrorReport().report(logReportingStrategy(this));
            context.getErrorReport().report(validationReportingStrategy());
        }
    }
}

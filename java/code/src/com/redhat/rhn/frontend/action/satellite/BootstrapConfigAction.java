/*
 * Copyright (c) 2009--2012 Red Hat, Inc.
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
package com.redhat.rhn.frontend.action.satellite;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.frontend.action.satellite.util.CACertPathUtil;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.frontend.struts.RhnValidationHelper;
import com.redhat.rhn.frontend.struts.StrutsDelegate;
import com.redhat.rhn.manager.satellite.ConfigureBootstrapCommand;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

import java.net.IDN;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * BootstrapConfigAction - action to handle changing the bootstrap config file options.
 */
public class BootstrapConfigAction extends BaseConfigAction {
    /* in document root */

    public static final String HOSTNAME = "hostname";
    public static final String SSL_CERT = "ssl-cert";
    public static final String SALT = "salt";

    public static final String ENABLE_GPG = "gpg";
    public static final String HTTP_PROXY = "http-proxy";
    public static final String HTTP_PROXY_USERNAME = "http-proxy-username";
    public static final String HTTP_PROXY_PASSWORD = "http-proxy-password";
    public static final String CSRF_TOKEN = "csrfToken";


    /** {@inheritDoc} */
    @Override
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm formIn,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {

        DynaActionForm form = (DynaActionForm) formIn;
        RequestContext requestContext = new RequestContext(request);

        StrutsDelegate strutsDelegate = getStrutsDelegate();

        request.setAttribute(CSRF_TOKEN, request.getSession().getAttribute("csrf_token"));

        if (isSubmitted(form)) {
            ActionErrors errors = RhnValidationHelper.validateDynaActionForm(
                              this, form);
            if (!errors.isEmpty()) {
                strutsDelegate.saveMessages(request, errors);
            }
            else {

                ConfigureBootstrapCommand cmd = (ConfigureBootstrapCommand)
                getCommand(requestContext.getCurrentUser());
                cmd.setHostname(IDN.toASCII(form.getString(HOSTNAME)));
                cmd.setSslPath(form.getString(SSL_CERT));
                cmd.setSaltEnabled((Boolean) form.get(SALT));
                cmd.setEnableGpg((Boolean) form.get(ENABLE_GPG));
                cmd.setAllowConfigActions((Boolean) form.get(ALLOW_CONFIG_ACTIONS));
                cmd.setAllowRemoteCommands((Boolean) form.get(ALLOW_REMOTE_COMMANDS));
                cmd.setHttpProxy(form.getString(HTTP_PROXY));
                cmd.setHttpProxyUsername(form.getString(HTTP_PROXY_USERNAME));
                cmd.setHttpProxyPassword(form.getString(HTTP_PROXY_PASSWORD));
                ValidatorError[] verrors = cmd.storeConfiguration();

                if (verrors != null) {
                    errors = RhnValidationHelper.validatorErrorToActionErrors(verrors);
                    strutsDelegate.saveMessages(request, errors);
                }
                else {
                    createSuccessMessage(request, "bootstrap.config.success",
                                         addProtocolToHostname(cmd.getHostname()));
                }
            }
        }
        else {
            String caCertPath = CACertPathUtil.processCACertPath();
            form.set(HOSTNAME, IDN.toUnicode(ConfigDefaults.get().getHostname()));
            form.set(SSL_CERT, caCertPath);
            form.set(SALT, Boolean.TRUE);
            form.set(ENABLE_GPG, Boolean.TRUE);
            form.set(ALLOW_CONFIG_ACTIONS, Boolean.FALSE);
            form.set(ALLOW_REMOTE_COMMANDS, Boolean.FALSE);
        }
        return mapping.findForward(RhnHelper.DEFAULT_FORWARD);
    }

    // the protocol should be specified if anything on that host is viewed
    // through a browser. We also need https for SSL
    private String addProtocolToHostname(String name) {
        if (!name.startsWith("http")) {
            name = "https://" + name;
        }

        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getCommandClassName() {
        return Config.get().getString("web.com.redhat.rhn.frontend." +
           "action.satellite.BootstrapConfigAction.command",
           "com.redhat.rhn.manager.satellite.ConfigureBootstrapCommand");
    }
}

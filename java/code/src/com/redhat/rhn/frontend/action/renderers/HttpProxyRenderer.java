/**
 * Copyright (c) 2014 SUSE
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

package com.redhat.rhn.frontend.action.renderers;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.manager.setup.NCCClient;
import com.redhat.rhn.manager.setup.ProxySettingsDto;
import com.redhat.rhn.manager.setup.SetupWizardManager;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.httpclient.HttpClient;
import org.apache.log4j.Logger;
import org.directwebremoting.WebContext;
import org.directwebremoting.WebContextFactory;

public class HttpProxyRenderer {
    // The logger for this class
    private static final Logger logger = Logger.getLogger(HttpProxyRenderer.class);

    /**
     * Add a new pair of credentials and re-render the whole list.
     * @param settings map with the keys hostname, username, password
     * @return saved settings
     */
    public Map<String, String> saveProxySettings(Map<String, String> settingsMap) {
        // Find the current user
        WebContext webContext = WebContextFactory.get();
        HttpServletRequest request = webContext.getHttpServletRequest();
        RequestContext rhnContext = new RequestContext(request);
        User webUser = rhnContext.getCurrentUser();

        ProxySettingsDto settings = new ProxySettingsDto();
        settings.setHostname(settingsMap.get("hostname"));
        settings.setUsername(settingsMap.get("username"));
        settings.setPassword(settingsMap.get("password"));

        if (logger.isDebugEnabled()) {
            logger.debug("Saving proxy settings: " + settings.toString());
        }

        // TODO: Handle errors
        //ValidatorError[] errors =
        SetupWizardManager.storeProxySettings(settings, webUser, request);
        return settingsMap;
    }

    public Map retrieveProxySettings() {
        ProxySettingsDto settings = SetupWizardManager.getProxySettings();
        Map ret = new HashMap();
        ret.put("hostname", settings.getHostname());
        ret.put("username", settings.getUsername());
        ret.put("password", settings.getPassword());
        return ret;
    }

    public boolean verifyProxySettings() {
        NCCClient client = new NCCClient();
        boolean ret = client.ping();
        if (logger.isDebugEnabled()) {
            HttpClient http = client.getHttpClient();
            String proxyHost = http.getHostConfiguration().getProxyHost();
            logger.debug("Proxy verification for " + proxyHost + " is " + ret);
        }
        return ret;
    }
}

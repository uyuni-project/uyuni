/*
 * Copyright (c) 2015 SUSE LLC
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

package com.suse.manager.gatherer;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.util.http.HttpClientAdapter;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManager;
import com.redhat.rhn.manager.satellite.Executor;
import com.redhat.rhn.manager.satellite.SystemCommandExecutor;

import com.suse.manager.model.gatherer.GathererModule;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * Runs the virtual-host-gatherer command.
 */
public class GathererRunner {

    private static final String GATHERER_CMD = "/usr/bin/virtual-host-gatherer";
    private static final String LOG_DESTINATION = "/var/log/rhn/gatherer.log";
    /**
     * Logger for this class
     */
    private static final Logger LOGGER = LogManager.getLogger(GathererRunner.class);

    /**
     * Call gatherer --list-modules and return the result
     * @return the available modules with details
     */
    public Map<String, GathererModule> listModules() {
        Executor e = new SystemCommandExecutor();
        List<String> args = new LinkedList<>();
        args.add(GATHERER_CMD);
        args.add("--list-modules");
        args.add("--logfile");
        args.add(LOG_DESTINATION);

        int exitcode = e.execute(args.toArray(new String[0]));
        if (exitcode != 0) {
            LOGGER.error(e.getLastCommandErrorMessage());
            return null;
        }
        return new GathererJsonIO().readGathererModules(e.getLastCommandOutput());
    }

    /**
     * Runs virtual-host-gatherer against a set of Virtual Host Managers.
     *
     * @param vhms the virtual host managers
     * @return a map from virtual host manager names to
     * (virtual name, {@link HostJson}) pairs
     */
    public Map<String, Map<String, HostJson>> run(List<VirtualHostManager> vhms) {
        List<String> args = new LinkedList<>();
        args.add(GATHERER_CMD);
        args.add("--infile");
        args.add("-");
        args.add("--logfile");
        args.add(LOG_DESTINATION);

        Map<String, String> env = new HashMap<>(System.getenv());
        ConfigDefaults config = ConfigDefaults.get();
        String proxyHostname = config.getProxyHost();
        if (!StringUtils.isBlank(proxyHostname)) {
            URIBuilder builder = new URIBuilder();
            builder.setScheme("http");
            String proxyUsername = config.getProxyUsername();
            String proxyPassword = config.getProxyPassword();
            if (!StringUtils.isBlank(proxyUsername) &&
                    !StringUtils.isBlank(proxyPassword)) {
                builder.setUserInfo(proxyUsername, proxyPassword);
            }
            builder.setHost(proxyHostname);
            builder.setPort(config.getProxyPort());
            try {
                String uri = builder.build().toString();
                env.put("http_proxy", uri);
                env.put("https_proxy", uri);
                LOGGER.debug("Set http(s)_proxy to {}", uri);
            }
            catch (URISyntaxException e) {
                LOGGER.error("URI syntax exception when setting Proxy: {}", e.getMessage());
            }
        }
        int debuglevel = Config.get().getInt("debug", 0);
        if (debuglevel >= 2) {
            args.add("-v");
        }
        if (debuglevel >= 3) {
            args.add("-v");
        }
        String noProxy = Config.get().getString(HttpClientAdapter.NO_PROXY);
        if (!StringUtils.isEmpty(noProxy)) {
            env.put("no_proxy", noProxy);
            LOGGER.debug("Set no_proxy to {}", noProxy);
        }

        String[] envp = new String[env.size()];
        int i = 0;
        for (Map.Entry<String, String> e : env.entrySet()) {
            envp[i++] = e.getKey() + "=" + e.getValue();
        }

        Map<String, Map<String, HostJson>> hosts = null;
        Runtime r = Runtime.getRuntime();
        try {
            Process p = r.exec(args.toArray(new String[0]), envp);
            PrintWriter stdin = new PrintWriter(p.getOutputStream());
            stdin.println(new GathererJsonIO().toJson(vhms));
            stdin.flush();
            stdin.close();

            // Thread that reads process error output to avoid blocking
            Thread errStreamReader = new Thread(() -> {
                try {
                    String line = null;
                    BufferedReader inErr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                    while ((line = inErr.readLine()) != null) {
                        // do nothing, just consuming stderr output
                    }
                }
                catch (Exception e) {
                    LOGGER.error("Error reading stderr from external process", e);
                }
            });
            errStreamReader.start();

            InputStreamReader irr = new InputStreamReader(p.getInputStream());
            // We need to consume the input stream as it comes to avoid
            // a deadlock because the buffer size is full.
            BufferedReader br = new BufferedReader(irr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            hosts = new GathererJsonIO().readHosts(sb.toString());

            int exitCode = p.waitFor();
            if (exitCode != 0) {
                LOGGER.error("Error while calling the virtual-host-gatherer, exit code {}", exitCode);
                LOGGER.error("Please check the virtual-host-gatherer logfile.");
                return null;
            }
        }
        catch (IOException ioe) {
            LOGGER.error("execute(String[])", ioe);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("execute(String[])", e);
        }
        return hosts;
    }
}

/**
 * Copyright (c) 2009--2013 Red Hat, Inc.
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

package com.redhat.rhn.domain.kickstart;

import com.redhat.rhn.common.util.StringUtil;
import com.redhat.rhn.manager.kickstart.KickstartUrlHelper;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author paji
 * @version $Rev$
 */
public class RepoInfo {
    private String name;
    private String baseUrl;
    private boolean available;

    private static final Pattern NAME_REGEX =
        Pattern.compile("--name\\s*=\\s*(\\w+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern URL_REGEX =
        Pattern.compile("--baseurl\\s*=\\s*(\\S+)", Pattern.CASE_INSENSITIVE);

    private RepoInfo() {
    }
    /**
     * Creates a repo info object by parsing a KS Repo command
     * @param cmd the ksrepo command to parse
     * @return the RepoInfoobject associated to this command
     */
    public static RepoInfo parse(KickstartCommand cmd) {
        if (!cmd.getCommandName().isRepoCommand()) {
            String msg = "Only repo commands are handled here .." +
                                " Given command[%s] is not a repo command ";
            throw new UnsupportedOperationException(String.format(msg, cmd.toString()));

        }
        RepoInfo info = new RepoInfo();
        info.name = search(NAME_REGEX, cmd);
        info.baseUrl = search(URL_REGEX, cmd);
        return info;
    }

    /**
     * Create a new repo info object with the given name and base url
     * @param name the repo name
     * @param baseUrl the repo URL
     * @return the Repo info object
     */
    public static RepoInfo create(String name, String baseUrl) {
        RepoInfo info = new RepoInfo();
        info.name  = name;
        info.baseUrl = baseUrl;
        return info;
    }

    /**
     * Create a new repo info object with the given name and base url
     * @param name the repo name
     * @param baseUrl the repo URL
     * @param available is the repo available
     * @return the Repo info object
     */
    public static RepoInfo create(String name, String baseUrl, boolean available) {
        RepoInfo info = create(name, baseUrl);
        info.setAvailable(available);
        return info;
    }

    /**
     * @return the repo name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the repo URL
     */
    public String getUrl() {
        return baseUrl;
    }

    private static String search(Pattern regex, KickstartCommand command) {
        Matcher match = regex.matcher(command.getArguments());
        if (match.find()) {
            return match.group(1);
        }
        return "";
    }

    /**
     * Returns the actual command line that will be used in the
     * KickstartFormatter.
     * @param data KickstartData needed to generate the media url
     * @return the formatted command
     */
    public String getFormattedCommand(KickstartData data) {
        KickstartUrlHelper helper = new KickstartUrlHelper(data);
        return String.format("repo --name=%s --baseurl=%s", name,
                                    helper.getRepoUrl(this));
    }

    /**
     * updates the arguments in kickstart command.
     * @param command the command to set arguments on.
     */
    public void setArgumentsIn(KickstartCommand command) {
        command.setArguments(String.format("--name=%s --baseurl=%s", name, baseUrl));
    }

    /**
     * Gets the RepoInfo for the VT repo
     * @return the RepoInfo for the Vt repo
     */
    public static RepoInfo vt() {
        String name = "VT";
        return create(name, name);
    }

    /**
     * Returns all the 4 standard repos available to rhel 5
     * cluster, clusterstorage, workstation and VT
     * @param tree the kickstartable tree
     * @return the standard repos..
     */
    public static List<RepoInfo> getStandardRepos(KickstartableTree  tree) {
        ArrayList<RepoInfo> repoList = new ArrayList<RepoInfo>();
        String[] repos = {"Cluster", "ClusterStorage", "HighAvailability",
                        "LoadBalancer", "ResilientStorage", "VT", "Workstation",
                        "ScalableFileSystem",
                        "addons/HighAvailability", "addons/ResilientStorage"};

        for (String repo : repos) {
            File file = new File(StringUtil.addPath(tree.getAbsolutePath(), repo));
            if (file.exists()) {
                File repodata = new File(file.getPath() +
                    File.separator + "repodata" +
                    File.separator + "repomd.xml");
                repoList.add(create(repo.replace("addons/", ""), repo, repodata.exists()));
            }
        }
        return repoList;
    }

    /**
     * @return the available
     */
    public boolean isAvailable() {
        return available;
    }
    /**
     * @param availableIn the available to set
     */
    public void setAvailable(boolean availableIn) {
        this.available = availableIn;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof RepoInfo)) {
            return false;
        }
        RepoInfo that = (RepoInfo)obj;

        EqualsBuilder builder = new EqualsBuilder();
        builder.append(name, that.name);
        builder.append(baseUrl, that.baseUrl);
        return builder.isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(name);
        builder.append(baseUrl);
        return builder.toHashCode();
    }
}

/**
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
package com.suse.manager.webui.utils;

import java.util.Optional;

/**
 * Representation of an entry in a repo file.
 */
public class RepoFileEntry {

    private final String alias;
    private final String name;
    private final boolean enabled;
    private final boolean autoRefresh;
    private final String baseUrl;
    private final String type;
    private final boolean gpgCheck;
    private final boolean repoGpgCheck;
    private final boolean packageGpgCheck;
    private final Optional<String> service;

    /**
     * Creates a new RepoFileEntry
     *
     * @param inAlias alias for this repo entry
     * @param inName name of this repo entry
     * @param inEnabled if the repo entry is enabled
     * @param inAutoRefresh if the repo entry gets automatically refreshed
     * @param inBaseUrl the base url of this repo entry
     * @param inType the type of this repo entry
     * @param inGpgCheck if the repo checks gpg
     * @param inRepoGpgCheck if the repo checks gpg
     * @param inPackageGpgCheck if the package get gpg checked
     * @param inService service associated with this repo entry
     */
    public RepoFileEntry(String inAlias, String inName, boolean inEnabled,
                         boolean inAutoRefresh, String inBaseUrl, String inType,
                         boolean inGpgCheck, boolean inRepoGpgCheck,
                         boolean inPackageGpgCheck, Optional<String> inService) {
        this.alias = inAlias;
        this.name = inName;
        this.enabled = inEnabled;
        this.autoRefresh = inAutoRefresh;
        this.baseUrl = inBaseUrl;
        this.type = inType;
        this.gpgCheck = inGpgCheck;
        this.repoGpgCheck = inRepoGpgCheck;
        this.packageGpgCheck = inPackageGpgCheck;
        this.service = inService;
    }

    /**
     * @return the alias of this RepoFileEntry
     */
    public String getAlias() {
        return alias;
    }

    /**
     * @return the alias of this RepoFileEntry
     */
    public String getName() {
        return name;
    }

    /**
     * @return the alias of this RepoFileEntry
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @return the alias of this RepoFileEntry
     */
    public boolean isAutoRefresh() {
        return autoRefresh;
    }

    /**
     * @return the alias of this RepoFileEntry
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * @return the alias of this RepoFileEntry
     */
    public String getType() {
        return type;
    }

    /**
     * @return the alias of this RepoFileEntry
     */
    public boolean isGpgCheck() {
        return gpgCheck;
    }

    /**
     * @return the alias of this RepoFileEntry
     */
    public boolean isRepoGpgCheck() {
        return repoGpgCheck;
    }

    /**
     * @return the alias of this RepoFileEntry
     */
    public boolean isPackageGpgCheck() {
        return packageGpgCheck;
    }

    /**
     * @return the alias of this RepoFileEntry
     */
    public Optional<String> getService() {
        return service;
    }

    /**
     * Creates a string containing this RepoFileEntries contents
     * in the format of a .repo file.
     *
     * @return the .repo file formatted string
     */
    public String fileFormat() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        builder.append(getAlias());
        builder.append("]\n");

        builder.append("name=");
        builder.append(getName());
        builder.append("\n");

        builder.append("enabled=");
        builder.append(isEnabled() ? 1 : 0);
        builder.append("\n");

        builder.append("autorefresh=");
        builder.append(isAutoRefresh() ? 1 : 0);
        builder.append("\n");

        builder.append("baseurl=");
        builder.append(getBaseUrl());
        builder.append("\n");

        builder.append("type=");
        builder.append(getType());
        builder.append("\n");

        builder.append("gpgcheck=");
        builder.append(isGpgCheck() ? 1 : 0);
        builder.append("\n");

        builder.append("repo_gpgcheck=");
        builder.append(isRepoGpgCheck() ? 1 : 0);
        builder.append("\n");

        builder.append("pkg_gpgcheck=");
        builder.append(isPackageGpgCheck() ? 1 : 0);
        builder.append("\n");

        getService().ifPresent(s -> {
            builder.append("service=");
            builder.append(s);
            builder.append("\n");
        });
        return builder.toString();
    }
}

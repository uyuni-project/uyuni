package com.suse.manager.webui.utils;

import java.util.Optional;

/**
 *
 */
public class RepoFile {

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

    public RepoFile(String alias, String name, boolean enabled, boolean autoRefresh, String baseUrl, String type, boolean gpgCheck, boolean repoGpgCheck, boolean packageGpgCheck, Optional<String> service) {
        this.alias = alias;
        this.name = name;
        this.enabled = enabled;
        this.autoRefresh = autoRefresh;
        this.baseUrl = baseUrl;
        this.type = type;
        this.gpgCheck = gpgCheck;
        this.repoGpgCheck = repoGpgCheck;
        this.packageGpgCheck = packageGpgCheck;
        this.service = service;
    }

    public String getAlias() {
        return alias;
    }

    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isAutoRefresh() {
        return autoRefresh;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getType() {
        return type;
    }

    public boolean isGpgCheck() {
        return gpgCheck;
    }

    public boolean isRepoGpgCheck() {
        return repoGpgCheck;
    }

    public boolean isPackageGpgCheck() {
        return packageGpgCheck;
    }

    public Optional<String> getService() {
        return service;
    }

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

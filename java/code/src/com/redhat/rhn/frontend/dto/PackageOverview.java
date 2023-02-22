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
package com.redhat.rhn.frontend.dto;

import com.redhat.rhn.common.db.datasource.Row;

import org.apache.commons.lang3.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.Tuple;

/**
 * PackageOverview
 */
public class PackageOverview extends BaseTupleDto {

    private Long id;
    private String packageName;
    private String summary;
    private String description;
    private String packageNvre;
    private String nvrea;
    private List<Row> packageChannels;
    private String packageArch;
    private String provider;
    private String version;
    private String epoch;
    private String release;
    private Boolean retracted;


    /**
     * Default constructor for hibernate
     */
    public PackageOverview() {
    }

    /**
     * Constructor using a tuple for paged SQL queries
     * @param tuple the tuple from hibernate
     */
    public PackageOverview(Tuple tuple) {
        id = getTupleValue(tuple, "id", Number.class).map(Number::longValue).orElse(null);
        nvrea = getTupleValue(tuple, "nvrea", String.class).orElse(null);
        provider = getTupleValue(tuple, "provider", String.class).orElse(null);
        packageChannels = getTupleValue(tuple, "channels", String.class)
                .map(value ->
                        Arrays.stream(value.split(","))
                                .map(c -> new Row(Map.of("name", c)))
                                .collect(Collectors.toList()))
                .orElse(new ArrayList<>());
    }

    /**
     * @return Returns the packageChannels.
     */
    public List<Row> getPackageChannels() {
        return packageChannels;
    }

    /**
     * @param packageChannelsIn The packageChannels to set.
     */
    public void setPackageChannels(List<Row> packageChannelsIn) {
        this.packageChannels = packageChannelsIn;
    }

    /**
     * @return Returns the nvrea.
     */
    public String getNvrea() {
        return nvrea;
    }

    /**
     * @param nvreaIn The nvrea to set.
     */
    public void setNvrea(String nvreaIn) {
        this.nvrea = nvreaIn;
    }

    /**
     * @return Returns the packageNvre.
     */
    public String getPackageNvre() {
        return packageNvre;
    }

    /**
     * @param p The packageNvre to set.
     */
    public void setPackageNvre(String p) {
        this.packageNvre = p;
    }

    /**
     * @return Returns the id.
     */
    @Override
    public Long getId() {
        return id;
    }
    /**
     * @param idIn The id to set.
     */
    public void setId(Long idIn) {
        this.id = idIn;
    }
    /**
     * @param descriptionIn The description to set.
     */
    public void setDescription(String descriptionIn) {
        this.description = descriptionIn;
    }
    /**
     * @return Returns the description.
     */
    public String getDescription() {
        return StringUtils.defaultString(description).trim();
    }
    /**
     * @return Returns the summary.
     */
    public String getSummary() {
        return StringUtils.defaultString(summary).trim();
    }
    /**
     * @param summaryIn The summary to set.
     */
    public void setSummary(String summaryIn) {
        this.summary = summaryIn;
    }
    /**
     * @return Returns the packageName.
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * Return the UrlEncoded package name
     * @return urleencoded package name
     */
    public String getUrlEncodedPackageName() {
        if (this.packageName != null) {
            return URLEncoder.encode(packageName, StandardCharsets.UTF_8);
        }
        return null;
    }

    /**
     * @param packageNameIn The packageName to set.
     */
    public void setPackageName(String packageNameIn) {
        this.packageName = packageNameIn;
    }


    /**
     * @return Returns the packageArch.
     */
    public String getPackageArch() {
        return packageArch;
    }


    /**
     * @param packageArchIn The packageArch to set.
     */
    public void setPackageArch(String packageArchIn) {
        this.packageArch = packageArchIn;
    }


    /**
     * @return Returns the provider.
     */
    public String getProvider() {
        return provider == null ? "Unknown" : provider;
    }


    /**
     * @param providerIn The provider to set.
     */
    public void setProvider(String providerIn) {
        this.provider = providerIn;
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param versionIn the version to set
     */
    public void setVersion(String versionIn) {
        this.version = versionIn;
    }

    /**
     * @return the epoch
     */
    public String getEpoch() {
        return epoch;
    }

    /**
     * @param epochIn the epoch to set
     */
    public void setEpoch(String epochIn) {
        this.epoch = epochIn;
    }

    /**
     * @return the release
     */
    public String getRelease() {
        return release;
    }

    /**
     * @param releaseIn the release to set
     */
    public void setRelease(String releaseIn) {
        this.release = releaseIn;
    }

    /**
     * Gets the retracted.
     *
     * @return retracted
     */
    public Boolean getRetracted() {
        return retracted;
    }

    /**
     * Sets the retracted.
     *
     * @param retractedIn the retracted
     */
    public void setRetracted(Boolean retractedIn) {
        retracted = retractedIn;
    }

}

/*
 * Copyright (c) 2025 SUSE LLC
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

package com.suse.proxy.get;

import static com.suse.utils.Predicates.isAbsent;
import static com.suse.utils.Predicates.isProvided;

import com.redhat.rhn.common.RhnErrorReport;

import com.suse.manager.api.ParseException;
import com.suse.proxy.ProxyContainerImagesEnum;
import com.suse.proxy.ProxyRegistryUtils;
import com.suse.proxy.ProxyRegistryUtilsImpl;
import com.suse.proxy.RegistryUrl;
import com.suse.rest.RestClientException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Contains the logic to retrieve the tags from the registry.
 * May them be by using a specific URL or a base URL.
 * In case of a base URL, it will retrieve the common tags among the proxy images.
 * By its turn, the proxy images are defined in the {@link ProxyContainerImagesEnum}.
 */
public class ProxyConfigGetRegistryTags {
    private static final Logger LOG = LogManager.getLogger(ProxyConfigGetRegistryTags.class);
    public static final String PATH_SEPARATOR = "/";

    private final String registryUrlAsString;
    private final boolean isExact;
    private final ProxyRegistryUtils proxyRegistryUtils;
    private RhnErrorReport errorReport;
    private List<String> tags;

    /**
     * Constructor
     *
     * @param registryUrlAsStringIn the registry URL
     * @param isExactIn             flag indicating if the URL is exact (in opposition to a base url)
     */
    public ProxyConfigGetRegistryTags(String registryUrlAsStringIn, boolean isExactIn) {
        this(registryUrlAsStringIn, isExactIn, new ProxyRegistryUtilsImpl());
    }

    /**
     * Full constructor
     *
     * @param registryUrlAsStringIn the registry URL
     * @param isExactIn             flag indicating if the URL is exact (in opposition to a base url)
     * @param proxyRegistryUtilsIn  the proxy registry utils implementation
     */
    public ProxyConfigGetRegistryTags(
            String registryUrlAsStringIn,
            boolean isExactIn,
            ProxyRegistryUtils proxyRegistryUtilsIn
    ) {
        this.registryUrlAsString = registryUrlAsStringIn;
        this.isExact = isExactIn;
        this.proxyRegistryUtils = proxyRegistryUtilsIn;
    }

    /**
     * Retrieves the tags from the registry.
     */
    public void retrieveTags() {
        this.errorReport = new RhnErrorReport();
        try {
            this.tags = isExact ? getTagsFromRegistry() : getCommonTagsFromRegistry();
        }
        catch (URISyntaxException e) {
            getErrorReport().register("Invalid URL");
            LOG.debug("Invalid URL: {}", e.getInput(), e);
            return;
        }
        catch (ParseException e) {
            getErrorReport().register("Error parsing response");
            LOG.debug("Error parsing response: {}", e.getMessage(), e);
            return;
        }
        catch (RestClientException e) {
            getErrorReport().register("Error retrieving tags");
            LOG.debug("Error retrieving tags: {}", e.getMessage(), e);
            return;
        }

        if (tags != null && tags.isEmpty()) {
            getErrorReport().register("No tags found on registry");
            LOG.debug("No tags found on registry {}", this.registryUrlAsString);
        }
    }

    /**
     * Get the tags from the registry when the URL for a specific image.
     * Eg:
     * - https://registry.opensuse.org/uyuni/proxy-httpd
     */
    private List<String> getTagsFromRegistry() throws URISyntaxException, ParseException {
        RegistryUrl registryUrl = new RegistryUrl(this.registryUrlAsString);
        return proxyRegistryUtils.getTags(registryUrl);
    }

    /**
     * Retrieves the common tags among the proxy images from the given base registry URL.
     */
    @SuppressWarnings("java:S1168") // we want to distinguish between null and empty
    private List<String> getCommonTagsFromRegistry() throws URISyntaxException, ParseException {
        RegistryUrl registryUrl = new RegistryUrl(this.registryUrlAsString);

        List<String> repositories = proxyRegistryUtils.getRepositories(registryUrl);
        if (repositories.isEmpty()) {
            LOG.debug("No repositories found on registry {}", this.registryUrlAsString);
            getErrorReport().register("No repositories found on registry");
            return null;
        }

        // Check if all proxy images are present in the catalog
        Set<String> repositorySet = new HashSet<>(repositories);
        Set<String> proxyImageList = new HashSet<>(ProxyContainerImagesEnum.values().length);

        String path = registryUrl.getPath();
        String pathPrefix = isProvided(path) ? path.substring(1) + PATH_SEPARATOR : "";
        for (ProxyContainerImagesEnum proxyImage : ProxyContainerImagesEnum.values()) {
            proxyImageList.add(pathPrefix + proxyImage.getImageName());
        }

        if (!repositorySet.containsAll(proxyImageList)) {
            LOG.debug("Cannot find all images in catalog using registryUrl {}", this.registryUrlAsString);
            getErrorReport().register("Cannot find all images in catalog");
            return null;
        }

        // Collect common tags among proxy images
        Set<String> commonTags = null;
        for (ProxyContainerImagesEnum proxyImage : ProxyContainerImagesEnum.values()) {
            RegistryUrl imageRegistryUrl = new RegistryUrl(registryUrl.getUrl() + "/" + proxyImage.getImageName());
            List<String> tagsFromImage = proxyRegistryUtils.getTags(imageRegistryUrl);

            if (tagsFromImage == null || tagsFromImage.isEmpty()) {
                LOG.debug("No tags found on registry {}", imageRegistryUrl);
                getErrorReport().register("No common tags found among proxy images");
                return null;
            }

            Set<String> tagSet = new HashSet<>(tagsFromImage);
            if (commonTags == null) {
                commonTags = new HashSet<>(tagSet);
            }
            else {
                commonTags.retainAll(tagSet);
                if (commonTags.isEmpty()) {
                    break;
                }
            }
        }

        if (isAbsent(commonTags)) {
            LOG.debug("No common tags found among proxy images using registryUrl {}", this.registryUrlAsString);
            getErrorReport().register("No common tags found among proxy images");
            return null;
        }

        List<String> commonTagsList = new ArrayList<>(commonTags);
        Collections.sort(commonTagsList);
        return commonTagsList;
    }

    public String getRegistryUrlAsString() {
        return registryUrlAsString;
    }

    public boolean isExact() {
        return isExact;
    }

    public RhnErrorReport getErrorReport() {
        return errorReport;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tagsIn) {
        tags = tagsIn;
    }

}

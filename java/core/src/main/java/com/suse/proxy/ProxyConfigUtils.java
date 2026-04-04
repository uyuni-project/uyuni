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

package com.suse.proxy;

import static com.redhat.rhn.common.ExceptionMessage.NOT_INSTANTIABLE;
import static com.suse.proxy.ProxyContainerImagesEnum.PROXY_HTTPD;
import static com.suse.proxy.ProxyContainerImagesEnum.PROXY_SALT_BROKER;
import static com.suse.proxy.ProxyContainerImagesEnum.PROXY_SQUID;
import static com.suse.proxy.ProxyContainerImagesEnum.PROXY_SSH;
import static com.suse.proxy.ProxyContainerImagesEnum.PROXY_TFTPD;
import static com.suse.utils.Predicates.allAbsent;
import static com.suse.utils.Predicates.isAbsent;
import static com.suse.utils.Predicates.isProvided;

import com.redhat.rhn.common.util.FileUtils;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.server.Pillar;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.manager.rhnpackage.PackageManager;

import com.suse.manager.webui.utils.YamlHelper;
import com.suse.proxy.model.ProxyConfig;
import com.suse.proxy.model.ProxyConfigImage;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class for handling Proxy Config
 * Includes relevant constants and mappings from DTO / Pillar
 * Pillar entries for the registry URLs will follow the example format:
 * { ..., "registries": { "proxy-httpd": { "url": "https://.../proxy-httpd", "tag": "latest" }, ... } }
 * names for the registry entries are defined in ProxyContainerImagesEnum image names
 */
public class ProxyConfigUtils {

    private ProxyConfigUtils() {
        throw new UnsupportedOperationException(NOT_INSTANTIABLE);
    }

    public static final String MGRPXY = "mgrpxy";

    //
    public static final String PROXY_PILLAR_CATEGORY = "proxy";

    // Field names and values used in the form and also in the pillar
    public static final String SERVER_ID_FIELD = "serverId";
    public static final String PROXY_FQDN_FIELD = "proxyFQDN";
    public static final String PARENT_FQDN_FIELD = "parentFQDN";
    public static final String PROXY_PORT_FIELD = "proxyPort";
    public static final String MAX_CACHE_FIELD = "maxSquidCacheSize";
    public static final String EMAIL_FIELD = "proxyAdminEmail";
    public static final String USE_CERTS_MODE_FIELD = "useCertsMode";
    public static final String USE_CERTS_MODE_KEEP = "keep";
    public static final String USE_CERTS_MODE_REPLACE = "replace";
    public static final String ROOT_CA_FIELD = "rootCA";
    public static final String INTERMEDIATE_CAS_FIELD = "intermediateCAs";
    public static final String PROXY_CERT_FIELD = "proxyCertificate";
    public static final String PROXY_KEY_FIELD = "proxyKey";
    public static final String SOURCE_MODE_FIELD = "sourceMode";
    public static final String SOURCE_MODE_RPM = "rpm";
    public static final String SOURCE_MODE_REGISTRY = "registry";
    public static final String REGISTRY_MODE = "registryMode";
    public static final String REGISTRY_MODE_SIMPLE = "simple";
    public static final String REGISTRY_MODE_ADVANCED = "advanced";
    public static final String REGISTRY_BASE_URL = "registryBaseURL";
    public static final String REGISTRY_BASE_TAG = "registryBaseTag";
    public static final String PROXY_SSH_PUB = "proxySshPub";
    public static final String PROXY_SSH_PRIV = "proxySshPriv";
    public static final String PROXY_SSH_PARENT_PUB = "parentSshPub";

    // Registry entries
    public static final String PILLAR_REGISTRY_ENTRY = "registries";
    public static final String PILLAR_REGISTRY_URL_ENTRY = "url";
    public static final String PILLAR_REGISTRY_TAG_ENTRY = "tag";

    /**
     * Maps a minion pillar ProxyConfig
     *
     * @param rootPillar the root pillar
     * @return the ProxyConfig
     */
    public static ProxyConfig proxyConfigFromPillar(Pillar rootPillar) {
        ProxyConfig proxyConfig = new ProxyConfig();

        if (rootPillar == null  || rootPillar.getPillar() == null) {
            return proxyConfig;
        }
        Map<String, Object> pillar = rootPillar.getPillar();

        proxyConfig.setServerId((Long) pillar.get(SERVER_ID_FIELD));
        proxyConfig.setProxyFqdn(Objects.toString(pillar.get(PROXY_FQDN_FIELD), null));
        proxyConfig.setParentFqdn(Objects.toString(pillar.get(PARENT_FQDN_FIELD), null));
        proxyConfig.setProxyPort((Integer) pillar.get(PROXY_PORT_FIELD));
        proxyConfig.setMaxCache((Integer) pillar.get(MAX_CACHE_FIELD));
        proxyConfig.setEmail(Objects.toString(pillar.get(EMAIL_FIELD), null));
        proxyConfig.setRootCA(Objects.toString(pillar.get(ROOT_CA_FIELD), null));
        proxyConfig.setIntermediateCAs((List<String>) pillar.get(INTERMEDIATE_CAS_FIELD));
        proxyConfig.setProxyCert(Objects.toString(pillar.get(PROXY_CERT_FIELD), null));
        proxyConfig.setProxyKey(Objects.toString(pillar.get(PROXY_KEY_FIELD), null));

        proxyConfig.setProxySshPub(Objects.toString(pillar.get(PROXY_SSH_PUB), null));
        proxyConfig.setProxySshPriv(Objects.toString(pillar.get(PROXY_SSH_PRIV), null));
        proxyConfig.setParentSshPub(Objects.toString(pillar.get(PROXY_SSH_PARENT_PUB), null));

        Map<String, Object> registries = (Map<String, Object>) pillar.get(PILLAR_REGISTRY_ENTRY);
        if (isProvided(registries)) {
            Map<String, String> httpdImageEntry = (Map<String, String>) registries.get(PROXY_HTTPD.getImageName());
            Map<String, String> saltBrokerImageEntry =
                    (Map<String, String>) registries.get(PROXY_SALT_BROKER.getImageName());
            Map<String, String> squidImageEntry = (Map<String, String>) registries.get(PROXY_SQUID.getImageName());
            Map<String, String> sshImageEntry = (Map<String, String>) registries.get(PROXY_SSH.getImageName());
            Map<String, String> tftpfImageEntry = (Map<String, String>) registries.get(PROXY_TFTPD.getImageName());

            if (isProvided(httpdImageEntry)) {
                proxyConfig.setHttpdImage(
                        new ProxyConfigImage(
                                httpdImageEntry.get(PILLAR_REGISTRY_URL_ENTRY),
                                httpdImageEntry.get(PILLAR_REGISTRY_TAG_ENTRY)
                        )
                );
            }
            if (isProvided(saltBrokerImageEntry)) {
                proxyConfig.setSaltBrokerImage(
                        new ProxyConfigImage(
                                saltBrokerImageEntry.get(PILLAR_REGISTRY_URL_ENTRY),
                                saltBrokerImageEntry.get(PILLAR_REGISTRY_TAG_ENTRY)
                        )
                );
            }
            if (isProvided(squidImageEntry)) {
                proxyConfig.setSquidImage(
                        new ProxyConfigImage(
                                squidImageEntry.get(PILLAR_REGISTRY_URL_ENTRY),
                                squidImageEntry.get(PILLAR_REGISTRY_TAG_ENTRY)
                        )
                );
            }
            if (isProvided(sshImageEntry)) {
                proxyConfig.setSshImage(
                        new ProxyConfigImage(
                                sshImageEntry.get(PILLAR_REGISTRY_URL_ENTRY),
                                sshImageEntry.get(PILLAR_REGISTRY_TAG_ENTRY)
                        )
                );
            }
            if (isProvided(tftpfImageEntry)) {
                proxyConfig.setTftpdImage(
                        new ProxyConfigImage(
                                tftpfImageEntry.get(PILLAR_REGISTRY_URL_ENTRY),
                                tftpfImageEntry.get(PILLAR_REGISTRY_TAG_ENTRY)
                        )
                );
            }
        }

        return proxyConfig;
    }

    /**
     * Stores ProxyConfig to pillar
     *
     * @param proxyConfig ProxyConfig to store
     * @return the pillar object with stored proxyConfig
     */
    public static Pillar proxyConfigToPillar(ProxyConfig proxyConfig) {
        Pillar pillar = new Pillar(ProxyConfigUtils.PROXY_PILLAR_CATEGORY, new HashMap<>());

        if (proxyConfig == null) {
            return pillar;
        }

        pillar.add(SERVER_ID_FIELD, proxyConfig.getServerId());
        pillar.add(PROXY_FQDN_FIELD, proxyConfig.getProxyFqdn());
        pillar.add(PARENT_FQDN_FIELD, proxyConfig.getParentFqdn());
        pillar.add(PROXY_PORT_FIELD, proxyConfig.getProxyPort());
        pillar.add(MAX_CACHE_FIELD, proxyConfig.getMaxCache());
        pillar.add(EMAIL_FIELD, proxyConfig.getEmail());
        pillar.add(ROOT_CA_FIELD, proxyConfig.getRootCA());
        pillar.add(INTERMEDIATE_CAS_FIELD, proxyConfig.getIntermediateCAs());
        pillar.add(PROXY_CERT_FIELD, proxyConfig.getProxyCert());
        pillar.add(PROXY_KEY_FIELD, proxyConfig.getProxyKey());

        pillar.add(PROXY_SSH_PUB, proxyConfig.getProxySshPub());
        pillar.add(PROXY_SSH_PRIV, proxyConfig.getProxySshPriv());
        pillar.add(PROXY_SSH_PARENT_PUB, proxyConfig.getParentSshPub());

        Pillar registries = new Pillar();

        ProxyConfigImage httpImageEntry = proxyConfig.getHttpdImage();
        if (isProvided(httpImageEntry)) {
            Pillar subEntry = new Pillar();
            subEntry.add(PILLAR_REGISTRY_URL_ENTRY, httpImageEntry.getUrl());
            subEntry.add(PILLAR_REGISTRY_TAG_ENTRY, httpImageEntry.getTag());
            registries.add(PROXY_HTTPD.getImageName(), subEntry);
        }

        ProxyConfigImage saltBrokerImageEntry = proxyConfig.getSaltBrokerImage();
        if (isProvided(saltBrokerImageEntry)) {
            Pillar subEntry = new Pillar();
            subEntry.add(PILLAR_REGISTRY_URL_ENTRY, saltBrokerImageEntry.getUrl());
            subEntry.add(PILLAR_REGISTRY_TAG_ENTRY, saltBrokerImageEntry.getTag());
            registries.add(PROXY_SALT_BROKER.getImageName(), subEntry);
        }

        ProxyConfigImage squidImageEntry = proxyConfig.getSaltBrokerImage();
        if (isProvided(squidImageEntry)) {
            Pillar subEntry = new Pillar();
            subEntry.add(PILLAR_REGISTRY_URL_ENTRY, squidImageEntry.getUrl());
            subEntry.add(PILLAR_REGISTRY_TAG_ENTRY, squidImageEntry.getTag());
            registries.add(PROXY_SQUID.getImageName(), subEntry);
        }

        ProxyConfigImage sshImageEntry = proxyConfig.getSaltBrokerImage();
        if (isProvided(sshImageEntry)) {
            Pillar subEntry = new Pillar();
            subEntry.add(PILLAR_REGISTRY_URL_ENTRY, sshImageEntry.getUrl());
            subEntry.add(PILLAR_REGISTRY_TAG_ENTRY, sshImageEntry.getTag());
            registries.add(PROXY_SSH.getImageName(), subEntry);
        }

        ProxyConfigImage tftpImageEntry = proxyConfig.getSaltBrokerImage();
        if (isProvided(tftpImageEntry)) {
            Pillar subEntry = new Pillar();
            subEntry.add(PILLAR_REGISTRY_URL_ENTRY, tftpImageEntry.getUrl());
            subEntry.add(PILLAR_REGISTRY_TAG_ENTRY, tftpImageEntry.getTag());
            registries.add(PROXY_TFTPD.getImageName(), subEntry);
        }

        pillar.add(PILLAR_REGISTRY_ENTRY, registries);
        return pillar;
    }

    /**
     * Maps a ProxyConfig
     *
     * @param proxyConfig the ProxyConfig
     * @return the data map
     */
    public static Map<String, Object> dataMapFromProxyConfig(ProxyConfig proxyConfig) {
        Map<String, Object> data = new HashMap<>();

        if (isAbsent(proxyConfig)) {
            return data;
        }

        data.put(SERVER_ID_FIELD, proxyConfig.getServerId());
        data.put(PROXY_FQDN_FIELD, proxyConfig.getProxyFqdn());
        data.put(PARENT_FQDN_FIELD, proxyConfig.getParentFqdn());
        data.put(PROXY_PORT_FIELD, proxyConfig.getProxyPort());
        data.put(MAX_CACHE_FIELD, proxyConfig.getMaxCache());
        data.put(EMAIL_FIELD, proxyConfig.getEmail());

        data.put(USE_CERTS_MODE_FIELD, USE_CERTS_MODE_KEEP);

        ProxyConfigImage httpdImage = proxyConfig.getHttpdImage();
        ProxyConfigImage saltBrokerImage = proxyConfig.getSaltBrokerImage();
        ProxyConfigImage squidImage = proxyConfig.getSquidImage();
        ProxyConfigImage sshImage = proxyConfig.getSshImage();
        ProxyConfigImage tftpdImage = proxyConfig.getTftpdImage();

        // If all images are absent, we assume the source mode is RPM
        if (allAbsent(httpdImage, saltBrokerImage, squidImage, sshImage, tftpdImage)) {
            data.put(SOURCE_MODE_FIELD, ProxyConfigUtils.SOURCE_MODE_RPM);
            return data;
        }

        // Otherwise we assume the source mode is registry
        data.put(SOURCE_MODE_FIELD, ProxyConfigUtils.SOURCE_MODE_REGISTRY);

        // Check if all images have the same tag and prefix
        boolean isRegistryModeSimple =
                ProxyConfigUtils.getCommonTag(httpdImage, saltBrokerImage, squidImage, sshImage, tftpdImage)
                .map(tag ->
                        ProxyConfigUtils.getCommonPrefix(httpdImage, saltBrokerImage, squidImage, sshImage, tftpdImage)
                        .map(prefix -> {
                            data.put(REGISTRY_MODE, ProxyConfigUtils.REGISTRY_MODE_SIMPLE);
                            data.put(REGISTRY_BASE_TAG, tag);
                            data.put(REGISTRY_BASE_URL, prefix);
                            return true;
                        })
                        .orElse(false))
                .orElse(false);

        if (isRegistryModeSimple) {
            return data;
        }

        // Otherwise registry mode is advanced
        data.put(REGISTRY_MODE, ProxyConfigUtils.REGISTRY_MODE_ADVANCED);
        if (isProvided(httpdImage)) {
            data.put(PROXY_HTTPD.getUrlField(), httpdImage.getUrl());
            data.put(PROXY_HTTPD.getTagField(), httpdImage.getTag());
        }

        if (isProvided(saltBrokerImage)) {
            data.put(PROXY_SALT_BROKER.getUrlField(), saltBrokerImage.getUrl());
            data.put(PROXY_SALT_BROKER.getTagField(), saltBrokerImage.getTag());
        }

        if (isProvided(squidImage)) {
            data.put(PROXY_SQUID.getUrlField(), squidImage.getUrl());
            data.put(PROXY_SQUID.getTagField(), squidImage.getTag());
        }

        if (isProvided(sshImage)) {
            data.put(PROXY_SSH.getUrlField(), sshImage.getUrl());
            data.put(PROXY_SSH.getTagField(), sshImage.getTag());
        }

        if (isProvided(tftpdImage)) {
            data.put(PROXY_TFTPD.getUrlField(), tftpdImage.getUrl());
            data.put(PROXY_TFTPD.getTagField(), tftpdImage.getTag());
        }

        return data;
    }

    /**
     * Load collected files to the ProxyConfig
     *
     * @param configPath path to the general configuration file
     * @param httpdPath path to the httpd configuration file
     * @param sshPath path to the ssh configuration file
     * @return map object with parsed data
     */
    public static ProxyConfig loadFilesToProxyConfig(Path configPath, Path httpdPath, Path sshPath) {
        ProxyConfig proxyConfig = new ProxyConfig();

        Map<String, String> httpd = YamlHelper.loadAs(
                FileUtils.readStringFromFile(httpdPath.toString()), Map.class);
        proxyConfig.setProxyCert(httpd.get("server_crt"));
        proxyConfig.setProxyKey(httpd.get("server_key"));

        Map<String, String> ssh = YamlHelper.loadAs(
                FileUtils.readStringFromFile(sshPath.toString()), Map.class);
        proxyConfig.setProxySshPriv(ssh.get("server_ssh_push"));
        proxyConfig.setProxySshPub(ssh.get("server_ssh_push_pub"));
        proxyConfig.setParentSshPub(ssh.get("server_ssh_key_pub"));

        Map<String, Object> config = YamlHelper.loadAs(
                FileUtils.readStringFromFile(configPath.toString()), Map.class);
        proxyConfig.setRootCA(config.get("ca_crt").toString());
        proxyConfig.setEmail(config.get("email").toString());
        proxyConfig.setMaxCache((Integer)config.get("max_cache_size_mb"));
        proxyConfig.setParentFqdn(config.get("server").toString());
        proxyConfig.setProxyFqdn(config.get("proxy_fqdn").toString());
        return proxyConfig;
    }

    /**
     * Verifies if all the images have the same (non-nullable) tag
     *
     * @param images the images to be verified
     * @return the common tag if all images have the same tag, otherwise empty
     */
    public static Optional<String> getCommonTag(ProxyConfigImage... images) {
        if (isAbsent(images)) {
            return Optional.empty();
        }

        Set<String> tags = new HashSet<>();
        for (ProxyConfigImage image : images) {
            if (image == null || image.getTag() == null) {
                return Optional.empty();
            }
            tags.add(image.getTag());
        }

        return tags.size() == 1 ? Optional.of(tags.iterator().next()) : Optional.empty();
    }

    /**
     * Verifies if all the images url have a common prefix
     *
     * @param images the images to be verified
     * @return the common prefix if all images have the same prefix, otherwise empty
     */
    public static Optional<String> getCommonPrefix(ProxyConfigImage... images) {
        if (isAbsent(images)) {
            return Optional.empty();
        }

        Set<String> allowedImageNames = Arrays.stream(ProxyContainerImagesEnum.values())
                .map(ProxyContainerImagesEnum::getImageName)
                .collect(Collectors.toSet());

        Set<String> imageUrlPrefixes = new HashSet<>();

        for (ProxyConfigImage image : images) {
            if (image == null || image.getUrl() == null) {
                return Optional.empty();
            }

            String url = image.getUrl();
            int lastSlash = url.lastIndexOf('/');

            // If there is no slash, the image name is already invalid
            if (lastSlash == -1) {
                return Optional.empty();
            }

            // If the image name not provided or not in the allowed image names, then no common prefix
            String imageName = url.substring(lastSlash + 1);
            if (isAbsent(imageName) || !allowedImageNames.contains(imageName)) {
                return Optional.empty();
            }

            // If the prefix is not provided, then no common prefix
            String urlPrefix = url.substring(0, lastSlash + 1);
            if (isAbsent(urlPrefix)) {
                return Optional.empty();
            }

            // Add the prefix to a set
            imageUrlPrefixes.add(urlPrefix);
        }

        // If the set does not contain exactly one entry, then no common prefix
        return imageUrlPrefixes.size() == 1 ? Optional.of(imageUrlPrefixes.iterator().next()) : Optional.empty();
    }

    /**
     * Check if mgrpxy is installed on the server
     * @param server the server to check
     * @return true if installed, false otherwise
     */
    public static boolean isMgrpxyInstalled(Server server) {
        return PackageManager.shallowSystemPackageList(server.getId())
                .stream().anyMatch(p -> p.getName().equals(MGRPXY));
    }

    /**
     * Returns the set of channels providing the mgrpxy package that should be subscribed to.
     * Logic:
     * - If mgrpxy is already available from a subscribed channel, returns an empty set.
     * - If mgrpxy is available only from non-subscribed channels, returns those channels.
     * - If no channels provide mgrpxy at all, returns {@code null}.
     *
     * @param server the server to inspect
     * @param user   the user requesting the channels
     * @return set of subscribable channels providing mgrpxy, an empty set if already subscribed,
     * or {@code null} if unavailable
     */
    public static Set<Channel> getSubscribableMgrpxyChannels(Server server, User user) {
        Channel baseChannel = server.getBaseChannel();
        if (baseChannel == null) {
            return null;
        }

        Map<Boolean, Set<Channel>> channelsBySubscription =
                collectAccessibleChannels(baseChannel, user).stream()
                        .filter(c -> !ChannelManager.listLatestPackagesEqual(c.getId(), MGRPXY).isEmpty())
                        .collect(Collectors.partitioningBy(
                                server::isSubscribed,
                                Collectors.toSet()
                        ));

        Set<Channel> subscribed = channelsBySubscription.getOrDefault(true, Set.of());
        Set<Channel> unsubscribed = channelsBySubscription.getOrDefault(false, Set.of());

        if (!subscribed.isEmpty()) {
            return Set.of();
        }

        return unsubscribed.isEmpty() ? null : unsubscribed;
    }

    /**
     * Recursively collects accessible channels for a given user.
     * @param channel the channel to check
     * @param user the user requesting access
     * @return a set of accessible channels
     */
    private static Set<Channel> collectAccessibleChannels(Channel channel, User user) {
        Set<Channel> collected = new HashSet<>();
        collected.add(channel);
        for (Channel child : channel.getAccessibleChildrenFor(user)) {
            collected.addAll(collectAccessibleChannels(child, user));
        }
        return collected;
    }

    /**
     * Helper method to check if mgrpxy is available on server on a subscribed channel
     * @param server the server to check
     * @return true if available, false otherwise
     */
    public static boolean isMgrpxyAvailable(Server server) {
        return server.getChildChannels().stream()
                .flatMap(c -> c.getPackages().stream())
                .anyMatch(p -> MGRPXY.equals(p.getPackageName().getName()));
    }
}

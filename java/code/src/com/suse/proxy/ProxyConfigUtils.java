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

import com.redhat.rhn.domain.server.Pillar;

import com.suse.proxy.model.ProxyConfig;
import com.suse.proxy.model.ProxyConfigImage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for handling Proxy Config
 * Includes relevant constants and mappings from DTO / Pillar
 */
public class ProxyConfigUtils {

    private static final String SAFE_SUFFIX = "_safe";
    private static final String CERTIFICATE_HEADER = "-----BEGIN CERTIFICATE-----\n";


    private ProxyConfigUtils() {
        throw new UnsupportedOperationException(NOT_INSTANTIABLE);
    }

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


    // Pillar entries
    // The pillar entries for the registry URLs will follow the example format:
    // { ..., "registries": { "proxy-httpd": { "url": "https://.../proxy-httpd", "tag": "latest" }, ... } }
    // names for the registry entries are defined in ProxyContainerImagesEnum image names
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
        Map<String, Object> pillar = rootPillar.getPillar();
        ProxyConfig proxyConfig = new ProxyConfig();

        proxyConfig.setServerId((Long) pillar.get(SERVER_ID_FIELD));
        proxyConfig.setProxyFqdn(String.valueOf(pillar.get(PROXY_FQDN_FIELD)));

        proxyConfig.setParentFqdn(String.valueOf(pillar.get(PARENT_FQDN_FIELD)));
        proxyConfig.setProxyPort((Integer) pillar.get(PROXY_PORT_FIELD));
        proxyConfig.setMaxCache((Integer) pillar.get(MAX_CACHE_FIELD));
        proxyConfig.setEmail(String.valueOf(pillar.get(EMAIL_FIELD)));
        proxyConfig.setRootCA(String.valueOf(pillar.get(ROOT_CA_FIELD)));
        proxyConfig.setIntermediateCAs((List<String>) pillar.get(INTERMEDIATE_CAS_FIELD));
        proxyConfig.setProxyCert(String.valueOf(pillar.get(PROXY_CERT_FIELD)));
        proxyConfig.setProxyKey(String.valueOf(pillar.get(PROXY_KEY_FIELD)));


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
     * Maps a ProxyConfig to a safe data map
     *
     * @param proxyConfig the ProxyConfig
     * @return the safe data map
     */
    public static Map<String, Object> safeDataMapFromProxyConfig(ProxyConfig proxyConfig) {
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
        data.put(ROOT_CA_FIELD + SAFE_SUFFIX, getSafeCertInput(proxyConfig.getRootCA()));
        data.put(PROXY_CERT_FIELD + SAFE_SUFFIX, getSafeCertInput(proxyConfig.getProxyCert()));
        data.put(PROXY_KEY_FIELD + SAFE_SUFFIX, getSafeCertInput(proxyConfig.getProxyKey()));

        List<String> intermediateCAs = proxyConfig.getIntermediateCAs();
        if (isProvided(intermediateCAs)) {
            data.put(INTERMEDIATE_CAS_FIELD + SAFE_SUFFIX,
                    intermediateCAs.stream()
                            .map(ProxyConfigUtils::getSafeCertInput)
                            .toList());
        }

        ProxyConfigImage httpdImage = proxyConfig.getHttpdImage();
        if (isProvided(httpdImage)) {
            data.put(PROXY_HTTPD.getUrlField(), httpdImage.getUrl());
            data.put(PROXY_HTTPD.getTagField(), httpdImage.getTag());
        }

        ProxyConfigImage saltBrokerImage = proxyConfig.getSaltBrokerImage();
        if (isProvided(saltBrokerImage)) {
            data.put(PROXY_SALT_BROKER.getUrlField(), saltBrokerImage.getUrl());
            data.put(PROXY_SALT_BROKER.getTagField(), saltBrokerImage.getTag());
        }

        ProxyConfigImage squidImage = proxyConfig.getSquidImage();
        if (isProvided(squidImage)) {
            data.put(PROXY_SQUID.getUrlField(), squidImage.getUrl());
            data.put(PROXY_SQUID.getTagField(), squidImage.getTag());
        }

        ProxyConfigImage sshImage = proxyConfig.getSshImage();
        if (isProvided(sshImage)) {
            data.put(PROXY_SSH.getUrlField(), sshImage.getUrl());
            data.put(PROXY_SSH.getTagField(), sshImage.getTag());
        }

        ProxyConfigImage tftpdImage = proxyConfig.getTftpdImage();
        if (isProvided(tftpdImage)) {
            data.put(PROXY_TFTPD.getUrlField(), tftpdImage.getUrl());
            data.put(PROXY_TFTPD.getTagField(), tftpdImage.getTag());
        }

        if (allAbsent(httpdImage, saltBrokerImage, squidImage, sshImage, tftpdImage)) {
            data.put(SOURCE_MODE_FIELD, ProxyConfigUtils.SOURCE_MODE_RPM);
        }
        else {
            data.put(SOURCE_MODE_FIELD, ProxyConfigUtils.SOURCE_MODE_REGISTRY);
            data.put(REGISTRY_MODE, ProxyConfigUtils.REGISTRY_MODE_ADVANCED);
        }

        return data;
    }


    /**
     * Maps a ProxyConfig pillar data to a ProxyConfig Map data meant for the apply_proxy_config salt state file
     *
     * @param rootPillar the root pillar containing the proxy data to be installed
     * @return a map of the data for the apply_proxy_config salt state file
     */
    public static Map<String, Object> applyProxyConfigDataFromPillar(Pillar rootPillar) {
        Map<String, Object> pillar = rootPillar.getPillar();
        Map<String, Object> data = new HashMap<>();

        data.put(PARENT_FQDN_FIELD, pillar.get(PARENT_FQDN_FIELD));
        data.put(PROXY_PORT_FIELD, pillar.get(PROXY_PORT_FIELD));
        data.put(MAX_CACHE_FIELD, pillar.get(MAX_CACHE_FIELD));
        data.put(EMAIL_FIELD, pillar.get(EMAIL_FIELD));
        data.put(ROOT_CA_FIELD, pillar.get(ROOT_CA_FIELD));
        data.put(INTERMEDIATE_CAS_FIELD, pillar.get(INTERMEDIATE_CAS_FIELD));
        data.put(PROXY_CERT_FIELD, pillar.get(PROXY_CERT_FIELD));
        data.put(PROXY_KEY_FIELD, pillar.get(PROXY_KEY_FIELD));

        Map<String, Object> registries = (Map<String, Object>) pillar.get(PILLAR_REGISTRY_ENTRY);
        if (isProvided(registries)) {
            data.put(SOURCE_MODE_FIELD, ProxyConfigUtils.SOURCE_MODE_REGISTRY);
            data.put(REGISTRY_MODE, ProxyConfigUtils.REGISTRY_MODE_ADVANCED);
            for (ProxyContainerImagesEnum image : ProxyContainerImagesEnum.values()) {
                Map<String, String> registryEntry = (Map<String, String>) registries.get(image.getImageName());
                if (isProvided(registryEntry)) {
                    data.put(image.getPillarImageVariableName(), registryEntry.get(PILLAR_REGISTRY_URL_ENTRY));
                    data.put(image.getPillarTagVariableName(), registryEntry.get(PILLAR_REGISTRY_TAG_ENTRY));
                }
            }
        }
        else {
            data.put(SOURCE_MODE_FIELD, ProxyConfigUtils.SOURCE_MODE_RPM);
        }

        return data;
    }


    /**
     * Returns a preview of the certificate to be used as a (safe) preview
     * Truncates the input to the 10 characters.
     *
     * @param cert the input string
     * @return the safe certificate input
     */
    public static String getSafeCertInput(String cert) {
        if (isAbsent(cert)) {
            return null;
        }
        String content = cert.startsWith(CERTIFICATE_HEADER) ? cert.substring(CERTIFICATE_HEADER.length()) : cert;
        return content.length() <= 10 ? content : content.substring(0, 10) + "...";
    }
}

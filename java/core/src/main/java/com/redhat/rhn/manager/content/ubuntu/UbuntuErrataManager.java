/*
 * Copyright (c) 2021 SUSE LLC
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
package com.redhat.rhn.manager.content.ubuntu;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.util.TimeUtils;
import com.redhat.rhn.common.util.http.HttpClientAdapter;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.errata.AdvisoryStatus;
import com.redhat.rhn.domain.errata.Cve;
import com.redhat.rhn.domain.errata.CveFactory;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.ErrataFactory;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.product.Tuple3;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.frontend.dto.PackageDto;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.manager.content.ContentSyncManager;
import com.redhat.rhn.manager.content.MgrSyncUtils;
import com.redhat.rhn.manager.errata.ErrataManager;

import com.suse.manager.reactor.utils.OptionalTypeAdapterFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UbuntuErrataManager {

    private static final Type ERRATA_INFO_TYPE = new TypeToken<Map<String, UbuntuErrataInfo>>() { }.getType();

    private static final Logger LOG = LogManager.getLogger(UbuntuErrataManager.class);

    private UbuntuErrataManager() {
    }

    private static boolean isFromDir() {
        return Config.get().getString(ContentSyncManager.RESOURCE_PATH, null) != null;
    }

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
            .registerTypeAdapter(Instant.class, new TypeAdapter<Instant>() {
                @Override
                public void write(JsonWriter jsonWriter, Instant instant) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public Instant read(JsonReader jsonReader) throws IOException {
                    double d = jsonReader.nextDouble();
                    long seconds = (long)d;
                    long n = (long)((d - seconds) * 1e9);
                    return Instant.ofEpochSecond(seconds, n);
                }
            })
            .create();

    private static Optional<String> archToPackageArchLabel(String arch) {
        switch (arch) {
            case "all": return Optional.of("all-deb");
            case "source": return Optional.of("src-deb");
            case "amd64": return Optional.of("amd64-deb");
            case "arm64": return Optional.of("arm64-deb");
            case "armel": return Optional.of("armel-deb");
            case "armhf": return Optional.of("armhf-deb");
            case "sparc": return Optional.of("sparc-deb");
            case "i386": return Optional.of("i386-deb");
            case "riscv64": return Optional.of("riscv64-deb");
            case "ppc64el": return Optional.of("ppc64el-deb");
            case "s390x": return Optional.of("s390x-deb");
            case "powerpc": return Optional.of("powerpc-deb");
            default: return Optional.empty();
        }
    }

    /**
     * Special compare for epoch as NULL == "" == 0
     * @param epochInA epoch A
     * @param epochInB epoch B
     * @return returns true if the epoch values are equal
     */
    private static boolean epochEquals(String epochInA, String epochInB) {
        epochInA = Optional.ofNullable(epochInA)
                .filter(e -> !e.equals("0"))
                .orElse("");
        epochInB = Optional.ofNullable(epochInB)
                .filter(e -> !e.equals("0"))
                .orElse("");
        return epochInA.equals(epochInB);
    }

    private static Map<String, UbuntuErrataInfo> downloadUbuntuErrataInfo(String jsonDBUrl) throws IOException {
        HttpClientAdapter httpClient = new HttpClientAdapter();
        String bzipJsonDBUrl = jsonDBUrl + ".bz2";
        HttpGet httpGet = new HttpGet(bzipJsonDBUrl);
        LOG.info("download ubuntu errata start");
        HttpResponse httpResponse = httpClient.executeRequest(httpGet);
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        if (statusCode == HttpStatus.SC_OK) {
            try (
                InputStream responseStream = httpResponse.getEntity().getContent();
                BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(responseStream);
                Reader responseReader = new InputStreamReader(bzIn)
            ) {
                Map<String, UbuntuErrataInfo> errataInfo = GSON.fromJson(responseReader, ERRATA_INFO_TYPE);
                LOG.info("download ubuntu errata end");
                return errataInfo;
            }
        }
        else {
            LOG.info("Failed to get bzip2 DB - try plain DB");
            httpGet = new HttpGet(jsonDBUrl);
            httpResponse = httpClient.executeRequest(httpGet);
            statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                try (
                        InputStream responseStream = httpResponse.getEntity().getContent();
                        Reader responseReader = new InputStreamReader(responseStream)
                ) {
                    Map<String, UbuntuErrataInfo> errataInfo = GSON.fromJson(responseReader, ERRATA_INFO_TYPE);
                    LOG.info("download ubuntu errata end");
                    return errataInfo;
                }
            }
            else {
                throw new IOException("error downloading " + jsonDBUrl + " status code " + statusCode);
            }
        }
    }

    private static Stream<Entry> parseUbuntuErrata(Map<String, UbuntuErrataInfo> errataInfo, Set<String> packageNames) {
        return errataInfo.values().stream().flatMap(ubuntuErrataInfo -> {
            String description = ubuntuErrataInfo.getDescription().length() > 4000 ?
                    ubuntuErrataInfo.getDescription().substring(0, 4000) :
                    ubuntuErrataInfo.getDescription();
            boolean reboot = ubuntuErrataInfo.getAction().map(a -> a.contains("you need to reboot")).orElse(false);
            List<Tuple3<String, String, List<String>>> packageData = ubuntuErrataInfo.getReleases().entrySet().stream()
                    .flatMap(release ->
                            release.getValue().getBinaries().entrySet().stream().flatMap(binary -> {
                                String name = binary.getKey();
                                if (!packageNames.contains(name)) {
                                    return Stream.empty();
                                }
                                String version = binary.getValue().getVersion();

                                List<String> archs = release.getValue().getArchs()
                                        .stream()
                                        .flatMap(m -> m.entrySet().stream())
                                        .flatMap(a -> {
                                            String arch = a.getKey();
                                            boolean hasArchPkg = a.getValue().getUrls().entrySet().stream()
                                                    .anyMatch(b -> {
                                                        String url = b.getKey();
                                                        return url.endsWith("/" + name + "_" + version + "_" + arch +
                                                                ".deb");
                                                    });
                                                    if (hasArchPkg) {
                                                        return Stream.of(arch);
                                                    }
                                                    else {
                                                        return Stream.empty();
                                                    }
                                                }).collect(Collectors.toList());
                                return Stream.of(new Tuple3<>(name, version, archs));
                            })
                    ).collect(Collectors.toList());

            if (packageData.isEmpty()) {
                // Skip Errata when we have no matching packages
                LOG.debug("Skipping errata without matching packages: {}", ubuntuErrataInfo.getId());
                return Stream.empty();
            }
            return Stream.of(new Entry(
                    ubuntuErrataInfo.getId(),
                    ubuntuErrataInfo.getCves(),
                    ubuntuErrataInfo.getSummary(),
                    ubuntuErrataInfo.getIsummary().orElse("-"),
                    ubuntuErrataInfo.getTimestamp(),
                    description,
                    reboot,
                    packageData));
        });
    }

    private static Map<String, UbuntuErrataInfo> getUbuntuErrataInfo() throws IOException {
        String jsonDBUrl = "https://usn.ubuntu.com/usn-db/database.json";
        if (isFromDir()) {
            URI uri = MgrSyncUtils.urlToFSPath(jsonDBUrl, "");
            try (
                InputStream inputStream = Files.newInputStream(Paths.get(uri));
                Reader fileReader = new InputStreamReader(inputStream)
            ) {
                return GSON.fromJson(fileReader, ERRATA_INFO_TYPE);
            }
        }
        else {
            return downloadUbuntuErrataInfo(jsonDBUrl);
        }
    }

    /**
     * Syncs ubuntu errata information and matches it against the given channels
     * @param channelIds ids of channels to match erratas against
     * @throws IOException in case of download issues
     */
    public static void sync(Set<Long> channelIds) throws IOException {
        LOG.debug("sync started - check deb packages in channels, totalMemory:{}, freeMemory:{}",
            Runtime.getRuntime().totalMemory(), Runtime.getRuntime().freeMemory());

        // Extract the deb packages from each channel
        var packagesByChannelMap = channelIds.stream()
                                             .map(ChannelFactory::lookupById)
                                             .filter(c -> c.isTypeDeb() && !c.isCloned())
                                             .collect(Collectors.toMap(c -> c,
                                                     c -> Set.copyOf(ChannelManager.listAllPackages(c))));


        if (packagesByChannelMap.isEmpty()) {
            LOG.info("No deb packages to process in channels: {}", channelIds);
            LOG.debug("check deb packages in channels finished - done, totalMemory:{}, freeMemory:{}",
                Runtime.getRuntime().totalMemory(), Runtime.getRuntime().freeMemory());
            return;
        }
        Set<String> packageNames = packagesByChannelMap.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream())
                .map(PackageDto::getName)
                .collect(Collectors.toSet());

        LOG.debug("check deb packages in channels finished - get and parse errata");
        Stream<Entry> ubuntuErrataInfo = parseUbuntuErrata(getUbuntuErrataInfo(), packageNames);
        LOG.debug("get and parse errata finished - process Ubuntu Errata");
        processUbuntuErrata(packagesByChannelMap, ubuntuErrataInfo);
        LOG.debug("process Ubuntu Errata finished - done, totalMemory:{}, freeMemory:{}",
            Runtime.getRuntime().totalMemory(), Runtime.getRuntime().freeMemory());
    }

    /**
     * Processes ubuntu errata and tries to associate them to the given channels
     * @param packagesMap Map of deb packages by their corresponding channel
     * @param ubuntuErrataInfo list of ubuntu errata entries
     */
    public static void processUbuntuErrata(Map<Channel, Set<PackageDto>> packagesMap, Stream<Entry> ubuntuErrataInfo) {
        Set<Errata> changedErrata = new HashSet<>();
        TimeUtils.logTime(LOG, "writing erratas to db", () -> ubuntuErrataInfo.flatMap(entry -> {
            Map<Channel, Set<PackageDto>> matchingPackagesByChannel =
                    TimeUtils.logTime(LOG, "matching packages for " + entry.getId(),
                            () -> packagesMap.entrySet().stream()
                                    .collect(Collectors.toMap(Map.Entry::getKey,
                                            c -> c.getValue().stream()
                                                    .filter(p -> entry.getPackages().stream()
                                                            .anyMatch(e -> e.getA().equals(p.getName())))
                                                    .filter(p -> entry.getPackages().stream()
                                                    .anyMatch(e -> {

                                    PackageEvr packageEvr = PackageEvr.parseDebian(e.getB());
                                    return e.getC().stream()
                                            .anyMatch(arch -> p.getName().equals(e.getA()) &&
                                                    archToPackageArchLabel(arch)
                                                            .map(a -> p.getArchLabel().equals(a))
                                                            .orElse(false) &&
                                                    p.getVersion().equals(packageEvr.getVersion()) &&
                                                    p.getRelease().equals(packageEvr.getRelease()) &&
                                                    epochEquals(p.getEpoch(), packageEvr.getEpoch())
                                            );
                                    })).collect(Collectors.toSet()))));

            Map<Optional<Org>, Map<Channel, Set<PackageDto>>> collect = matchingPackagesByChannel.entrySet().stream()
                    .collect(Collectors.groupingBy(e -> Optional.ofNullable(e.getKey().getOrg()),
                            Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

            return collect.entrySet().stream().flatMap(e -> {
                Optional<Org> org = e.getKey();
                Errata errata = Optional.ofNullable(ErrataFactory.lookupByAdvisoryAndOrg(
                            entry.getId(), org.orElse(null)
                        )).orElseGet(() -> {
                            Errata newErrata = new Errata();
                            newErrata.setOrg(org.orElse(null));
                            return newErrata;
                        });

                errata.setAdvisory(entry.getId());
                errata.setAdvisoryName(entry.getId());
                errata.setAdvisoryStatus(AdvisoryStatus.STABLE);
                errata.setAdvisoryType(ErrataFactory.ERRATA_TYPE_SECURITY);
                errata.setIssueDate(Date.from(entry.getDate()));
                errata.setUpdateDate(Date.from(entry.getDate()));


                String[] split = entry.getId().split("-", 3);
                if (split.length == 3 && split[0].equals("USN")) {
                    errata.setAdvisoryRel(Long.parseLong(split[2]));
                }
                else if (split.length == 2) {
                    errata.setAdvisoryRel(Long.parseLong(split[1]));
                }
                else {
                    LOG.warn("Could not parse advisory id: {}", entry.getId());
                    return Stream.empty();
                }

                errata.setProduct("Ubuntu");
                errata.setSolution("-");
                errata.setSynopsis(entry.getIsummary());

                // faster lookup for existing entries
                Map<String, Cve> cveByName = errata.getCves().stream()
                    .collect(Collectors.toMap(Cve::getName, cve -> cve));

                Set<Cve> cves = entry.getCves().stream()
                        .filter(c -> c.startsWith("CVE-"))
                        .map(name -> cveByName.computeIfAbsent(name, CveFactory::lookupOrInsertByName))
                        .collect(Collectors.toSet());
                errata.setCves(cves);
                errata.setDescription(entry.getDescription());

                Set<Package> packages = e.getValue().entrySet().stream()
                        .flatMap(x -> x.getValue().stream())
                        .map(d -> PackageFactory.lookupByIdAndOrg(d.getId(),
                                org.orElseGet(OrgFactory::getSatelliteOrg)))
                        .collect(Collectors.toSet());
                if (errata.getPackages() == null) {
                    errata.setPackages(packages);
                    changedErrata.add(errata);
                }
                else if (errata.getPackages().addAll(packages)) {
                    changedErrata.add(errata);
                }

                Set<Channel> matchingChannels = e.getValue().entrySet().stream()
                        .filter(c -> !c.getValue().isEmpty())
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toSet());

                if (errata.getChannels().addAll(matchingChannels)) {
                    changedErrata.add(errata);
                }

                if (errata.getId() == null) {
                    changedErrata.add(errata);
                }
                return Stream.of(errata);
            });
        }).forEach(ErrataFactory::save));

        // add changed errata to notification queue
        Map<Long, List<Long>> errataToChannels = changedErrata.stream().collect(
                Collectors.toMap(
                        Errata::getId,
                        e -> e.getChannels().stream().map(Channel::getId).collect(Collectors.toList())
                        )
                );
        changedErrata.stream().flatMap(e -> e.getChannels().stream()).distinct()
            .forEach(channel -> {
                LOG.debug("Update NeededCache for Channel: {}", channel.getLabel());
                ErrataManager.insertErrataCacheTask(channel);
        });
        ErrataManager.bulkErrataNotification(errataToChannels, new Date());
    }
}

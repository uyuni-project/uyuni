/**
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
import com.redhat.rhn.domain.product.Tuple3;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
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

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

    private static final Logger LOG = Logger.getLogger(UbuntuErrataManager.class);

    private UbuntuErrataManager() {
    }

    private static boolean isFromDir() {
        return Config.get().getString(ContentSyncManager.RESOURCE_PATH, null) != null;
    }

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
            .registerTypeAdapter(Instant.class, new TypeAdapter<Instant>() {
                @Override
                public void write(JsonWriter jsonWriter, Instant instant) throws IOException {
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

    private static Map<String, UbuntuErrataInfo> downloadUbuntuErrataInfo(String jsonDBUrl) throws IOException {
        HttpClientAdapter httpClient = new HttpClientAdapter();
        HttpGet httpGet = new HttpGet(jsonDBUrl);
        LOG.info("download ubuntu errata start");
        HttpResponse httpResponse = httpClient.executeRequest(httpGet);
        if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            Map<String, UbuntuErrataInfo> errataInfo = GSON.fromJson(
                    new InputStreamReader(httpResponse.getEntity().getContent()),
                    new TypeToken<Map<String, UbuntuErrataInfo>>() { } .getType());
            LOG.info("download ubuntu errata end");
            return errataInfo;
        }
        else {
            throw new IOException("error downloading " + jsonDBUrl + " status code " +
                   httpResponse.getStatusLine().getStatusCode());
        }
    }

    private static List<Entry> parseUbuntuErrata(Map<String, UbuntuErrataInfo> errataInfo) {
        return errataInfo.values().stream().map(ubuntuErrataInfo -> {
            String description = ubuntuErrataInfo.getDescription().length() > 4000 ?
                    ubuntuErrataInfo.getDescription().substring(0, 4000) :
                    ubuntuErrataInfo.getDescription();
            boolean reboot = ubuntuErrataInfo.getAction().map(a -> a.contains("you need to reboot")).orElse(false);
            List<Tuple3<String, String, List<String>>> packageData = ubuntuErrataInfo.getReleases().entrySet().stream()
                    .flatMap(release ->
                            release.getValue().getBinaries().entrySet().stream().flatMap(binary -> {
                                String name = binary.getKey();
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

            return new Entry(
                    ubuntuErrataInfo.getId(),
                    ubuntuErrataInfo.getCves(),
                    ubuntuErrataInfo.getSummary(),
                    ubuntuErrataInfo.getIsummary().orElse("-"),
                    ubuntuErrataInfo.getTimestamp(),
                    description,
                    reboot,
                    packageData);
        }).collect(Collectors.toList());
    }

    private static Map<String, UbuntuErrataInfo> getUbuntuErrataInfo() throws IOException {
        String jsonDBUrl = "https://usn.ubuntu.com/usn-db/database.json";
        if (isFromDir()) {
            URI uri = MgrSyncUtils.urlToFSPath(jsonDBUrl, "");
            InputStream inputStream = Files.newInputStream(Paths.get(uri));
            return GSON.fromJson(new InputStreamReader(inputStream),
                    new TypeToken<Map<String, UbuntuErrataInfo>>() { }.getType());
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
        List<Entry> ubuntuErrataInfo = parseUbuntuErrata(getUbuntuErrataInfo());
        processUbuntuErrataByIds(channelIds, ubuntuErrataInfo);
    }

    /**
     * Processes ubuntu errata and tries to associate them to the given channels
     * @param channelIds list of channel ids to match errata against
     * @param ubuntuErrataInfo list of ubuntu errata entries
     */
    public static void processUbuntuErrataByIds(Set<Long> channelIds, List<Entry> ubuntuErrataInfo) {
        processUbuntuErrata(channelIds.stream()
                .map(cid -> ChannelFactory.lookupById(cid))
                .collect(Collectors.toSet()), ubuntuErrataInfo);
    }

    /**
     * Processes ubuntu errata and tries to associate them to the given channels
     * @param channels list of channels to match errata against
     * @param ubuntuErrataInfo list of ubuntu errata entries
     */
    public static void processUbuntuErrata(Set<Channel> channels, List<Entry> ubuntuErrataInfo) {

        Map<Channel, Set<Package>> ubuntuChannels = channels.stream()
                .filter(c -> c.isTypeDeb() && !c.isCloned())
                .collect(Collectors.toMap(c -> c, c -> c.getPackages()));

        List<String> uniqueCVEs = ubuntuErrataInfo.stream()
                .flatMap(e -> e.getCves().stream().filter(c -> c.startsWith("CVE-")))
                .distinct()
                .collect(Collectors.toList());

        Map<String, Cve> cveByName = TimeUtils.logTime(LOG, "looking up " +  uniqueCVEs.size() + " CVEs",
                () -> uniqueCVEs.stream()
                        .map(e -> CveFactory.lookupOrInsertByName(e))
                        .collect(Collectors.toMap(e -> e.getName(), e -> e)));

        Set<Errata> changedErrata = new HashSet<>();
        TimeUtils.logTime(LOG, "writing " + ubuntuErrataInfo.size() + " erratas to db", () -> {
            ubuntuErrataInfo.stream().flatMap(entry -> {


                Map<Channel, Set<Package>> matchingPackagesByChannel =
                        TimeUtils.logTime(LOG, "matching packages for " + entry.getId(), () -> {
                            return ubuntuChannels.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), c -> {

                                return c.getValue().stream().filter(p -> {

                                    return entry.getPackages().stream().anyMatch(e -> {

                                        PackageEvr packageEvr = PackageEvr.parseDebian(e.getB());
                                        return e.getC().stream().anyMatch(arch -> {
                                            return p.getPackageName().getName().equals(e.getA()) &&
                                                    archToPackageArchLabel(arch)
                                                            .map(a -> p.getPackageArch().getLabel().equals(a))
                                                            .orElse(false) &&
                                                    p.getPackageEvr().getVersion().equals(packageEvr.getVersion()) &&
                                                    p.getPackageEvr().getRelease().equals(packageEvr.getRelease()) &&
                                                    Optional.ofNullable(p.getPackageEvr().getEpoch())
                                                            .equals(Optional.ofNullable(packageEvr.getEpoch())) &&
                                                    p.getPackageEvr().getPackageType()
                                                            .equals(packageEvr.getPackageType());
                                        });

                                    });

                                }).collect(Collectors.toSet());
                            }));
                        });

                Map<Optional<Org>, Map<Channel, Set<Package>>> collect = matchingPackagesByChannel.entrySet().stream()
                        .collect(Collectors.groupingBy(e -> Optional.ofNullable(e.getKey().getOrg()),
                                Collectors.toMap(e -> e.getKey(), e -> e.getValue())));

                return collect.entrySet().stream().map(e -> {
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
                    String[] split = entry.getId().split("-", 2);
                    errata.setAdvisoryRel(Long.parseLong(split[1]));
                    errata.setProduct("Ubuntu");
                    errata.setSolution("-");
                    errata.setSynopsis(entry.getIsummary());
                    Set<Cve> cves = entry.getCves().stream()
                            .filter(c -> c.startsWith("CVE-"))
                            .map(cveByName::get)
                            .collect(Collectors.toSet());
                    errata.setCves(cves);
                    errata.setDescription(entry.getDescription());

                    Set<Package> packages = e.getValue().entrySet().stream()
                            .flatMap(x -> x.getValue().stream())
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
                            .map(c -> c.getKey())
                            .collect(Collectors.toSet());

                    if (errata.getChannels().addAll(matchingChannels)) {
                        changedErrata.add(errata);
                    }

                    if (errata.getId() == null) {
                        changedErrata.add(errata);
                    }
                    return errata;
                });
            }).forEach(ErrataFactory::save);
        });

        // add changed errata to notification queue
        Map<Long, List<Long>> errataToChannels = changedErrata.stream().collect(
                Collectors.toMap(
                        e -> e.getId(),
                        e -> e.getChannels().stream().map(c -> c.getId()).collect(Collectors.toList())
                        )
                );
        ErrataManager.bulkErrataNotification(errataToChannels, new Date());
    }
}

/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.manager.api.test.contract;

import com.redhat.rhn.domain.channel.ChannelArch;
import com.redhat.rhn.domain.channel.ContentSource;
import com.redhat.rhn.domain.channel.ContentSourceFilter;
import com.redhat.rhn.domain.channel.ContentSourceType;
import com.redhat.rhn.domain.channel.SslContentSource;
import com.redhat.rhn.domain.errata.AdvisoryStatus;
import com.redhat.rhn.domain.kickstart.crypto.SslCryptoKey;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.ErrataOverview;
import com.redhat.rhn.frontend.dto.PackageDto;
import com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler;

import org.jmock.Expectations;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Covers every documented channel.software operation except the three answering a channel:
 * getDetails, associateRepo and disassociateRepo. ChannelSerializer resolves the clone origin
 * through ChannelFactory#lookupOriginalChannel, which opens a Hibernate session, so those three
 * cannot be exercised without a database.
 */
public class ChannelSoftwareHandlerContractTest extends BaseOpenApiTest {

    private static final String CHANNEL_LABEL = "test-channel";
    private static final String REPO_LABEL = "test-repo";
    private static final Integer REPO_ID = 501;
    private static final Date START_DATE = new Date(0);
    private static final Date END_DATE = new Date(86400000L);
    private static final String START_DATE_JSON = "1970-01-01T00:00:00Z";
    private static final String END_DATE_JSON = "1970-01-02T00:00:00Z";

    @Override
    protected String getApiNamespace() {
        return "channel.software";
    }

    @Override
    protected Class<ChannelSoftwareHandler> getHandlerClass() {
        return ChannelSoftwareHandler.class;
    }

    private ChannelSoftwareHandler handler() {
        return (ChannelSoftwareHandler) handlerMock;
    }

    /**
     * Builds a repository serialized by the registered ContentSourceSerializer.
     *
     * @param name a unique mock name, so several repositories can coexist in one test run
     * @return the repository
     */
    private ContentSource contentSource(String name) {
        ContentSource source = context.mock(ContentSource.class, name);
        ContentSourceType type = context.mock(ContentSourceType.class, name + "Type");

        context.checking(new Expectations() {{
            allowing(type).getLabel();
            will(returnValue("yum"));
            allowing(source).getId();
            will(returnValue(REPO_ID.longValue()));
            allowing(source).getLabel();
            will(returnValue(REPO_LABEL));
            allowing(source).getSourceUrl();
            will(returnValue("https://example.com/repo"));
            allowing(source).getType();
            will(returnValue(type));
            allowing(source).getMetadataSigned();
            will(returnValue(true));
            allowing(source).getSslSets();
            will(returnValue(Set.of(sslContentSource(name + "Ssl"))));
        }});

        return source;
    }

    /**
     * Builds an SSL set serialized by the registered SslContentSourceSerializer.
     *
     * @param name a unique mock name
     * @return the SSL content source
     */
    private SslContentSource sslContentSource(String name) {
        SslContentSource ssl = context.mock(SslContentSource.class, name);
        SslCryptoKey caCert = context.mock(SslCryptoKey.class, name + "Ca");

        context.checking(new Expectations() {{
            allowing(caCert).getDescription();
            will(returnValue("test-ca"));
            allowing(ssl).getCaCert();
            will(returnValue(caCert));
            allowing(ssl).getClientCert();
            will(returnValue(null));
            allowing(ssl).getClientKey();
            will(returnValue(null));
        }});

        return ssl;
    }

    /**
     * Builds a repository filter serialized by the registered ContentSourceFilterSerializer.
     *
     * @return the repository filter
     */
    private ContentSourceFilter contentSourceFilter() {
        ContentSourceFilter filter = context.mock(ContentSourceFilter.class, "contentSourceFilter");

        context.checking(new Expectations() {{
            allowing(filter).getSortOrder();
            will(returnValue(1));
            allowing(filter).getFilter();
            will(returnValue("test-package"));
            allowing(filter).getFlag();
            will(returnValue("+"));
        }});

        return filter;
    }

    /**
     * Builds a channel architecture serialized by the registered ChannelArchSerializer.
     *
     * @return the channel architecture
     */
    private ChannelArch channelArch() {
        ChannelArch arch = context.mock(ChannelArch.class, "channelArch");

        context.checking(new Expectations() {{
            allowing(arch).getName();
            will(returnValue("x86_64"));
            allowing(arch).getLabel();
            will(returnValue("channel-x86_64"));
        }});

        return arch;
    }

    /**
     * Builds an erratum serialized by the registered ErrataOverviewSerializer.
     *
     * @param name a unique mock name
     * @return the erratum overview
     */
    private ErrataOverview errataOverview(String name) {
        ErrataOverview errata = context.mock(ErrataOverview.class, name);

        context.checking(new Expectations() {{
            allowing(errata).getId();
            will(returnValue(201L));
            allowing(errata).getIssueDateIsoFormat();
            will(returnValue("2026-01-01 00:00:00"));
            allowing(errata).getUpdateDateIsoFormat();
            will(returnValue("2026-01-02 00:00:00"));
            allowing(errata).getAdvisorySynopsis();
            will(returnValue("A test erratum"));
            allowing(errata).getAdvisoryType();
            will(returnValue("Security Advisory"));
            allowing(errata).getAdvisoryStatus();
            will(returnValue(AdvisoryStatus.FINAL));
            allowing(errata).getAdvisoryName();
            will(returnValue("SUSE-2026-0001"));
            allowing(errata).isRebootSuggested();
            will(returnValue(false));
            allowing(errata).isRestartSuggested();
            will(returnValue(false));
        }});

        return errata;
    }

    /**
     * Builds a package serialized by the registered PackageDtoSerializer. The retracted flag is
     * set, so the property the serializer only adds when it is not null is present.
     *
     * @return the package
     */
    private PackageDto packageDto() {
        PackageDto pkg = context.mock(PackageDto.class, "packageDto");

        context.checking(new Expectations() {{
            allowing(pkg).getName();
            will(returnValue("test-package"));
            allowing(pkg).getEpoch();
            will(returnValue(null));
            allowing(pkg).getVersion();
            will(returnValue("1.0.0"));
            allowing(pkg).getRelease();
            will(returnValue("1"));
            allowing(pkg).getChecksum();
            will(returnValue("0123456789abcdef"));
            allowing(pkg).getChecksumType();
            will(returnValue("sha256"));
            allowing(pkg).getId();
            will(returnValue(101L));
            allowing(pkg).getArchLabel();
            will(returnValue("x86_64"));
            allowing(pkg).getLastModified();
            will(returnValue("2026-01-02 00:00:00"));
            allowing(pkg).getRetracted();
            will(returnValue(false));
        }});

        return pkg;
    }

    /**
     * Builds a package as the registered PackageSerializer writes it, which is the shape the
     * orphan package listing and the package merge both answer with.
     *
     * @return the serialized package
     */
    private Map<String, Object> packageInfo() {
        return Map.of(
                "name", "test-package",
                "version", "1.0.0",
                "release", "1",
                "epoch", "",
                "id", 101,
                "arch_label", "x86_64",
                "last_modified", "2026-01-02T00:00:00Z",
                "path", "redhat/1/abc/test-package-1.0.0-1.x86_64.rpm",
                "part_of_retracted_patch", false,
                "provider", "SUSE");
    }

    /**
     * Presents an integer list as the long list a handler parameter declares, so an expectation can
     * name the values the router actually hands over.
     *
     * @param values the values the router parses out of the request
     * @return the same list, seen as the declared parameter type
     */
    @SuppressWarnings("unchecked")
    private List<Long> asLongList(List<Integer> values) {
        return (List<Long>) (List<?>) values;
    }

    /**
     * Presents a boolean map as the string map a handler parameter declares. The synchronisation
     * options are documented as booleans and the router keeps them as such, so the handler is
     * handed booleans however its parameter is declared.
     *
     * @param values the values the router parses out of the request
     * @return the same map, seen as the declared parameter type
     */
    @SuppressWarnings("unchecked")
    private Map<String, String> asStringMap(Map<String, Boolean> values) {
        return (Map<String, String>) (Map<?, ?>) values;
    }

    @Test
    public void testListErrataNeedingSync() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listErrataNeedingSync(with(mockUser), with(CHANNEL_LABEL));
            will(returnValue(List.of(errataOverview("needingSync"))));
        }});

        validateApiContract("/channel.software/listErrataNeedingSync", "GET")
                .withParams(Map.of("channelLabel", new String[]{CHANNEL_LABEL}))
                .onHandlerMethod("listErrataNeedingSync", User.class, String.class);
    }

    @Test
    public void testSyncErrata() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).syncErrata(with(mockUser), with(CHANNEL_LABEL));
            will(returnValue(1));
        }});

        validateApiContract("/channel.software/syncErrata", "POST")
                .withBody(Map.of("channelLabel", CHANNEL_LABEL))
                .onHandlerMethod("syncErrata", User.class, String.class);
    }

    @Test
    public void testListLatestPackages() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listLatestPackages(with(mockUser), with(CHANNEL_LABEL));
            will(returnValue(new Object[]{Map.of(
                    "name", "test-package",
                    "version", "1.0.0",
                    "release", "1",
                    "epoch", "",
                    "id", 101,
                    "arch_label", "x86_64")}));
        }});

        validateApiContract("/channel.software/listLatestPackages", "GET")
                .withParams(Map.of("channelLabel", new String[]{CHANNEL_LABEL}))
                .onHandlerMethod("listLatestPackages", User.class, String.class);
    }

    @Test
    public void testListAllPackages() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listAllPackages(with(mockUser), with(CHANNEL_LABEL));
            will(returnValue(List.of(packageDto())));
        }});

        validateApiContract("/channel.software/listAllPackages", "GET")
                .withParams(Map.of("channelLabel", new String[]{CHANNEL_LABEL}))
                .onHandlerMethod("listAllPackages", User.class, String.class);
    }

    /**
     * A start date alone selects the overload that leaves the end date open.
     */
    @Test
    public void testListAllPackagesFromStartDate() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listAllPackages(with(mockUser), with(CHANNEL_LABEL), with(START_DATE));
            will(returnValue(List.of(packageDto())));
        }});

        validateApiContract("/channel.software/listAllPackages", "GET")
                .withParams(Map.of(
                        "channelLabel", new String[]{CHANNEL_LABEL},
                        "startDate", new String[]{START_DATE_JSON}))
                .onHandlerMethod("listAllPackages", User.class, String.class, Date.class);
    }

    @Test
    public void testListAllPackagesBetweenDates() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listAllPackages(with(mockUser), with(CHANNEL_LABEL), with(START_DATE),
                    with(END_DATE));
            will(returnValue(List.of(packageDto())));
        }});

        validateApiContract("/channel.software/listAllPackages", "GET")
                .withParams(Map.of(
                        "channelLabel", new String[]{CHANNEL_LABEL},
                        "startDate", new String[]{START_DATE_JSON},
                        "endDate", new String[]{END_DATE_JSON}))
                .onHandlerMethod("listAllPackages", User.class, String.class, Date.class, Date.class);
    }

    @Test
    public void testListArches() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listArches(with(mockUser));
            will(returnValue(List.of(channelArch())));
        }});

        validateApiContract("/channel.software/listArches", "GET")
                .onHandlerMethod("listArches", User.class);
    }

    @Test
    public void testDelete() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).delete(with(mockUser), with(CHANNEL_LABEL));
            will(returnValue(1));
        }});

        validateApiContract("/channel.software/delete", "POST")
                .withBody(Map.of("channelLabel", CHANNEL_LABEL))
                .onHandlerMethod("delete", User.class, String.class);
    }

    @Test
    public void testIsGloballySubscribable() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).isGloballySubscribable(with(mockUser), with(CHANNEL_LABEL));
            will(returnValue(1));
        }});

        validateApiContract("/channel.software/isGloballySubscribable", "GET")
                .withParams(Map.of("channelLabel", new String[]{CHANNEL_LABEL}))
                .onHandlerMethod("isGloballySubscribable", User.class, String.class);
    }

    @Test
    public void testSetDetailsByLabel() throws Exception {
        var details = Map.of("name", "Renamed Channel", "summary", "A renamed channel");

        context.checking(new Expectations() {{
            oneOf(handler()).setDetails(with(mockUser), with(CHANNEL_LABEL), with(details));
            will(returnValue(1));
        }});

        validateApiContract("/channel.software/setDetails", "POST")
                .withBody(Map.of("channelLabel", CHANNEL_LABEL, "details", details))
                .onHandlerMethod("setDetails", User.class, String.class, Map.class);
    }

    /**
     * A request naming the channel by id has to reach the overload taking the id.
     */
    @Test
    public void testSetDetailsById() throws Exception {
        var details = Map.of("name", "Renamed Channel");

        context.checking(new Expectations() {{
            oneOf(handler()).setDetails(with(mockUser), with(101), with(details));
            will(returnValue(1));
        }});

        validateApiContract("/channel.software/setDetails", "POST")
                .withBody(Map.of("channelId", 101, "details", details))
                .onHandlerMethod("setDetails", User.class, Integer.class, Map.class);
    }

    @Test
    public void testCreate() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).create(with(mockUser), with(CHANNEL_LABEL), with("Test Channel"),
                    with("A test channel"), with("channel-x86_64"), with(""));
            will(returnValue(1));
        }});

        validateApiContract("/channel.software/create", "POST")
                .withBody(Map.of(
                        "label", CHANNEL_LABEL,
                        "name", "Test Channel",
                        "summary", "A test channel",
                        "archLabel", "channel-x86_64",
                        "parentLabel", ""))
                .onHandlerMethod("create", User.class, String.class, String.class, String.class,
                        String.class, String.class);
    }

    @Test
    public void testCreateWithChecksumType() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).create(with(mockUser), with(CHANNEL_LABEL), with("Test Channel"),
                    with("A test channel"), with("channel-x86_64"), with(""), with("sha256"));
            will(returnValue(1));
        }});

        validateApiContract("/channel.software/create", "POST")
                .withBody(Map.of(
                        "label", CHANNEL_LABEL,
                        "name", "Test Channel",
                        "summary", "A test channel",
                        "archLabel", "channel-x86_64",
                        "parentLabel", "",
                        "checksumType", "sha256"))
                .onHandlerMethod("create", User.class, String.class, String.class, String.class,
                        String.class, String.class, String.class);
    }

    @Test
    public void testCreateWithGpgKey() throws Exception {
        var gpgKey = Map.of("url", "https://example.com/key.gpg", "id", "ABCDEF12",
                "fingerprint", "0123 4567 89AB CDEF");

        context.checking(new Expectations() {{
            oneOf(handler()).create(with(mockUser), with(CHANNEL_LABEL), with("Test Channel"),
                    with("A test channel"), with("channel-x86_64"), with(""), with("sha256"),
                    with(gpgKey));
            will(returnValue(1));
        }});

        validateApiContract("/channel.software/create", "POST")
                .withBody(Map.of(
                        "label", CHANNEL_LABEL,
                        "name", "Test Channel",
                        "summary", "A test channel",
                        "archLabel", "channel-x86_64",
                        "parentLabel", "",
                        "checksumType", "sha256",
                        "gpgKey", gpgKey))
                .onHandlerMethod("create", User.class, String.class, String.class, String.class,
                        String.class, String.class, String.class, Map.class);
    }

    @Test
    public void testCreateWithGpgCheck() throws Exception {
        var gpgKey = Map.of("url", "https://example.com/key.gpg");

        context.checking(new Expectations() {{
            oneOf(handler()).create(with(mockUser), with(CHANNEL_LABEL), with("Test Channel"),
                    with("A test channel"), with("channel-x86_64"), with(""), with("sha256"),
                    with(gpgKey), with(true));
            will(returnValue(1));
        }});

        validateApiContract("/channel.software/create", "POST")
                .withBody(Map.of(
                        "label", CHANNEL_LABEL,
                        "name", "Test Channel",
                        "summary", "A test channel",
                        "archLabel", "channel-x86_64",
                        "parentLabel", "",
                        "checksumType", "sha256",
                        "gpgKey", gpgKey,
                        "gpgCheck", true))
                .onHandlerMethod("create", User.class, String.class, String.class, String.class,
                        String.class, String.class, String.class, Map.class, boolean.class);
    }

    @Test
    public void testSetContactDetails() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).setContactDetails(with(mockUser), with(CHANNEL_LABEL), with("Test Maintainer"),
                    with("maintainer@example.com"), with("+49 000 000000"), with("Best effort"));
            will(returnValue(1));
        }});

        validateApiContract("/channel.software/setContactDetails", "POST")
                .withBody(Map.of(
                        "channelLabel", CHANNEL_LABEL,
                        "maintainerName", "Test Maintainer",
                        "maintainerEmail", "maintainer@example.com",
                        "maintainerPhone", "+49 000 000000",
                        "supportPolicy", "Best effort"))
                .onHandlerMethod("setContactDetails", User.class, String.class, String.class, String.class,
                        String.class, String.class);
    }

    @Test
    public void testListSubscribedSystems() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listSubscribedSystems(with(mockUser), with(CHANNEL_LABEL));
            will(returnValue(new Object[]{Map.of("id", 1001, "name", "test-system")}));
        }});

        validateApiContract("/channel.software/listSubscribedSystems", "GET")
                .withParams(Map.of("channelLabel", new String[]{CHANNEL_LABEL}))
                .onHandlerMethod("listSubscribedSystems", User.class, String.class);
    }

    @Test
    public void testListSystemChannels() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listSystemChannels(with(mockUser), with(1001));
            will(returnValue(new Object[]{Map.of(
                    "id", "101", "label", CHANNEL_LABEL, "name", "Test Channel")}));
        }});

        validateApiContract("/channel.software/listSystemChannels", "GET")
                .withParams(Map.of("sid", new String[]{"1001"}))
                .onHandlerMethod("listSystemChannels", User.class, Integer.class);
    }

    @Test
    public void testSetUserSubscribable() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).setUserSubscribable(with(mockUser), with(CHANNEL_LABEL), with("testuser"),
                    with(true));
            will(returnValue(1));
        }});

        validateApiContract("/channel.software/setUserSubscribable", "POST")
                .withBody(Map.of("channelLabel", CHANNEL_LABEL, "login", "testuser", "value", true))
                .onHandlerMethod("setUserSubscribable", User.class, String.class, String.class, Boolean.class);
    }

    @Test
    public void testSetUserManageable() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).setUserManageable(with(mockUser), with(CHANNEL_LABEL), with("testuser"),
                    with(true));
            will(returnValue(1));
        }});

        validateApiContract("/channel.software/setUserManageable", "POST")
                .withBody(Map.of("channelLabel", CHANNEL_LABEL, "login", "testuser", "value", true))
                .onHandlerMethod("setUserManageable", User.class, String.class, String.class, Boolean.class);
    }

    @Test
    public void testIsUserSubscribable() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).isUserSubscribable(with(mockUser), with(CHANNEL_LABEL), with("testuser"));
            will(returnValue(1));
        }});

        validateApiContract("/channel.software/isUserSubscribable", "GET")
                .withParams(Map.of(
                        "channelLabel", new String[]{CHANNEL_LABEL},
                        "login", new String[]{"testuser"}))
                .onHandlerMethod("isUserSubscribable", User.class, String.class, String.class);
    }

    @Test
    public void testIsExisting() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).isExisting(with(mockUser), with(CHANNEL_LABEL));
            will(returnValue(true));
        }});

        validateApiContract("/channel.software/isExisting", "GET")
                .withParams(Map.of("channelLabel", new String[]{CHANNEL_LABEL}))
                .onHandlerMethod("isExisting", User.class, String.class);
    }

    @Test
    public void testIsUserManageable() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).isUserManageable(with(mockUser), with(CHANNEL_LABEL), with("testuser"));
            will(returnValue(1));
        }});

        validateApiContract("/channel.software/isUserManageable", "GET")
                .withParams(Map.of(
                        "channelLabel", new String[]{CHANNEL_LABEL},
                        "login", new String[]{"testuser"}))
                .onHandlerMethod("isUserManageable", User.class, String.class, String.class);
    }

    @Test
    public void testSetGloballySubscribable() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).setGloballySubscribable(with(mockUser), with(CHANNEL_LABEL), with(true));
            will(returnValue(1));
        }});

        validateApiContract("/channel.software/setGloballySubscribable", "POST")
                .withBody(Map.of("channelLabel", CHANNEL_LABEL, "value", true))
                .onHandlerMethod("setGloballySubscribable", User.class, String.class, boolean.class);
    }

    /**
     * The package ids are declared as longs, but the router deserializes a JSON array without the
     * declared element type, so values that fit an int reach the handler as integers.
     */
    @Test
    public void testAddPackages() throws Exception {
        var packageIds = List.of(101, 102);

        context.checking(new Expectations() {{
            oneOf(handler()).addPackages(with(mockUser), with(CHANNEL_LABEL), with(asLongList(packageIds)));
            will(returnValue(1));
        }});

        validateApiContract("/channel.software/addPackages", "POST")
                .withBody(Map.of("channelLabel", CHANNEL_LABEL, "packageIds", packageIds))
                .onHandlerMethod("addPackages", User.class, String.class, List.class);
    }

    @Test
    public void testRemoveErrata() throws Exception {
        var errataNames = List.of("SUSE-2026-0001");

        context.checking(new Expectations() {{
            oneOf(handler()).removeErrata(with(mockUser), with(CHANNEL_LABEL), with(errataNames), with(true));
            will(returnValue(1));
        }});

        validateApiContract("/channel.software/removeErrata", "POST")
                .withBody(Map.of("channelLabel", CHANNEL_LABEL, "errataNames", errataNames,
                        "removePackages", true))
                .onHandlerMethod("removeErrata", User.class, String.class, List.class, boolean.class);
    }

    @Test
    public void testRemovePackages() throws Exception {
        var packageIds = List.of(101, 102);

        context.checking(new Expectations() {{
            oneOf(handler()).removePackages(with(mockUser), with(CHANNEL_LABEL), with(asLongList(packageIds)));
            will(returnValue(1));
        }});

        validateApiContract("/channel.software/removePackages", "POST")
                .withBody(Map.of("channelLabel", CHANNEL_LABEL, "packageIds", packageIds))
                .onHandlerMethod("removePackages", User.class, String.class, List.class);
    }

    @Test
    public void testListErrata() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listErrata(with(mockUser), with(CHANNEL_LABEL));
            will(returnValue(List.of(errataOverview("listed"))));
        }});

        validateApiContract("/channel.software/listErrata", "GET")
                .withParams(Map.of("channelLabel", new String[]{CHANNEL_LABEL}))
                .onHandlerMethod("listErrata", User.class, String.class);
    }

    @Test
    public void testListErrataFromStartDate() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listErrata(with(mockUser), with(CHANNEL_LABEL), with(START_DATE));
            will(returnValue(List.of(errataOverview("listedFrom"))));
        }});

        validateApiContract("/channel.software/listErrata", "GET")
                .withParams(Map.of(
                        "channelLabel", new String[]{CHANNEL_LABEL},
                        "startDate", new String[]{START_DATE_JSON}))
                .onHandlerMethod("listErrata", User.class, String.class, Date.class);
    }

    @Test
    public void testListErrataBetweenDates() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listErrata(with(mockUser), with(CHANNEL_LABEL), with(START_DATE), with(END_DATE));
            will(returnValue(List.of(errataOverview("listedBetween"))));
        }});

        validateApiContract("/channel.software/listErrata", "GET")
                .withParams(Map.of(
                        "channelLabel", new String[]{CHANNEL_LABEL},
                        "startDate", new String[]{START_DATE_JSON},
                        "endDate", new String[]{END_DATE_JSON}))
                .onHandlerMethod("listErrata", User.class, String.class, Date.class, Date.class);
    }

    @Test
    public void testListErrataByLastModified() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listErrata(with(mockUser), with(CHANNEL_LABEL), with(START_DATE), with(END_DATE),
                    with(true));
            will(returnValue(List.of(errataOverview("listedModified"))));
        }});

        validateApiContract("/channel.software/listErrata", "GET")
                .withParams(Map.of(
                        "channelLabel", new String[]{CHANNEL_LABEL},
                        "startDate", new String[]{START_DATE_JSON},
                        "endDate", new String[]{END_DATE_JSON},
                        "lastModified", new String[]{"true"}))
                .onHandlerMethod("listErrata", User.class, String.class, Date.class, Date.class, boolean.class);
    }

    @Test
    public void testListErrataByType() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listErrataByType(with(mockUser), with(CHANNEL_LABEL), with("Security Advisory"));
            will(returnValue(new Object[]{Map.of(
                    "advisory", "SUSE-2026-0001",
                    "issue_date", "2026-01-01 00:00:00",
                    "update_date", "2026-01-02 00:00:00",
                    "synopsis", "A test erratum",
                    "advisory_type", "Security Advisory",
                    "last_modified_date", "2026-01-02 00:00:00")}));
        }});

        validateApiContract("/channel.software/listErrataByType", "GET")
                .withParams(Map.of(
                        "channelLabel", new String[]{CHANNEL_LABEL},
                        "advisoryType", new String[]{"Security Advisory"}))
                .onHandlerMethod("listErrataByType", User.class, String.class, String.class);
    }

    @Test
    public void testListPackagesWithoutChannel() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listPackagesWithoutChannel(with(mockUser));
            will(returnValue(new Object[]{packageInfo()}));
        }});

        validateApiContract("/channel.software/listPackagesWithoutChannel", "GET")
                .onHandlerMethod("listPackagesWithoutChannel", User.class);
    }

    @Test
    public void testClone() throws Exception {
        var channelDetails = Map.of("name", "Cloned Channel", "label", "cloned-channel",
                "summary", "A cloned channel");

        context.checking(new Expectations() {{
            oneOf(handler()).clone(with(mockUser), with(CHANNEL_LABEL), with(channelDetails), with(true));
            will(returnValue(1));
        }});

        validateApiContract("/channel.software/clone", "POST")
                .withBody(Map.of("originalLabel", CHANNEL_LABEL, "channelDetails", channelDetails,
                        "originalState", true))
                .onHandlerMethod("clone", User.class, String.class, Map.class, Boolean.class);
    }

    @Test
    public void testMergeErrata() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).mergeErrata(with(mockUser), with(CHANNEL_LABEL), with("other-channel"));
            will(returnValue(new Object[]{Map.of(
                    "id", 201,
                    "date", "2026-01-01 00:00:00",
                    "advisory_type", "Security Advisory",
                    "advisory_status", "final",
                    "advisory_name", "SUSE-2026-0001",
                    "advisory_synopsis", "A test erratum")}));
        }});

        validateApiContract("/channel.software/mergeErrata", "POST")
                .withBody(Map.of("mergeFromLabel", CHANNEL_LABEL, "mergeToLabel", "other-channel"))
                .onHandlerMethod("mergeErrata", User.class, String.class, String.class);
    }

    @Test
    public void testMergeErrataByName() throws Exception {
        var errataNames = List.of("SUSE-2026-0001");

        context.checking(new Expectations() {{
            oneOf(handler()).mergeErrata(with(mockUser), with(CHANNEL_LABEL), with("other-channel"),
                    with(errataNames));
            will(returnValue(new Object[]{}));
        }});

        validateApiContract("/channel.software/mergeErrata", "POST")
                .withBody(Map.of("mergeFromLabel", CHANNEL_LABEL, "mergeToLabel", "other-channel",
                        "errataNames", errataNames))
                .onHandlerMethod("mergeErrata", User.class, String.class, String.class, List.class);
    }

    /**
     * The date bounded merge documents its dates as strings, so they reach the handler unparsed.
     */
    @Test
    public void testMergeErrataByDate() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).mergeErrata(with(mockUser), with(CHANNEL_LABEL), with("other-channel"),
                    with("2026-01-01"), with("2026-01-31"));
            will(returnValue(new Object[]{}));
        }});

        validateApiContract("/channel.software/mergeErrata", "POST")
                .withBody(Map.of("mergeFromLabel", CHANNEL_LABEL, "mergeToLabel", "other-channel",
                        "startDate", "2026-01-01", "endDate", "2026-01-31"))
                .onHandlerMethod("mergeErrata", User.class, String.class, String.class, String.class,
                        String.class);
    }

    @Test
    public void testMergePackages() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).mergePackages(with(mockUser), with(CHANNEL_LABEL), with("other-channel"));
            will(returnValue(new Object[]{Map.of(
                    "name", "test-package",
                    "version", "1.0.0",
                    "release", "1",
                    "epoch", "",
                    "id", 101,
                    "arch_label", "x86_64",
                    "last_modified", "2026-01-02T00:00:00Z",
                    "path", "redhat/1/abc/test-package-1.0.0-1.x86_64.rpm",
                    "part_of_retracted_patch", false,
                    "provider", "SUSE")}));
        }});

        validateApiContract("/channel.software/mergePackages", "POST")
                .withBody(Map.of("mergeFromLabel", CHANNEL_LABEL, "mergeToLabel", "other-channel"))
                .onHandlerMethod("mergePackages", User.class, String.class, String.class);
    }

    @Test
    public void testMergePackagesAligningModules() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).mergePackages(with(mockUser), with(CHANNEL_LABEL), with("other-channel"),
                    with(true));
            will(returnValue(new Object[]{}));
        }});

        validateApiContract("/channel.software/mergePackages", "POST")
                .withBody(Map.of("mergeFromLabel", CHANNEL_LABEL, "mergeToLabel", "other-channel",
                        "alignModules", true))
                .onHandlerMethod("mergePackages", User.class, String.class, String.class, boolean.class);
    }

    @Test
    public void testAlignMetadata() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).alignMetadata(with(mockUser), with(CHANNEL_LABEL), with("other-channel"),
                    with("modules"));
            will(returnValue(1));
        }});

        validateApiContract("/channel.software/alignMetadata", "POST")
                .withBody(Map.of("channelFromLabel", CHANNEL_LABEL, "channelToLabel", "other-channel",
                        "metadataType", "modules"))
                .onHandlerMethod("alignMetadata", User.class, String.class, String.class, String.class);
    }

    @Test
    public void testRegenerateNeededCacheForChannel() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).regenerateNeededCache(with(mockUser), with(CHANNEL_LABEL));
            will(returnValue(1));
        }});

        validateApiContract("/channel.software/regenerateNeededCache", "POST")
                .withBody(Map.of("channelLabel", CHANNEL_LABEL))
                .onHandlerMethod("regenerateNeededCache", User.class, String.class);
    }

    /**
     * The channel label is optional, so a request omitting it regenerates every cache.
     */
    @Test
    public void testRegenerateNeededCacheForAllChannels() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).regenerateNeededCache(with(mockUser));
            will(returnValue(1));
        }});

        validateApiContract("/channel.software/regenerateNeededCache", "POST")
                .withBody(Map.of())
                .onHandlerMethod("regenerateNeededCache", User.class);
    }

    @Test
    public void testRegenerateYumCache() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).regenerateYumCache(with(mockUser), with(CHANNEL_LABEL), with(true));
            will(returnValue(1));
        }});

        validateApiContract("/channel.software/regenerateYumCache", "POST")
                .withBody(Map.of("channelLabel", CHANNEL_LABEL, "force", true))
                .onHandlerMethod("regenerateYumCache", User.class, String.class, Boolean.class);
    }

    @Test
    public void testListChildren() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listChildren(with(mockUser), with(CHANNEL_LABEL));
            will(returnValue(new Object[]{}));
        }});

        validateApiContract("/channel.software/listChildren", "GET")
                .withParams(Map.of("channelLabel", new String[]{CHANNEL_LABEL}))
                .onHandlerMethod("listChildren", User.class, String.class);
    }

    @Test
    public void testGetChannelLastBuildById() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).getChannelLastBuildById(with(mockUser), with(101));
            will(returnValue("2026-01-02 00:00:00"));
        }});

        validateApiContract("/channel.software/getChannelLastBuildById", "GET")
                .withParams(Map.of("id", new String[]{"101"}))
                .onHandlerMethod("getChannelLastBuildById", User.class, Integer.class);
    }

    @Test
    public void testListUserRepos() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listUserRepos(with(mockUser));
            will(returnValue(List.of(Map.<String, Object>of(
                    "id", REPO_ID,
                    "label", REPO_LABEL,
                    "sourceUrl", "https://example.com/repo"))));
        }});

        validateApiContract("/channel.software/listUserRepos", "GET")
                .onHandlerMethod("listUserRepos", User.class);
    }

    @Test
    public void testCreateRepo() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).createRepo(with(mockUser), with(REPO_LABEL), with("yum"),
                    with("https://example.com/repo"));
            will(returnValue(contentSource("createdRepo")));
        }});

        validateApiContract("/channel.software/createRepo", "POST")
                .withBody(Map.of("label", REPO_LABEL, "type", "yum", "url", "https://example.com/repo"))
                .onHandlerMethod("createRepo", User.class, String.class, String.class, String.class);
    }

    @Test
    public void testCreateRepoWithSsl() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).createRepo(with(mockUser), with(REPO_LABEL), with("yum"),
                    with("https://example.com/repo"), with("test-ca"), with("test-cert"), with("test-key"));
            will(returnValue(contentSource("createdSslRepo")));
        }});

        validateApiContract("/channel.software/createRepo", "POST")
                .withBody(Map.of(
                        "label", REPO_LABEL,
                        "type", "yum",
                        "url", "https://example.com/repo",
                        "sslCaCert", "test-ca",
                        "sslCliCert", "test-cert",
                        "sslCliKey", "test-key"))
                .onHandlerMethod("createRepo", User.class, String.class, String.class, String.class,
                        String.class, String.class, String.class);
    }

    @Test
    public void testCreateRepoWithSignedMetadata() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).createRepo(with(mockUser), with(REPO_LABEL), with("yum"),
                    with("https://example.com/repo"), with("test-ca"), with("test-cert"), with("test-key"),
                    with(true));
            will(returnValue(contentSource("createdSignedRepo")));
        }});

        validateApiContract("/channel.software/createRepo", "POST")
                .withBody(Map.of(
                        "label", REPO_LABEL,
                        "type", "yum",
                        "url", "https://example.com/repo",
                        "sslCaCert", "test-ca",
                        "sslCliCert", "test-cert",
                        "sslCliKey", "test-key",
                        "hasSignedMetadata", true))
                .onHandlerMethod("createRepo", User.class, String.class, String.class, String.class,
                        String.class, String.class, String.class, boolean.class);
    }

    @Test
    public void testRemoveRepoByLabel() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).removeRepo(with(mockUser), with(REPO_LABEL));
            will(returnValue(1));
        }});

        validateApiContract("/channel.software/removeRepo", "POST")
                .withBody(Map.of("label", REPO_LABEL))
                .onHandlerMethod("removeRepo", User.class, String.class);
    }

    @Test
    public void testRemoveRepoById() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).removeRepo(with(mockUser), with(REPO_ID));
            will(returnValue(1));
        }});

        validateApiContract("/channel.software/removeRepo", "POST")
                .withBody(Map.of("id", REPO_ID))
                .onHandlerMethod("removeRepo", User.class, Integer.class);
    }

    @Test
    public void testUpdateRepoUrlByLabel() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).updateRepoUrl(with(mockUser), with(REPO_LABEL),
                    with("https://example.com/other"));
            will(returnValue(contentSource("updatedUrlByLabel")));
        }});

        validateApiContract("/channel.software/updateRepoUrl", "POST")
                .withBody(Map.of("label", REPO_LABEL, "url", "https://example.com/other"))
                .onHandlerMethod("updateRepoUrl", User.class, String.class, String.class);
    }

    @Test
    public void testUpdateRepoUrlById() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).updateRepoUrl(with(mockUser), with(REPO_ID), with("https://example.com/other"));
            will(returnValue(contentSource("updatedUrlById")));
        }});

        validateApiContract("/channel.software/updateRepoUrl", "POST")
                .withBody(Map.of("id", REPO_ID, "url", "https://example.com/other"))
                .onHandlerMethod("updateRepoUrl", User.class, Integer.class, String.class);
    }

    @Test
    public void testUpdateRepoSslByLabel() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).updateRepoSsl(with(mockUser), with(REPO_LABEL), with("test-ca"),
                    with("test-cert"), with("test-key"));
            will(returnValue(contentSource("updatedSslByLabel")));
        }});

        validateApiContract("/channel.software/updateRepoSsl", "POST")
                .withBody(Map.of("label", REPO_LABEL, "sslCaCert", "test-ca", "sslCliCert", "test-cert",
                        "sslCliKey", "test-key"))
                .onHandlerMethod("updateRepoSsl", User.class, String.class, String.class, String.class,
                        String.class);
    }

    @Test
    public void testUpdateRepoSslById() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).updateRepoSsl(with(mockUser), with(REPO_ID), with("test-ca"),
                    with("test-cert"), with("test-key"));
            will(returnValue(contentSource("updatedSslById")));
        }});

        validateApiContract("/channel.software/updateRepoSsl", "POST")
                .withBody(Map.of("id", REPO_ID, "sslCaCert", "test-ca", "sslCliCert", "test-cert",
                        "sslCliKey", "test-key"))
                .onHandlerMethod("updateRepoSsl", User.class, Integer.class, String.class, String.class,
                        String.class);
    }

    @Test
    public void testUpdateRepoLabelByLabel() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).updateRepoLabel(with(mockUser), with(REPO_LABEL), with("renamed-repo"));
            will(returnValue(contentSource("relabelledByLabel")));
        }});

        validateApiContract("/channel.software/updateRepoLabel", "POST")
                .withBody(Map.of("label", REPO_LABEL, "newLabel", "renamed-repo"))
                .onHandlerMethod("updateRepoLabel", User.class, String.class, String.class);
    }

    @Test
    public void testUpdateRepoLabelById() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).updateRepoLabel(with(mockUser), with(REPO_ID), with("renamed-repo"));
            will(returnValue(contentSource("relabelledById")));
        }});

        validateApiContract("/channel.software/updateRepoLabel", "POST")
                .withBody(Map.of("id", REPO_ID, "label", "renamed-repo"))
                .onHandlerMethod("updateRepoLabel", User.class, Integer.class, String.class);
    }

    @Test
    public void testUpdateRepo() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).updateRepo(with(mockUser), with(REPO_ID), with("renamed-repo"),
                    with("https://example.com/other"));
            will(returnValue(contentSource("updatedRepo")));
        }});

        validateApiContract("/channel.software/updateRepo", "POST")
                .withBody(Map.of("id", REPO_ID, "label", "renamed-repo", "url", "https://example.com/other"))
                .onHandlerMethod("updateRepo", User.class, Integer.class, String.class, String.class);
    }

    @Test
    public void testGetRepoDetailsByLabel() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).getRepoDetails(with(mockUser), with(REPO_LABEL));
            will(returnValue(contentSource("repoByLabel")));
        }});

        validateApiContract("/channel.software/getRepoDetails", "GET")
                .withParams(Map.of("repoLabel", new String[]{REPO_LABEL}))
                .onHandlerMethod("getRepoDetails", User.class, String.class);
    }

    @Test
    public void testGetRepoDetailsById() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).getRepoDetails(with(mockUser), with(REPO_ID));
            will(returnValue(contentSource("repoById")));
        }});

        validateApiContract("/channel.software/getRepoDetails", "GET")
                .withParams(Map.of("id", new String[]{REPO_ID.toString()}))
                .onHandlerMethod("getRepoDetails", User.class, Integer.class);
    }

    @Test
    public void testListChannelRepos() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listChannelRepos(with(mockUser), with(CHANNEL_LABEL));
            will(returnValue(List.of(contentSource("channelRepo"))));
        }});

        validateApiContract("/channel.software/listChannelRepos", "GET")
                .withParams(Map.of("channelLabel", new String[]{CHANNEL_LABEL}))
                .onHandlerMethod("listChannelRepos", User.class, String.class);
    }

    @Test
    public void testSyncRepo() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).syncRepo(with(mockUser), with(CHANNEL_LABEL));
            will(returnValue(1));
        }});

        validateApiContract("/channel.software/syncRepo", "POST")
                .withBody(Map.of("channelLabel", CHANNEL_LABEL))
                .onHandlerMethod("syncRepo", User.class, String.class);
    }

    @Test
    public void testSyncRepoForChannelList() throws Exception {
        var channelLabels = List.of(CHANNEL_LABEL, "other-channel");

        context.checking(new Expectations() {{
            oneOf(handler()).syncRepo(with(mockUser), with(channelLabels));
            will(returnValue(1));
        }});

        validateApiContract("/channel.software/syncRepo", "POST")
                .withBody(Map.of("channelLabels", channelLabels))
                .onHandlerMethod("syncRepo", User.class, List.class);
    }

    @Test
    public void testSyncRepoWithParams() throws Exception {
        var params = Map.of("sync-kickstart", true, "no-errata", false);

        context.checking(new Expectations() {{
            oneOf(handler()).syncRepo(with(mockUser), with(CHANNEL_LABEL), with(asStringMap(params)));
            will(returnValue(1));
        }});

        validateApiContract("/channel.software/syncRepo", "POST")
                .withBody(Map.of("channelLabel", CHANNEL_LABEL, "params", params))
                .onHandlerMethod("syncRepo", User.class, String.class, Map.class);
    }

    @Test
    public void testSyncRepoOnSchedule() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).syncRepo(with(mockUser), with(CHANNEL_LABEL), with("0 0 3 ? * *"));
            will(returnValue(1));
        }});

        validateApiContract("/channel.software/syncRepo", "POST")
                .withBody(Map.of("channelLabel", CHANNEL_LABEL, "cronExpr", "0 0 3 ? * *"))
                .onHandlerMethod("syncRepo", User.class, String.class, String.class);
    }

    @Test
    public void testSyncRepoOnScheduleWithParams() throws Exception {
        var params = Map.of("latest", true);

        context.checking(new Expectations() {{
            oneOf(handler()).syncRepo(with(mockUser), with(CHANNEL_LABEL), with("0 0 3 ? * *"),
                    with(asStringMap(params)));
            will(returnValue(1));
        }});

        validateApiContract("/channel.software/syncRepo", "POST")
                .withBody(Map.of("channelLabel", CHANNEL_LABEL, "cronExpr", "0 0 3 ? * *", "params", params))
                .onHandlerMethod("syncRepo", User.class, String.class, String.class, Map.class);
    }

    @Test
    public void testGetRepoSyncCronExpression() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).getRepoSyncCronExpression(with(mockUser), with(CHANNEL_LABEL));
            will(returnValue("0 0 3 ? * *"));
        }});

        validateApiContract("/channel.software/getRepoSyncCronExpression", "GET")
                .withParams(Map.of("channelLabel", new String[]{CHANNEL_LABEL}))
                .onHandlerMethod("getRepoSyncCronExpression", User.class, String.class);
    }

    @Test
    public void testListRepoFilters() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listRepoFilters(with(mockUser), with(REPO_LABEL));
            will(returnValue(List.of(contentSourceFilter())));
        }});

        validateApiContract("/channel.software/listRepoFilters", "GET")
                .withParams(Map.of("label", new String[]{REPO_LABEL}))
                .onHandlerMethod("listRepoFilters", User.class, String.class);
    }

    @Test
    public void testAddRepoFilter() throws Exception {
        var filterProps = Map.of("filter", "test-package", "flag", "+");

        context.checking(new Expectations() {{
            oneOf(handler()).addRepoFilter(with(mockUser), with(REPO_LABEL), with(filterProps));
            will(returnValue(1));
        }});

        validateApiContract("/channel.software/addRepoFilter", "POST")
                .withBody(Map.of("label", REPO_LABEL, "filterProps", filterProps))
                .onHandlerMethod("addRepoFilter", User.class, String.class, Map.class);
    }

    @Test
    public void testRemoveRepoFilter() throws Exception {
        var filterProps = Map.of("filter", "test-package", "flag", "+");

        context.checking(new Expectations() {{
            oneOf(handler()).removeRepoFilter(with(mockUser), with(REPO_LABEL), with(filterProps));
            will(returnValue(1));
        }});

        validateApiContract("/channel.software/removeRepoFilter", "POST")
                .withBody(Map.of("label", REPO_LABEL, "filterProps", filterProps))
                .onHandlerMethod("removeRepoFilter", User.class, String.class, Map.class);
    }

    @Test
    public void testSetRepoFilters() throws Exception {
        var filterProps = List.of(Map.of("filter", "test-package", "flag", "+"));

        context.checking(new Expectations() {{
            oneOf(handler()).setRepoFilters(with(mockUser), with(REPO_LABEL), with(filterProps));
            will(returnValue(1));
        }});

        validateApiContract("/channel.software/setRepoFilters", "POST")
                .withBody(Map.of("label", REPO_LABEL, "filterProps", filterProps))
                .onHandlerMethod("setRepoFilters", User.class, String.class, List.class);
    }

    @Test
    public void testClearRepoFilters() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).clearRepoFilters(with(mockUser), with(REPO_LABEL));
            will(returnValue(1));
        }});

        validateApiContract("/channel.software/clearRepoFilters", "POST")
                .withBody(Map.of("label", REPO_LABEL))
                .onHandlerMethod("clearRepoFilters", User.class, String.class);
    }

    @Test
    public void testApplyChannelState() throws Exception {
        var sids = List.of(1001, 1002);

        context.checking(new Expectations() {{
            oneOf(handler()).applyChannelState(with(mockUser), with(sids));
            will(returnValue(401L));
        }});

        validateApiContract("/channel.software/applyChannelState", "POST")
                .withBody(Map.of("sids", sids))
                .onHandlerMethod("applyChannelState", User.class, List.class);
    }

    @Test
    public void testIsAutoSync() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).isAutoSync(with(mockUser), with(CHANNEL_LABEL));
            will(returnValue(true));
        }});

        validateApiContract("/channel.software/isAutoSync", "GET")
                .withParams(Map.of("channelLabel", new String[]{CHANNEL_LABEL}))
                .onHandlerMethod("isAutoSync", User.class, String.class);
    }

    @Test
    public void testSetAutoSync() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).setAutoSync(with(mockUser), with(CHANNEL_LABEL), with(true));
            will(returnValue(true));
        }});

        validateApiContract("/channel.software/setAutoSync", "POST")
                .withBody(Map.of("channelLabel", CHANNEL_LABEL, "autoSync", true))
                .onHandlerMethod("setAutoSync", User.class, String.class, Boolean.class);
    }
}

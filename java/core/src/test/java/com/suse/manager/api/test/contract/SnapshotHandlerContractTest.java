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

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.config.ConfigFile;
import com.redhat.rhn.domain.config.ConfigFileName;
import com.redhat.rhn.domain.config.ConfigFileType;
import com.redhat.rhn.domain.config.ConfigInfo;
import com.redhat.rhn.domain.config.ConfigRevision;
import com.redhat.rhn.domain.rhnpackage.PackageArch;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.rhnpackage.PackageName;
import com.redhat.rhn.domain.rhnpackage.PackageNevra;
import com.redhat.rhn.domain.server.ManagedServerGroup;
import com.redhat.rhn.domain.server.ServerSnapshot;
import com.redhat.rhn.domain.server.SnapshotTag;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.system.provisioning.snapshot.SnapshotHandler;

import org.jmock.Expectations;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.time.Instant;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SnapshotHandlerContractTest extends BaseOpenApiTest {

    private static final Integer SID = 1000010000;
    private static final Integer SNAP_ID = 10;
    private static final String TAG_NAME = "before-upgrade";
    private static final String START_DATE = "2026-01-01T00:00:00Z";
    private static final String END_DATE = "2026-12-31T23:59:59Z";
    private static final Date START = Date.from(Instant.parse(START_DATE));
    private static final Date END = Date.from(Instant.parse(END_DATE));
    private static final Map<String, Object> DATE_DETAILS =
            Map.of("startDate", START_DATE, "endDate", END_DATE);

    @Override
    protected String getApiNamespace() {
        return "system.provisioning.snapshot";
    }

    @Override
    protected Class<SnapshotHandler> getHandlerClass() {
        return SnapshotHandler.class;
    }

    private SnapshotHandler handler() {
        return (SnapshotHandler) handlerMock;
    }

    /**
     * Instantiates a domain class that only lets its own package construct it.
     *
     * @param type the class to instantiate
     * @param <T> the type of the instance
     * @return a new instance
     * @throws Exception if the constructor cannot be invoked
     */
    private <T> T newInstance(Class<T> type) throws Exception {
        Constructor<T> constructor = type.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    /**
     * The tags of a snapshot are read from the database, which a contract test does not reach, so
     * the fixture answers them from memory. A named class keeps the serializer lookup working,
     * which an anonymous one would not.
     */
    private static final class TestServerSnapshot extends ServerSnapshot {

        private final List<SnapshotTag> snapshotTags;

        private TestServerSnapshot(Long id, List<SnapshotTag> tagsIn) {
            setId(id);
            snapshotTags = tagsIn;
        }

        @Override
        public List<SnapshotTag> getTags() {
            return snapshotTags;
        }
    }

    private SnapshotTag tag(String name) {
        com.redhat.rhn.domain.server.SnapshotTagName tagName =
                new com.redhat.rhn.domain.server.SnapshotTagName();
        tagName.setName(name);

        SnapshotTag snapshotTag = new SnapshotTag();
        snapshotTag.setName(tagName);
        snapshotTag.setOrg(fakeOrg);
        return snapshotTag;
    }

    private ServerSnapshot snapshot() throws Exception {
        Channel channel = new Channel();
        channel.setLabel("sle-product-sles15-sp6-pool-x86_64");

        ConfigChannel configChannel = newInstance(ConfigChannel.class);
        configChannel.setLabel("test-config-channel");

        ManagedServerGroup group = new ManagedServerGroup();
        group.setName("test-group");

        TestServerSnapshot snapshot = new TestServerSnapshot(10L, List.of(tag(TAG_NAME)));
        snapshot.setOrg(fakeOrg);
        snapshot.setReason("Package install");
        snapshot.setCreated(new Date());
        snapshot.setChannels(Set.of(channel));
        snapshot.setConfigChannels(Set.of(configChannel));
        snapshot.setGroups(Set.of(group));
        return snapshot;
    }

    private PackageNevra packageNevra() {
        PackageName name = new PackageName();
        name.setName("apache2");

        PackageEvr evr = PackageEvr.parsePackageEvr(com.redhat.rhn.domain.rhnpackage.PackageType.RPM,
                "0:2.4.51-1.1");

        PackageArch arch = new PackageArch();
        arch.setLabel("x86_64");

        PackageNevra nevra = new PackageNevra();
        nevra.setName(name);
        nevra.setEvr(evr);
        nevra.setArch(arch);
        return nevra;
    }

    /**
     * A directory revision carries the properties the serializer emits for every config file type
     * plus the ownership ones; the file-only and symlink-only properties stay absent, which is
     * what the documented schema marks as optional. The revision itself is mocked because its
     * only constructor looks the default file type up in the database.
     *
     * @return a config revision describing a directory
     * @throws Exception if an instance cannot be created
     */
    private ConfigRevision configRevision() throws Exception {
        ConfigFileName fileName = new ConfigFileName();
        fileName.setPath("/etc/test");

        ConfigChannel configChannel = newInstance(ConfigChannel.class);
        configChannel.setLabel("test-config-channel");
        configChannel.setName("Test Config Channel");

        ConfigFile configFile = newInstance(ConfigFile.class);
        configFile.setConfigFileName(fileName);
        configFile.setConfigChannel(configChannel);

        ConfigInfo configInfo = newInstance(ConfigInfo.class);
        configInfo.setUsername("root");
        configInfo.setGroupname("root");
        configInfo.setFilemode(755L);
        configInfo.setSelinuxCtx("system_u:object_r:etc_t:s0");

        ConfigFileType directory = newInstance(ConfigFileType.class);
        directory.setLabel(ConfigFileType.DIR);

        ConfigRevision revision = context.mock(ConfigRevision.class);
        context.checking(new Expectations() {{
            allowing(revision).getConfigFileType();
            will(returnValue(directory));
            allowing(revision).getConfigFile();
            will(returnValue(configFile));
            allowing(revision).getConfigInfo();
            will(returnValue(configInfo));
            allowing(revision).getRevision();
            will(returnValue(1L));
            allowing(revision).getCreated();
            will(returnValue(new Date()));
            allowing(revision).getModified();
            will(returnValue(new Date()));
            allowing(revision).isSymlink();
            will(returnValue(false));
            allowing(revision).isFile();
            will(returnValue(false));
            allowing(revision).isSls();
            will(returnValue(false));
        }});
        return revision;
    }

    @Test
    public void testListSnapshots() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listSnapshots(with(mockUser), with(SID), with(START), with(END));
            will(returnValue(List.of(snapshot())));
        }});

        Map<String, String[]> params = new LinkedHashMap<>();
        params.put("sid", new String[] {SID.toString()});
        params.put("startDate", new String[] {START_DATE});
        params.put("endDate", new String[] {END_DATE});

        validateApiContract("/system.provisioning.snapshot/listSnapshots", "GET")
                .withParams(params)
                .onHandlerMethod("listSnapshots", User.class, Integer.class, Date.class, Date.class);
    }

    /*
     * listSnapshots(sid, dateDetails) has no contract test on purpose: the legacy documentation
     * declares the overload as an HTTP GET, and ApiRequestParser rejects a struct in a query
     * string with "Complex types are not allowed in query string", so the documented call cannot
     * be made over HTTP at all.
     */

    @Test
    public void testListSnapshotPackages() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listSnapshotPackages(with(mockUser), with(SNAP_ID));
            will(returnValue(Set.of(packageNevra())));
        }});

        validateApiContract("/system.provisioning.snapshot/listSnapshotPackages", "GET")
                .withParams(Map.of("snapId", new String[] {SNAP_ID.toString()}))
                .onHandlerMethod("listSnapshotPackages", User.class, Integer.class);
    }

    @Test
    public void testListSnapshotConfigFiles() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listSnapshotConfigFiles(with(mockUser), with(SNAP_ID));
            will(returnValue(Set.of(configRevision())));
        }});

        validateApiContract("/system.provisioning.snapshot/listSnapshotConfigFiles", "GET")
                .withParams(Map.of("snapId", new String[] {SNAP_ID.toString()}))
                .onHandlerMethod("listSnapshotConfigFiles", User.class, Integer.class);
    }

    @Test
    public void testDeleteSnapshotsByDate() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).deleteSnapshots(with(mockUser), with(START), with(END));
            will(returnValue(1));
        }});

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("startDate", START_DATE);
        body.put("endDate", END_DATE);

        validateApiContract("/system.provisioning.snapshot/deleteSnapshots", "POST")
                .withBody(body)
                .onHandlerMethod("deleteSnapshots", User.class, Date.class, Date.class);
    }

    @Test
    public void testDeleteSnapshotsBySystemAndDate() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).deleteSnapshots(with(mockUser), with(SID), with(START), with(END));
            will(returnValue(1));
        }});

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("sid", SID);
        body.put("startDate", START_DATE);
        body.put("endDate", END_DATE);

        validateApiContract("/system.provisioning.snapshot/deleteSnapshots", "POST")
                .withBody(body)
                .onHandlerMethod("deleteSnapshots", User.class, Integer.class, Date.class, Date.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDeleteSnapshotsByDateDetails() throws Exception {
        Map<String, Date> received = (Map<String, Date>) (Map<String, ?>) DATE_DETAILS;

        context.checking(new Expectations() {{
            oneOf(handler()).deleteSnapshots(with(mockUser), with(received));
            will(returnValue(1));
        }});

        validateApiContract("/system.provisioning.snapshot/deleteSnapshots", "POST")
                .withBody(Map.of("dateDetails", DATE_DETAILS))
                .onHandlerMethod("deleteSnapshots", User.class, Map.class);
    }

    /**
     * The router parses a struct parameter into the raw JSON values, so the dates reach the
     * handler as strings even though the parameter is declared as a map of dates.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testDeleteSnapshotsBySystemAndDateDetails() throws Exception {
        Map<String, Date> received = (Map<String, Date>) (Map<String, ?>) DATE_DETAILS;

        context.checking(new Expectations() {{
            oneOf(handler()).deleteSnapshots(with(mockUser), with(SID), with(received));
            will(returnValue(1));
        }});

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("sid", SID);
        body.put("dateDetails", DATE_DETAILS);

        validateApiContract("/system.provisioning.snapshot/deleteSnapshots", "POST")
                .withBody(body)
                .onHandlerMethod("deleteSnapshots", User.class, Integer.class, Map.class);
    }

    @Test
    public void testDeleteSnapshot() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).deleteSnapshot(with(mockUser), with(SNAP_ID));
            will(returnValue(1));
        }});

        validateApiContract("/system.provisioning.snapshot/deleteSnapshot", "POST")
                .withBody(Map.of("snapId", SNAP_ID))
                .onHandlerMethod("deleteSnapshot", User.class, Integer.class);
    }

    @Test
    public void testAddTagToSnapshot() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("snapId", SNAP_ID);
        body.put("tagName", TAG_NAME);

        context.checking(new Expectations() {{
            oneOf(handler()).addTagToSnapshot(with(mockUser), with(SNAP_ID), with(TAG_NAME));
            will(returnValue(1));
        }});

        validateApiContract("/system.provisioning.snapshot/addTagToSnapshot", "POST")
                .withBody(body)
                .onHandlerMethod("addTagToSnapshot", User.class, Integer.class, String.class);
    }

    @Test
    public void testRollbackToSnapshot() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("sid", SID);
        body.put("snapId", SNAP_ID);

        context.checking(new Expectations() {{
            oneOf(handler()).rollbackToSnapshot(with(mockUser), with(SID), with(SNAP_ID));
            will(returnValue(1));
        }});

        validateApiContract("/system.provisioning.snapshot/rollbackToSnapshot", "POST")
                .withBody(body)
                .onHandlerMethod("rollbackToSnapshot", User.class, Integer.class, Integer.class);
    }

    @Test
    public void testRollbackToTag() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("sid", SID);
        body.put("tagName", TAG_NAME);

        context.checking(new Expectations() {{
            oneOf(handler()).rollbackToTag(with(mockUser), with(SID), with(TAG_NAME));
            will(returnValue(1));
        }});

        validateApiContract("/system.provisioning.snapshot/rollbackToTag", "POST")
                .withBody(body)
                .onHandlerMethod("rollbackToTag", User.class, Integer.class, String.class);
    }

    /**
     * The system id is documented as optional, so the overload that rolls every system back to a
     * tag has to accept a request that leaves it out.
     */
    @Test
    public void testRollbackToTagWithoutSystem() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).rollbackToTag(with(mockUser), with(TAG_NAME));
            will(returnValue(1));
        }});

        validateApiContract("/system.provisioning.snapshot/rollbackToTag", "POST")
                .withBody(Map.of("tagName", TAG_NAME))
                .onHandlerMethod("rollbackToTag", User.class, String.class);
    }
}

package com.suse.manager.webui.services.test;

import com.redhat.rhn.domain.state.CustomState;
import com.redhat.rhn.domain.state.StateFactory;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;
import com.suse.manager.webui.services.SaltCustomStateStorageManager;
import com.suse.manager.webui.services.SaltStateExistsException;
import com.suse.manager.webui.services.StaleSaltStateException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static com.suse.manager.webui.utils.SaltFileUtils.defaultExtension;

/**
 * Tests for {@link SaltCustomStateStorageManager}.
 */
public class SaltCustomStateStorageManagerTest extends BaseTestCaseWithUser {

    private static final String THE_STATE_CONTENT = "#the state content";
    public static final long ORG_ID = 1;
    private SaltCustomStateStorageManager manager;
    private Path baseDir;

    /**
     * {@inheritDoc}
     */
    public void setUp() throws Exception {
        super.setUp();
        manager = new SaltCustomStateStorageManager();
        baseDir = Files.createTempDirectory("salt-" + TestUtils.randomString());
        manager.setBaseDirPath(baseDir.toString());
    }

    /**
     * {@inheritDoc}
     */
    public void tearDown() throws Exception {
        super.tearDown();
        FileUtils.deleteDirectory(baseDir.toFile());
    }

    public void testStoreCreateNew() throws IOException {
        String name = "state-" + TestUtils.randomString();
        manager.storeState(ORG_ID, name, THE_STATE_CONTENT, null, null);

        Path newFile = getStatePath(name);
        assertTrue(Files.exists(newFile));

        Optional<String> content = manager.getContent(ORG_ID, name);
        assertEquals(THE_STATE_CONTENT, content.get());

        Optional<CustomState> stateOpt = StateFactory.getCustomStateByName(ORG_ID, name);
        assertTrue(stateOpt.isPresent());
        assertEquals(name, stateOpt.get().getStateName());
        assertEquals(ORG_ID, (long)stateOpt.get().getOrg().getId());
    }

    public void testStoreSaltStateExistsExceptionCreate() throws IOException {
        try {
            String name = "state-" + TestUtils.randomString();
            manager.storeState(ORG_ID, name, THE_STATE_CONTENT, null, null);

            manager.storeState(ORG_ID, name, THE_STATE_CONTENT, null, null);

            fail("Expected exception "  + SaltStateExistsException.class.getName());

        } catch (SaltStateExistsException e) {
            assertTrue(e.getMessage().startsWith("Cannot create"));
        }
    }

    public void testStoreSaltStateExistsExceptionRename() throws IOException {
        try {
            String name = "state-" + TestUtils.randomString();
            manager.storeState(ORG_ID, name, THE_STATE_CONTENT, null, null);

            String name2 = "state-" + TestUtils.randomString();
            manager.storeState(ORG_ID, name2, THE_STATE_CONTENT, null, null);

            manager.storeState(ORG_ID, name2, THE_STATE_CONTENT, name, DigestUtils.md5Hex(THE_STATE_CONTENT));

            fail("Expected exception "  + SaltStateExistsException.class.getName());

        } catch (SaltStateExistsException e) {
            assertTrue(e.getMessage().startsWith("Cannot rename"));
        }
    }

    public void testStoreStaleSaltStateException() throws IOException {
        try {
            String name = "state-" + TestUtils.randomString();
            manager.storeState(ORG_ID, name, THE_STATE_CONTENT, null, null);

            manager.storeState(ORG_ID, name, "foo bar", name, DigestUtils.md5Hex(THE_STATE_CONTENT));
            manager.storeState(ORG_ID, name, THE_STATE_CONTENT, name, DigestUtils.md5Hex(THE_STATE_CONTENT));

            fail("Expected exception "  + StaleSaltStateException.class.getName());

        } catch (StaleSaltStateException e) {
            // got the expected exception
        }
    }

    public void testStoreUpdateNoRename() throws IOException {
        String name = "state-" + TestUtils.randomString();
        manager.storeState(ORG_ID, name, THE_STATE_CONTENT, null, null);

        manager.storeState(ORG_ID, name, "foo bar", name, DigestUtils.md5Hex(THE_STATE_CONTENT));

        Path newFile = getStatePath(name);
        assertTrue(Files.exists(newFile));

        Optional<String> content = manager.getContent(ORG_ID, name);
        assertEquals("foo bar", content.get());
    }

    public void testStoreUpdateRename() throws IOException {
        String random = TestUtils.randomString();
        String name = "state-" + random;
        manager.storeState(ORG_ID, name, THE_STATE_CONTENT, null, null);

        String newName = "stateNewName-" + random;
        manager.storeState(ORG_ID, newName, "foo bar", name, DigestUtils.md5Hex(THE_STATE_CONTENT));

        Path newFile = getStatePath(newName);
        assertTrue(Files.exists(newFile));

        Optional<String> content = manager.getContent(ORG_ID, newName);
        assertEquals("foo bar", content.get());

        Optional<CustomState> newNameOpt = StateFactory.getCustomStateByName(ORG_ID, newName);
        Optional<CustomState> oldNameOpt = StateFactory.getCustomStateByName(ORG_ID, name);
        assertFalse(oldNameOpt.isPresent());
        assertEquals(newName, newNameOpt.get().getStateName());
    }

    public void testDelete() throws IOException {
        String name = "state-" + TestUtils.randomString();
        manager.storeState(ORG_ID, name, THE_STATE_CONTENT, null, null);
        Path newFile = getStatePath(name);

        assertTrue(Files.exists(newFile));
        assertTrue(StateFactory.getCustomStateByName(ORG_ID, name).isPresent());

        manager.deleteState(ORG_ID, name);
        assertFalse(Files.exists(newFile));
        assertFalse(StateFactory.getCustomStateByName(ORG_ID, name).isPresent());
    }

    private Path getStatePath(String name) {
        return baseDir.resolve("manager_org_" + ORG_ID).resolve(defaultExtension(name));
    }

}

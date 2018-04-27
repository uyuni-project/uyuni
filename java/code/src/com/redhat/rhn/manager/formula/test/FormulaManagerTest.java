/**
 * Copyright (c) 2017 SUSE LLC
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
package com.redhat.rhn.manager.formula.test;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.domain.formula.FormulaFactory;
import com.redhat.rhn.domain.server.ManagedServerGroup;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerConstants;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.formula.FormulaManager;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerXMLRPCHelper;
import com.redhat.rhn.manager.kickstart.cobbler.test.MockXMLRPCInvoker;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ServerGroupTestUtils;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.utils.Json;
import org.cobbler.test.MockConnection;
import org.jmock.Expectations;
import org.jmock.lib.legacy.ClassImposteriser;

import java.util.Collections;
import java.util.Map;


/**
 * Test for {@link com.redhat.rhn.manager.formula.FormulaManager}.
 */
public class FormulaManagerTest extends JMockBaseTestCaseWithUser {

    static final String FORMULA_DATA = "dhcpd-formula-data.json";
    static final String FORMULA_DEFINATION = "dhcpd-formula-form.json";
    static final String TEMP_PATH = "formulas/";
    static final String formulaName = "dhcpd";
    private SaltService saltServiceMock;
    private FormulaManager manager = FormulaManager.getInstance();

    @Override
    public void setUp() throws Exception {
        super.setUp();
        setImposteriser(ClassImposteriser.INSTANCE);
        MockConnection.clear();
        saltServiceMock = mock(SaltService.class);
        manager.setSaltService(saltServiceMock);
    }


    /**
     * Validate the input data(valid) with the definition of formula
     *
     * @throws Exception - if anything goes wrong
     */

    public void testValidContents() throws Exception {

        String contentsData = TestUtils.readAll(TestUtils.findTestData(FORMULA_DATA));
        String layoutData = TestUtils.readAll(TestUtils.findTestData(FORMULA_DEFINATION));
        Map<String, Object> contents = Json.GSON.fromJson(contentsData, Map.class);

        Map<String, Object> layout = Json.GSON.fromJson(layoutData, Map.class);
        FormulaManager manager = FormulaManager.getInstance();
        manager.validateContents(contents,layout);

    }

    /**
     * Validate the input data(invalid) with the definition of formula
     * @throws Exception
     */
    public void testInValidContents() throws Exception {

        String contentsData = TestUtils.readAll(TestUtils.findTestData(FORMULA_DATA));
        String layoutData = TestUtils.readAll(TestUtils.findTestData(FORMULA_DEFINATION));
        Map<String, Object> contents = Json.GSON.fromJson(contentsData, Map.class);
        Map<String, Object> layout = Json.GSON.fromJson(layoutData, Map.class);

        contents.put("test","dummy"); // add a random field

        FormulaManager manager = FormulaManager.getInstance();
        try {
            manager.validateContents(contents,layout);
            fail( "Exception expected but didn't throw" );
        } catch (IllegalArgumentException ex) {

        }
    }

    /**
     * Test the saved group formula data
     * @throws Exception
     */
    public void testSaveGroupFormulaData() throws Exception {
        String contentsData = TestUtils.readAll(TestUtils.findTestData(FORMULA_DATA));
        Map<String, Object> contents = Json.GSON.fromJson(contentsData, Map.class);
        ManagedServerGroup managed = ServerGroupTestUtils.createManaged(user);
        FormulaFactory.setDataDir(tmpSaltRoot.resolve(TEMP_PATH).toString());

        context().checking(new Expectations(){{
            allowing(saltServiceMock).refreshPillar(with(any(MinionList.class)));
        }});
        manager.saveGroupFormulaData(user,managed.getId(), formulaName, contents);
        Map<String, Object> savedFormulaData =
                FormulaFactory.getGroupFormulaValuesByNameAndGroupId(formulaName, managed.getId())
                        .orElseGet(Collections::emptyMap);
        assertNotNull(savedFormulaData);
        assertEquals(contents,savedFormulaData);
    }

    /**
     * Test the saved server formula data
     * @throws Exception
     */
    public void testSaveServerFormulaData() throws Exception {

        String contentsData = TestUtils.readAll(TestUtils.findTestData(FORMULA_DATA));
        Map<String, Object> contents = Json.GSON.fromJson(contentsData, Map.class);
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        FormulaFactory.setDataDir(tmpSaltRoot.resolve(TEMP_PATH).toString());
        context().checking(new Expectations() {{
            allowing(saltServiceMock).refreshPillar(with(any(MinionList.class)));
        }});
        manager.saveServerFormulaData(user,minion.getId(), formulaName, contents);
        Map<String, Object> savedFormulaData =
                FormulaFactory.getFormulaValuesByNameAndServerId(formulaName, minion.getId())
                        .orElseGet(Collections::emptyMap);
        assertNotNull(savedFormulaData);
        assertEquals(contents,savedFormulaData);
        assertEquals( true, savedFormulaData.equals(contents));
    }

    /**
     * Test if unauthorized user can save formula data
     * @throws Exception
     */
    public void testSaveServerFormulaDataForUnAuthorized() throws Exception {
        String contentsData = TestUtils.readAll(TestUtils.findTestData(FORMULA_DATA));
        Map<String, Object> contents = Json.GSON.fromJson(contentsData, Map.class);
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        FormulaFactory.setDataDir(tmpSaltRoot.resolve(TEMP_PATH).toString());
        FormulaManager manager = FormulaManager.getInstance();
        User testUser = UserTestUtils.createUser("test-user", user.getOrg().getId());
        try {
            manager.saveServerFormulaData(testUser,minion.getId(), formulaName, contents);
            fail( "Exception expected but didn't throw" );
        } catch (LookupException ex) {
            //expected exception
        }
    }
}

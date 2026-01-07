/*
 * Copyright (c) 2013 SUSE LLC
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

package org.cobbler.test;

import static com.redhat.rhn.testing.RhnBaseTestCase.assertContains;

import com.redhat.rhn.testing.TestUtils;

import org.cobbler.CobblerConnection;
import org.cobbler.Image;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tests Image.
 */
public class ImageTest {

    /**
     * Image name used by setUp().
     */
    private static final String EXPECTED_NAME = "test";

    /**
     * File name used by setUp().
     */
    private static final String EXPECTED_FILE = "dummy.file";

    /**
     * Image type used by setUp().
     */
    private static final String EXPECTED_TYPE = Image.TYPE_ISO;

    /**
     * The connection.
     */
    private CobblerConnection connection;

    /**
     * The image.
     */
    private Image image;

    /**
     * Sets up a connection and image.
     *
     */
    @BeforeEach
    public void setUp() {
        MockConnection.clear();
        connection = new MockConnection("http://localhost", "token");
        image = Image.create(connection, EXPECTED_NAME, EXPECTED_TYPE, EXPECTED_FILE);
        Assertions.assertNotNull(image);
    }

    /**
     * Removes the image created by setUp().
     *
     */
    @AfterEach
    public void tearDown() {
        Assertions.assertTrue(image.remove());
        MockConnection.clear();
    }

    /**
     * Test image creation.
     */
    @Test
    public void testCreate() {
        Assertions.assertEquals(EXPECTED_NAME, image.getName());
        Assertions.assertEquals(EXPECTED_TYPE, image.getType());
        Assertions.assertEquals(EXPECTED_FILE, image.getFile());
    }

    /**
     * Test lookup by name.
     */
    @Test
    public void testLookupByName() {
        Assertions.assertEquals(image, Image.lookupByName(connection, EXPECTED_NAME));
    }

    /**
     * Test lookup by id.
     */
    @Test
    public void testLookupById() {
        Assertions.assertEquals(image, Image.lookupById(connection, image.getId()));
    }

    /**
     * Test image list.
     */
    @Test
    public void testList() {
        List<Image> result = Image.list(connection);
        Assertions.assertEquals(1, result.size());
        assertContains(result, image);
    }

    /**
     * Test setter and getter for image type.
     */
    @Test
    public void testSetGetType() {
        // Arrange
        String expected = Image.TYPE_DIRECT;

        // Act
        image.setType(expected);

        // Assert
        Assertions.assertEquals(expected, image.getType());
        assertImageKeyEquals(expected, Image.TYPE);
    }

    /**
     * Test setter and getter for image file.
     */
    @Test
    public void testSetGetFile() {
        // Arrange
        String expected = TestUtils.randomString();

        // Act
        image.setFile(expected);
        String result = image.getFile();

        // Assert
        Assertions.assertEquals(expected, result);
        assertImageKeyEquals(expected, Image.FILE);
    }

    /**
     * Check in MockConnection that the current image has a certain value
     * corresponding to a key.
     *
     * @param expected the expected value for key
     * @param key      the key
     */
    @SuppressWarnings("unchecked")
    private void assertImageKeyEquals(String expected, String key) {
        HashMap<String, Object> criteria = new HashMap<>();
        criteria.put("uid", image.getId());
        List<Map<String, Object>> result = (List<Map<String, Object>>) connection
                .invokeMethod("find_image", criteria);
        Assertions.assertEquals(expected, result.get(0).get(key));
    }
}

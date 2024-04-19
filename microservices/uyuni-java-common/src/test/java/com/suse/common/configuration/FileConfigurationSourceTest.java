/*
 * Copyright (c) 2009--2024 SUSE LLC
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

package com.suse.common.configuration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

class FileConfigurationSourceTest extends AbstractConfigurationSourceTest {

    private Path tempDirectory;

    protected ConfigurationSource createConfigurationSource() throws Exception {
        // create test config path
        tempDirectory = Files.createTempDirectory("ConfigTest-");

        List<String> paths = List.of(
            "rhn.conf",
            "default/rhn_web.conf",
            "default/rhn_prefix.conf",
            "default/bug154517.conf.rpmsave"
        );

        // copy test configuration files over
        for (String relativePath : paths) {
            InputStream inputStream = FileConfigurationSourceTest.class.getResourceAsStream(relativePath);
            if (inputStream == null) {
                throw new IllegalStateException("Unable to load resource " + relativePath);
            }

            Path targetPath = tempDirectory.resolve(Path.of(relativePath));
            Files.createDirectories(targetPath.getParent());

            Files.copy(inputStream, targetPath);
        }

        return new FileConfigurationSource(
            List.of(tempDirectory, tempDirectory.resolve("default")), "rhn",
            List.of("web", "server")
        );
    }

    @AfterEach
    public void tearDown() throws IOException {
        // Delete test files
        Files.walk(tempDirectory)
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach(File::delete);
    }

    @Nested
    @DisplayName("Namespaced retrieval")
    class NamespacedTest {
        @Test
        @DisplayName("Fully qualified in rhn_web.conf, accessed fully qualified")
        void testGetFullyQualified() {
            assertOptionalEquals("this is a property with a prefix", source.getString("web.property_with_prefix"));
            assertOptionalEquals("this is a property without a prefix", source.getString("web.without_prefix"));
        }

        @Test
        @DisplayName("Fully qualified in rhn_web.conf, accessed by name only")
        void testGetByPropertyNameOnly() {
            assertOptionalEquals("this is a property with a prefix", source.getString("property_with_prefix"));
            assertOptionalEquals("this is a property without a prefix", source.getString("without_prefix"));
        }

        @Test
        @DisplayName("Retrieve unprefixed property")
        void testUnprefixedProperty() {
            assertOptionalEquals("thirty-three", source.getString("prefix.foo"));
            assertOptionalEmpty(source.getString("foo"));
        }

        @Test
        @DisplayName("Ignore files without .conf extension")
        void testBug154517IgnoreRpmsave() {
            assertOptionalEmpty(source.getString("bug154517.conf.betternotfindme"));
            assertOptionalEmpty(source.getString("betternotfindme"));

            assertOptionalEmpty(source.getString("random.property_definition"));
            assertOptionalEmpty(source.getString("random.file.property_definition"));
            assertOptionalEmpty(source.getString("property_definition"));
        }
    }

    @Nested
    @DisplayName("Property override mechanism")
    class OvverideTest {
        @Test
        @DisplayName("Fully qualified in rhn_web.conf, overridden without prefix in rhn.conf, accessed fully qualified")
        void testOverride() {
            assertOptionalEquals("keep", source.getString("web.to_override"));
        }

        @Test
        @DisplayName("Fully qualified in rhn_web.conf, overridden without prefix in rhn.conf, accessed by name only")
        void testOverride1() {
            assertOptionalEquals("keep", source.getString("to_override"));
        }

        @Test
        @DisplayName("Fully qualified in rhn_web.conf, overridden fully qualified in rhn.conf, accessed fully qualified")
        void testOverride2() {
            assertOptionalEquals("1", source.getString("web.fq_to_override"));
        }

        @Test
        @DisplayName("Fully qualified in rhn_web.conf, overridden fully qualified in rhn.conf, accessed by name only")
        void testOverride3() {
            assertOptionalEquals("1", source.getString("fq_to_override"));
        }

        @Test
        @DisplayName("Name only in rhn_web.conf, overridden fully qualified in rhn.conf, accessed fully qualified")
        void testOverride4() {
            assertOptionalEquals("overridden", source.getString("web.to_override_without_prefix"));
            assertOptionalEquals("overridden", source.getString("to_override_without_prefix"));
        }

        @Test
        @DisplayName("Name only in rhn_web.conf, overridden name only in rhn.conf, accessed fully qualified")
        void testOverride5() {
            assertOptionalEquals("overridden", source.getString("to_override_without_prefix1"));
            assertOptionalEquals("overridden", source.getString("web.to_override_without_prefix1"));
        }
    }

    @Nested
    @DisplayName("Property collision behaviour")
    class CollisionTest {
        @Test
        @DisplayName("Multiple properties with the same name in different configuration file, accessed fully qualified")
        public void testCollision() {
            assertOptionalEquals("10", source.getString("web.collision"));
            assertOptionalEquals("12", source.getString("prefix.collision"));
        }

        @Test
        @DisplayName("Multiple properties with the same name in different configuration file, accessed without prefix")
        public void testFallbackNamespaceOrder() {
            assertOptionalEquals("10", source.getString("collision"));
        }
    }

    @Nested
    @DisplayName("Edge cases")
    class EdgeCasesTest {

        @Test
        @DisplayName("Trying to retrieve a null property")
        void testForNull() {
            assertOptionalEmpty(source.getString(null));
            assertOptionalEmpty(source.getInteger(null));
            assertOptionalEmpty(source.getBoolean(null));
            assertOptionalEmpty(source.getList(null, String.class));
        }

        @Test
        @DisplayName("A comment within property definition is considered part of the value")
        void testComment() {
            assertOptionalEquals(
                "#this will NOT be a comment!",
                source.getString("server.satellite.key_with_seeming_comment")
            );
        }

        @Test
        @DisplayName("Parsing of backslash")
        void testBackSlashes() {
            assertOptionalEquals("we\\have\\backslashes", source.getString("server.satellite.key_with_backslash"));
        }
    }
}

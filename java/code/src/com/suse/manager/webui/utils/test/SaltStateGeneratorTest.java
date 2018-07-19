/**
 * Copyright (c) 2015 SUSE LLC
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

package com.suse.manager.webui.utils.test;

import com.suse.manager.webui.utils.SaltPkgInstalled;
import com.suse.manager.webui.utils.SaltStateGenerator;
import java.io.IOException;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;
import org.yaml.snakeyaml.Yaml;

/**
 * Test for SparkTestUtils.
 */
public class SaltStateGeneratorTest extends TestCase {
    private SaltStateGenerator generator;
    private StringWriter writer;
    private Yaml yaml;

    @Override
    protected void setUp() throws Exception {
        this.writer = new StringWriter();
        this.generator = new SaltStateGenerator(this.writer);
        this.yaml = new Yaml();
    }

    /**
     * Get the body of the SLS
     *
     * @param data
     * @return
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getPayload(Map<String, Object> data) {
        Map<String, Object> sub = (Map<String, Object>) data.get("pkg_installed");
        return (List<Map<String, Object>>) sub.get("pkg.installed");
    }

    /**
     * Test basic tree before the payload of the SLS
     *
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public void testSLSTestBasicTree() throws IOException {
        SaltPkgInstalled obj = new SaltPkgInstalled();
        obj.addPackage("emacs");
        this.generator.generate(obj);

        LinkedHashMap<String, Object> data = (LinkedHashMap<String, Object>)
                this.yaml.load(this.writer.getBuffer().toString());
        LinkedHashMap<String, Object> sub = (LinkedHashMap<String, Object>)
                data.get("pkg_installed");
        assertNotNull(sub);
        assertNotNull(sub.get("pkg.installed"));
        assertEquals(((List<Map<String, Object>>) sub.get("pkg.installed"))
                .get(0).get("refresh"), true);
    }

    /**
     * Test plain package with just a name.
     *
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public void testSLSPackageInstalledNoVersion() throws IOException {
        SaltPkgInstalled obj = new SaltPkgInstalled();
        obj.addPackage("emacs");
        this.generator.generate(obj);

        List<Map<String, Object>> data = this.getPayload((Map<String, Object>)
                this.yaml.load(this.writer.getBuffer().toString()));

        List<Object> pkgs = (List<Object>) data.get(1).get("pkgs");
        assertEquals(pkgs.size(), 1);
        assertEquals(pkgs.get(0), "emacs");
    }

    /**
     * Test package with the version.
     *
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public void testSLSPackageInstalledVersion() throws IOException {
        String ver = "24.5.1";
        SaltPkgInstalled obj = new SaltPkgInstalled();
        obj.addPackageNameArchVersion("emacs", "x86_64", ver);
        this.generator.generate(obj);

        List<Map<String, Object>> data = this.getPayload((Map<String, Object>)
                this.yaml.load(this.writer.getBuffer().toString()));

        assertEquals(ver,
                (String) ((Map) ((List) data.get(1).get("pkgs")).get(0)).get("emacs.x86_64"));
    }

    /**
     * Test package with the version and the operator.
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public void testSLSPackageInstalledOpVersion() throws IOException {
        String ver = "24.5.1";
        SaltPkgInstalled obj = new SaltPkgInstalled();
        obj.addPackageNameArchVersionOp("emacs", "x86_64", ver, ">");
        this.generator.generate(obj);
        List<Map<String, Object>> data = this.getPayload((Map<String, Object>)
                this.yaml.load(this.writer.getBuffer().toString()));
        assertEquals(">" + ver,
                (String) ((Map) ((List) data.get(1).get("pkgs")).get(0)).get("emacs.x86_64"));
    }

    /**
     * Test many packages with just a name.
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public void testSLSPackagesInstalledNoVersion() throws IOException {
        SaltPkgInstalled obj = new SaltPkgInstalled();
        obj.addPackage("emacs");
        obj.addPackage("jed");
        obj.addPackage("mutt");
        this.generator.generate(obj);

        List<Map<String, Object>> data = this.getPayload((Map<String, Object>)
                this.yaml.load(this.writer.getBuffer().toString()));

        List<String> pkgs = (List<String>) data.get(1).get("pkgs");
        assertEquals(pkgs.size(), 3);
        assertTrue(pkgs.contains("emacs"));
        assertTrue(pkgs.contains("jed"));
        assertTrue(pkgs.contains("mutt"));
    }

    /**
     * Test packages with the versions.
     *
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public void testSLSPackagesInstalledVersion() throws IOException {
        SaltPkgInstalled obj = new SaltPkgInstalled();
        obj.addPackageNameArchVersion("emacs", "x86_64", "24.5.1");
        obj.addPackageNameArchVersion("jed", "x86_64", "1.2.3");
        obj.addPackageNameArchVersion("mutt", "x86_64", "5.0.5");
        this.generator.generate(obj);

        List<Map<String, Object>> data = this.getPayload((Map<String, Object>)
                this.yaml.load(this.writer.getBuffer().toString()));

        List<String> pkgs = (List<String>) data.get(1).get("pkgs");
        assertEquals(pkgs.size(), 3);
        assertTrue(pkgs.contains(new LinkedHashMap<String, Object>(){
            {
                put("emacs.x86_64", "24.5.1");
            }
        }));
        assertTrue(pkgs.contains(new LinkedHashMap<String, Object>(){
            {
                put("jed.x86_64", "1.2.3");
            }
        }));
        assertTrue(pkgs.contains(new LinkedHashMap<String, Object>(){
            {
                put("mutt.x86_64", "5.0.5");
            }
        }));
    }

    /**
     * Test package with the version and the operator.
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public void testSLSPackagesInstalledOpVersion() throws IOException {
        SaltPkgInstalled obj = new SaltPkgInstalled();
        obj.addPackageNameArchVersionOp("emacs", "x86_64", "24.5.1", ">");
        obj.addPackageNameArchVersionOp("jed", "x86_64", "1.2.3", ">");
        obj.addPackageNameArchVersionOp("mutt", "x86_64", "5.0.5", ">");
        this.generator.generate(obj);

        List<Map<String, Object>> data = this.getPayload((Map<String, Object>)
                this.yaml.load(this.writer.getBuffer().toString()));

        List<String> pkgs = (List<String>) data.get(1).get("pkgs");
        assertEquals(pkgs.size(), 3);
        assertTrue(pkgs.contains(new LinkedHashMap<String, Object>(){
            {
                put("emacs.x86_64", ">24.5.1");
            }
        }));
        assertTrue(pkgs.contains(new LinkedHashMap<String, Object>(){
            {
                put("jed.x86_64", ">1.2.3");
            }
        }));
        assertTrue(pkgs.contains(new LinkedHashMap<String, Object>(){
            {
                put("mutt.x86_64", ">5.0.5");
            }
        }));
    }
}
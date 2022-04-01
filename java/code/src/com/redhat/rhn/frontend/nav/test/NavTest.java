/*
 * Copyright (c) 2009--2017 Red Hat, Inc.
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

package com.redhat.rhn.frontend.nav.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.frontend.nav.DepthGuard;
import com.redhat.rhn.frontend.nav.NavCache;
import com.redhat.rhn.frontend.nav.NavDigester;
import com.redhat.rhn.frontend.nav.NavNode;
import com.redhat.rhn.frontend.nav.NavTree;
import com.redhat.rhn.frontend.nav.NavTreeIndex;
import com.redhat.rhn.frontend.nav.RenderEngine;
import com.redhat.rhn.frontend.nav.Renderable;
import com.redhat.rhn.frontend.nav.TextRenderer;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.TestUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NavTest extends RhnBaseTestCase {
    private static Logger log = LogManager.getLogger(NavTest.class);

    /**
     * {@inheritDoc}
     */
    @BeforeEach
    public void setUp() throws Exception {
        TestUtils.disableLocalizationLogging();
    }

    @Test
    public void testCache() throws Exception {
        NavTree realTree = NavDigester.buildTree(TestUtils.findTestData("sitenav.xml"));
        NavTree cacheTree1 = NavCache.getTree(TestUtils.findTestData("sitenav.xml"));
        NavTree cacheTree2 = NavCache.getTree(TestUtils.findTestData("sitenav.xml"));

        assertNotSame(realTree, cacheTree1);
        assertSame(cacheTree1, cacheTree2);
    }

    @Test
    public void testDigester() throws Exception {
        StopWatch st = new StopWatch();
        st.start();
        NavTree nt =
            NavDigester.buildTree(TestUtils.findTestData("sitenav.xml"));
        assertTrue(nt.getTitleDepth() == 0);
        assertTrue(nt.getLabel().equals("sitenav_unauth"));
        assertNotNull(nt.getAclMixins());

        NavTreeIndex nti = new NavTreeIndex(nt);

        String testPath = "/rhn/help/index.do";
        nti.computeActiveNodes(testPath, null);

        NavNode bestNode = nti.getBestNode();
        assertEquals(bestNode.getName(), "Help Desk");
        assertEquals(bestNode.getPrimaryURL(), testPath);

        log.info("Index Duration: " +
                       st.getTime() / 1000f + " seconds");

        RenderEngine nr = new RenderEngine(nti);
        st.stop();

        Renderable renderer = new TextRenderer();
        renderer.setRenderGuard(new DepthGuard(1, Integer.MAX_VALUE));

        log.info("Using Renderable " +
            renderer.getClass() + ":\n" + nr.render(renderer));

        log.info("Parse Duration: " +
                       st.getTime() / 1000f + " seconds");
    }

    @Test
    public void testUrlSplit() throws Exception {
        String[] testUrls = new String[] {
            "/",
            "/foo",
            "/foo/",
            "/foo/bar",
            "/foo/bar/",
            "/foo/bar/baz/"
        };

        String[] expected = new String[] {
            "/",
            "/foo:/",
            "/foo:/",
            "/foo/bar:/foo:/",
            "/foo/bar:/foo:/",
            "/foo/bar/baz:/foo/bar:/foo:/"
        };

        for (int i = 0; i < testUrls.length; i++) {
            String[] prefixes = NavTreeIndex.splitUrlPrefixes(testUrls[i]);
            String result = StringUtils.join(prefixes, ":");

            assertEquals(result, expected[i]);
        }
    }

    @Test
    public void testLastMappedPath() throws Exception {

        NavTree nt =
            NavDigester.buildTree(TestUtils.findTestData("sitenav.xml"));

        NavTreeIndex nti = new NavTreeIndex(nt);

        String testPath = "/SOMEUNKNOWNURLTHATHASNOMAPPING.html";
        String lastPath = "/rhn/systems/SystemEntitlements.do";
        // Here we want to make sure our "Best Node" is what is used in the last
        // path.
        String activePath = nti.computeActiveNodes(testPath, lastPath);

        NavNode bestNode = nti.getBestNode();
        assertEquals(bestNode.getName(), "Subscription Management");
        assertEquals(bestNode.getPrimaryURL(), lastPath);
        assertEquals(bestNode.getPrimaryURL(), activePath);
    }

    @Test
    public void testMatchByUrl() throws Exception {
        NavTree nt =
            NavDigester.buildTree(TestUtils.findTestData("sitenav.xml"));

        NavTreeIndex nti = new NavTreeIndex(nt);
        String lastPath = "/rhn/help/displatcher/release_notes";
        String curPath = "/rhn/apidoc/handlers/PackagesProviderHandler.jsp";
        nti.computeActiveNodes(curPath, lastPath);
        NavNode bestNode = nti.getBestNode();
        assertEquals("/rhn/apidoc/index.jsp", bestNode.getPrimaryURL());
    }

    @Test
    public void testMatchByDir() throws Exception {
        NavTree nt =
            NavDigester.buildTree(TestUtils.findTestData("sitenav.xml"));

        NavTreeIndex nti = new NavTreeIndex(nt);
        nti.computeActiveNodes("/rhn/help/getting-started", "");
        NavNode bestNode = nti.getBestNode();
        assertEquals("/rhn/help/dispatcher/getting_started_guide", bestNode.getPrimaryURL());
    }
}



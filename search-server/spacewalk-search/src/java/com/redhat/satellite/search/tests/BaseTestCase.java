/*
 * Copyright (c) 2010--2015 Red Hat, Inc.
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
package com.redhat.satellite.search.tests;

import com.redhat.satellite.search.config.Configuration;

import org.picocontainer.defaults.DefaultPicoContainer;

import junit.framework.TestCase;

public abstract class BaseTestCase extends TestCase {

    protected DefaultPicoContainer container;

    @Override
    protected void setUp() throws Exception {
        System.getProperties().put("isTesting", "true");
        super.setUp();
        container = TestUtil.buildContainer(getComponentClasses());
        container.start();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        Configuration config = (Configuration)
            container.getComponentInstanceOfType(Configuration.class);
        TestUtil.cleanupDirectories(config);
        container.stop();
    }

    @SuppressWarnings("unchecked")
    protected abstract Class[] getComponentClasses();

}

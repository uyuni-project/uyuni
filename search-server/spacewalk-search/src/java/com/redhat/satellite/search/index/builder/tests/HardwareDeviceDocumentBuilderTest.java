/*
 * Copyright (c) 2008--2010 Red Hat, Inc.
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
package com.redhat.satellite.search.index.builder.tests;

import com.redhat.satellite.search.index.builder.BuilderFactory;
import com.redhat.satellite.search.index.builder.DocumentBuilder;
import com.redhat.satellite.search.index.builder.HardwareDeviceDocumentBuilder;

import org.apache.lucene.document.Document;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;



/**
 * HardwareDeviceDocumentBuilderTest
 * @version $Rev$
 */
public class HardwareDeviceDocumentBuilderTest extends TestCase {

    public void testBuilderDocument() {
        Map<String, String> metadata = new HashMap<String, String>();
        metadata.put("name", "Name");
        metadata.put("description", "Description");

        DocumentBuilder db = BuilderFactory.getBuilder(BuilderFactory.HARDWARE_DEVICE_TYPE);
        assertTrue(db instanceof HardwareDeviceDocumentBuilder);
        Document doc = db.buildDocument(10L, metadata);

        assertNotNull(doc);
        assertEquals(doc.getField("id").stringValue(), Long.toString(10));
        assertEquals(doc.getField("name").stringValue(), "Name");
        assertEquals(doc.getField("description").stringValue(), "Description");
    }
}

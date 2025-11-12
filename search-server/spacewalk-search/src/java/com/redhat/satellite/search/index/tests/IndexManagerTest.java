/*
 * Copyright (c) 2010--2015 Red Hat, Inc.
 * Copyright (c) 2022 SUSE LLC
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
package com.redhat.satellite.search.index.tests;

import com.redhat.satellite.search.index.IndexManager;
import com.redhat.satellite.search.index.IndexingException;
import com.redhat.satellite.search.index.QueryParseException;
import com.redhat.satellite.search.index.Result;
import com.redhat.satellite.search.index.builder.DocumentBuilder;
import com.redhat.satellite.search.index.builder.PackageDocumentBuilder;
import com.redhat.satellite.search.tests.BaseTestCase;
import com.redhat.satellite.search.tests.TestUtil;

import org.apache.lucene.document.Document;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IndexManagerTest extends BaseTestCase {

    private IndexManager indexManager;

    public void setUp() throws Exception {
        super.setUp();
        indexManager = (IndexManager)
            container.getComponentInstance(IndexManager.class);
    }

    public void testIndexing() throws IndexingException {
        String index = "foo";
        Long objectId = new Long(123);
        Map<String, String> meta = new HashMap<String, String>();
        meta.put("name", "foo");
        meta.put("desc", "A really nice foo");
        meta.put("size", "12345");
        meta.put("dateCreated", "7/13/2007");
        DocumentBuilder pdb = new PackageDocumentBuilder();
        Document doc = pdb.buildDocument(objectId, meta);
        indexManager.addToIndex(index, doc, "en");
    }

    public void testQuerying()
        throws IndexingException, QueryParseException {

        String index = "foo";
        Long objectId = new Long(123);
        Map<String, String> meta = new HashMap<String, String>();
        meta.put("name", "foo");
        meta.put("desc", "A really nice foo");
        meta.put("size", "12345");
        meta.put("dateCreated", "7/13/2007");
        DocumentBuilder pdb = new PackageDocumentBuilder();
        Document doc = pdb.buildDocument(objectId, meta);
        indexManager.addToIndex(index, doc, "en");
        List<Result> results = indexManager.search(index, "name:foo", "en");
        assertTrue(results.size() >= 1);
        results = indexManager.search(index, "desc:really", "en");
        assertTrue(results.size() >= 1);
    }


    @SuppressWarnings("unchecked")
    @Override
    protected Class[] getComponentClasses() {
        return TestUtil.buildComponentsList(IndexManager.class);
    }


}

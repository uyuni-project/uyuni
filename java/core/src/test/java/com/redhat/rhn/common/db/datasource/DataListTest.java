/*
 * Copyright (c) 2009--2013 Red Hat, Inc.
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
package com.redhat.rhn.common.db.datasource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.testing.RhnBaseTestCase;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DataListTest extends RhnBaseTestCase {
    private HookedSelectMode hsm;
    private Map<String, Object> elabParams;

    @Override
    @BeforeEach
    public void setUp() {
        String dbUser = Config.get().getString(ConfigDefaults.DB_USER);

        hsm = new HookedSelectMode(
                ModeFactory.getMode("test_queries", "user_tables_pg"));
        elabParams = new HashMap<>();
        elabParams.put("user_name", dbUser);
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
        hsm = null;
        elabParams = null;
    }

    @Test
    public void testElaborate() {
        DataList<Map<String, String>> list = getList();
        assertNotNull(list.iterator());
        assertTrue(hsm.isElaborated());
    }

    @Test
    public void testSubList() {
        //work it like a list
        DataList<Map<String, String>> list = getList();
        DataList<Map<String, String>> sub = getSubList(list);

        //subList does not force elaboration
        assertFalse(hsm.isElaborated());
        //No elaboration until data is actually accessed.
        assertNotNull(sub.toString());
        assertFalse(hsm.isElaborated());
        assertFalse(sub.isEmpty());
        assertFalse(hsm.isElaborated());
        assertNotNull(sub.get(1));
        assertTrue(hsm.isElaborated());
    }

    @Test
    public void testElaborateOnce() {
        //at first, nothing is elaborated
        DataList<Map<String, String>> list = getList();
        assertEquals(0, hsm.getElaborated());

        //iterator causes elaboration
        assertNotNull(list.iterator());
        assertEquals(1, hsm.getElaborated());
        //don't elaborate again
        assertNotNull(list.get(1));
        assertEquals(1, hsm.getElaborated());

        DataList<Map<String, String>> sub = getSubList(list);
        assertEquals(1, hsm.getElaborated());
        //sublist should also know that it is already elaborated
        assertNotNull(sub.iterator());
        assertEquals(1, hsm.getElaborated());
        assertEquals(sub.getMode(), hsm);
    }

    private DataList<Map<String, String>> getList() {
        //test the get method
        DataList<Map<String, String>> list = DataList.getDataList(hsm, new HashMap<>(), elabParams);
        assertFalse(list.isEmpty());
        assertFalse(hsm.isElaborated());
        return list;
    }

    private DataList<Map<String, String>> getSubList(DataList<Map<String, String>> list) {
        int end = list.size() < 11 ? list.size() - 1 : 10;
        List<Map<String, String>> sub = list.subList(0, end);
        assertEquals(sub.size(), end);
        assertEquals(sub.getClass(), DataList.class);
        return (DataList<Map<String, String>>) sub;
    }

    /**
     * Lets us divorce the infrastructure testing from requiring an actual DB
     * Previous incarnation only worked if connected to Oracle
     * @author ggainey
     *
     */
    public class HookedSelectMode extends SelectMode {

        private static final long serialVersionUID = 1L;
        private int elaborated;
        private DataResult<Map<String, String>> baseDr;
        private SelectMode selectMode;

        public HookedSelectMode(SelectMode m) {
            selectMode = m;
            elaborated = 0;
            super.setName(m.getName());
        }

        public boolean isElaborated() {
            return (elaborated > 0);
        }

        public int getElaborated() {
            return elaborated;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> DataResult<T> execute(Map<String, ?> parameters) {
            if (baseDr == null) {
                baseDr = buildBase();
            }
            return (DataResult<T>) baseDr;
        }

        @Override
        public void elaborate(List resultList, Map<String, ?> parms) {
            if (elaborated <= 0) {
                buildElab();
            }
            elaborated++;
        }

        private DataResult<Map<String, String>> buildBase() {
            ArrayList<Map<String, String>> rslts = new ArrayList<>();
            String[] names = {
                    "RHN", "RHNDEBUG", "WEB"
            };

            int uid = 101;
            for (String name : names) {
                Map<String, String> rsltRow = new HashMap<>();
                rsltRow.put("username", name);
                rsltRow.put("user_id", "" + uid++);
                rsltRow.put("created", "01-APR-2013 00:00");
                rslts.add(rsltRow);
            }
            return new DataResult<>(rslts);
        }

        private void buildElab() {
            ArrayList<Map<String, String>> typedDr = baseDr;
            for (Map<String, String> oneRow : typedDr) {
                oneRow.put("table_count", "13");
            }
        }

        @Override
        public String getClassString() {
            return selectMode.getClassString();
        }

        @Override
        public void addElaborator(CachedStatement q) {
            selectMode.addElaborator(q);
        }

        @Override
        public List<CachedStatement> getElaborators() {
            return selectMode.getElaborators();
        }

        @Override
        public <T> DataResult<T> execute(List<?> inClause) {
            return selectMode.execute(inClause);
        }

        @Override
        public <T> DataResult<T> execute() {
            return selectMode.execute();
        }

        @Override
        public <T> DataResult<T> execute(Map<String, ?> parameters, List<?> inClause) {
            return selectMode.execute(parameters, inClause);
        }

        @Override
        public void setMaxRows(int max) {
            selectMode.setMaxRows(max);
        }

        @Override
        public int getMaxRows() {
            return selectMode.getMaxRows();
        }

        @Override
        public void setName(String n) {
            selectMode.setName(n);
        }

        @Override
        public String getName() {
            return selectMode.getName();
        }

        @Override
        public void setQuery(CachedStatement q) {
            selectMode.setQuery(q);
        }

        @Override
        public CachedStatement getQuery() {
            return selectMode.getQuery();
        }

        @Override
        public int getArity() {
            return selectMode.getArity();
        }
    }

}

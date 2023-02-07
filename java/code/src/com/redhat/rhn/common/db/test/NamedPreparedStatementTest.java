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
package com.redhat.rhn.common.db.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.common.db.BindVariableNotFoundException;
import com.redhat.rhn.common.db.NamedPreparedStatement;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.testing.RhnBaseTestCase;

import org.hibernate.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NamedPreparedStatementTest extends RhnBaseTestCase {

    private Session session;

    private static final int LIST_SIZE = 2;
    private static final int FIRST_POS = 2;
    private static final int SECOND_POS = 3;


    private String SIMPLE_QUERY = "SELECT wc.id AS ID, " +
                                  "wc.login, " +
                                  "wc.login_uc " +
                                  " FROM web_contact wc " +
                                  " WHERE wc.org_id = :org_id " +
                                  " ORDER BY wc.login_uc, wc.id";

    private String SIMPLE_QUERY_SUBST = "SELECT wc.id AS ID, " +
                                        "wc.login, " +
                                        "wc.login_uc " +
                                        " FROM web_contact wc " +
                                        " WHERE wc.org_id = ? " +
                                        " ORDER BY wc.login_uc, wc.id";

    private String TWO_VAR_QUERY = "SELECT DISTINCT E.id, E.update_date " +
                                   "FROM rhnErrata E, " +
                                   "rhnServerNeededCache SNC " +
                                   "WHERE EXISTS (SELECT server_id FROM " +
                                   "rhnUserServerPerms USP WHERE " +
                                   "USP.user_id = :user_id AND " +
                                   "USP.server_id = :sid) " +
                                   "AND SNC.server_id = :sid " +
                                   "AND SNC.errata_id = E.id " +
                                   "ORDER BY E.update_date, E.id";

    private String TWO_VAR_QUERY_SUBST = "SELECT DISTINCT E.id, " +
                                         "E.update_date " +
                                         "FROM rhnErrata E, " +
                                         "rhnServerNeededCache SNC " +
                                         "WHERE EXISTS (SELECT server_id " +
                                         "FROM rhnUserServerPerms USP " +
                                         "WHERE USP.user_id = ? AND " +
                                         "USP.server_id = ?) " +
                                         "AND SNC.server_id = ? " +
                                         "AND SNC.errata_id = E.id " +
                                         "ORDER BY E.update_date, E.id";

    private String COLON_IN_QUOTES = "SELECT 'FOO:BAR:MI:SS' " +
                                     "FROM FOOBAR";


    @Override
    @BeforeEach
    public void setUp() {
        session = HibernateFactory.getSession();
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception {
        session = null;
        super.tearDown();
    }

    @Test
    public void testColonInQuotes() {
        String jdbcQuery;
        Map<String, List<Integer>> pMap = new HashMap<>();

        jdbcQuery = NamedPreparedStatement.replaceBindParams(COLON_IN_QUOTES, pMap);

        assertEquals(COLON_IN_QUOTES, jdbcQuery);
        assertTrue(pMap.isEmpty());
    }

    @Test
    public void testCreateSQL() {
        String jdbcQuery;
        Map<String, List<Integer>> pMap = new HashMap<>();

        jdbcQuery = NamedPreparedStatement.replaceBindParams(SIMPLE_QUERY, pMap);
        assertEquals(SIMPLE_QUERY_SUBST, jdbcQuery);

        List<Integer> lst = pMap.get("org_id");
        assertNotNull(lst);
        assertEquals(1, lst.size());
        assertEquals(1, lst.get(0).intValue());
    }

    @Test
    public void testPrepare() {
        String jdbcQuery;
        Map<String, List<Integer>> pMap = new HashMap<>();

        jdbcQuery = NamedPreparedStatement.replaceBindParams(SIMPLE_QUERY, pMap);
        assertEquals(SIMPLE_QUERY_SUBST, jdbcQuery);

        List<Integer> lst = pMap.get("org_id");
        assertNotNull(lst);
        assertEquals(1, lst.size());
        assertEquals(1, lst.get(0).intValue());

        session.doWork(c -> c.prepareStatement(jdbcQuery));
    }

    @Test
    public void testTwoBindPrepare() {
        List<Integer> lst;
        String jdbcQuery;
        Map<String, List<Integer>> pMap = new HashMap<>();

        jdbcQuery = NamedPreparedStatement.replaceBindParams(TWO_VAR_QUERY, pMap);
        assertEquals(TWO_VAR_QUERY_SUBST, jdbcQuery);

        lst = pMap.get("sid");
        assertNotNull(lst);
        assertEquals(LIST_SIZE, lst.size());
        assertEquals(FIRST_POS, lst.get(0).intValue());
        assertEquals(SECOND_POS, lst.get(1).intValue());

        lst = pMap.get("user_id");
        assertNotNull(lst);
        assertEquals(1, lst.size());
        assertEquals(1, lst.get(0).intValue());

        session.doWork(c -> c.prepareStatement(jdbcQuery));
    }

    @Test
    public void testNotFoundBindParam() {
        Map<String, List<Integer>> pMap = new HashMap<>();

        String jdbcQuery = NamedPreparedStatement.replaceBindParams(TWO_VAR_QUERY, pMap);
        assertEquals(TWO_VAR_QUERY_SUBST, jdbcQuery);

        List<Integer> params = pMap.get("sid");
        assertNotNull(params);
        assertEquals(LIST_SIZE, params.size());
        assertEquals(FIRST_POS, params.get(0).intValue());
        assertEquals(SECOND_POS, params.get(1).intValue());

        List<Integer> userIdParams = pMap.get("user_id");
        assertNotNull(userIdParams);
        assertEquals(1, userIdParams.size());
        assertEquals(1, userIdParams.get(0).intValue());

        session.doWork(connection -> {
            PreparedStatement ps = connection.prepareStatement(jdbcQuery);
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("BAD_DATA", "GARBAGE");
            try {
                NamedPreparedStatement.execute(ps, pMap, parameters);
                fail("Should have received BindVariableNotFoundException");
            }
            catch (BindVariableNotFoundException e) {
                // Expected exception
            }
        });
    }
}

/*
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
package com.suse.manager.utils.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.frontend.listview.PageControl;
import com.redhat.rhn.testing.MockObjectTestCase;

import com.suse.manager.utils.PagedSqlQueryBuilder;

import org.cobbler.test.MockConnection;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import javax.persistence.Tuple;
import javax.persistence.TupleElement;

/**
 * Tests for PageSqlQueryBuilder
 */
public class PagedSqlQueryBuilderTest extends MockObjectTestCase {

    private Session sessionMock;

    @BeforeEach
    public void setUp() throws Exception {
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
        MockConnection.clear();

        sessionMock = mock(Session.class);
    }

    private static Stream<Arguments> provideDataForParseFilter() {
        return Stream.of(
                Arguments.of("foo", "col1 ILIKE :filter_value", "%foo%"),
                Arguments.of(">", "col1 ILIKE :filter_value", "%"),
                Arguments.of(">123", "col1 > :filter_value", 123L),
                Arguments.of("!= 123.45", "col1 != :filter_value", 123.45),
                Arguments.of("true", "col1 = CAST(:filter_value AS BOOLEAN)", "true"),
                Arguments.of("False", "col1 = CAST(:filter_value AS BOOLEAN)", "False"),
                Arguments.of("<2022-07-12", "CAST(col1 AS DATE) < CAST(:filter_value AS DATE)",
                        "2022-07-12")
        );
    }

    @ParameterizedTest
    @MethodSource("provideDataForParseFilter")
    public void testParseFilter(String data, String pattern, Object value) {
        PageControl pc = new PageControl();
        pc.setFilter(true);
        pc.setFilterColumn("col1");
        pc.setFilterData(data);

        PagedSqlQueryBuilder.FilterWithValue filter = PagedSqlQueryBuilder.parseFilter(Optional.of(pc));

        assertEquals(String.format(" %s ", pattern), filter.getFilter());
        assertEquals(value, filter.getValue());
    }

    @Test
    public void testRun() {
        PageControl pc = new PageControl(50, 25);
        pc.setSortColumn("name");
        pc.setSortDescending(true);
        pc.setFilter(true);
        pc.setFilterColumn("col1");
        pc.setFilterData("foo");

        Map<String, Object> params = Map.of("value", 123);
        PagedSqlQueryBuilder builder = new PagedSqlQueryBuilder();

        NativeQuery<Tuple> mockQuery = mock(NativeQuery.class, "query");
        NativeQuery<Object> mockCountQuery = mock(NativeQuery.class, "countQuery");

        String expectedSql = "SELECT S.id, O.name FROM SomeTable S, OtherTable O " +
                "WHERE (S.id = O.sid AND O.value = :value) AND  col1 ILIKE :filter_value  " +
                "ORDER BY name DESC";

        String expectedCountSql = "SELECT count(id) FROM SomeTable S, OtherTable O " +
                "WHERE (S.id = O.sid AND O.value = :value) AND  col1 ILIKE :filter_value ";

        context().checking(new Expectations() {{
            oneOf(sessionMock).createNativeQuery(expectedSql, Tuple.class); will(returnValue(mockQuery));
            oneOf(sessionMock).createNativeQuery(expectedCountSql); will(returnValue(mockCountQuery));
            oneOf(mockQuery).setFirstResult(49); will(returnValue(mockQuery));
            oneOf(mockQuery).setMaxResults(25); will(returnValue(mockQuery));
            oneOf(mockQuery).setParameter("value", 123); will(returnValue(mockQuery));
            oneOf(mockQuery).setParameter("filter_value", "%foo%"); will(returnValue(mockQuery));
            oneOf(mockQuery).list(); will(returnValue(List.of(new TestTuple(1L, "one"), new TestTuple(2L, "two"))));

            oneOf(mockCountQuery).setParameter("value", 123); will(returnValue(mockCountQuery));
            oneOf(mockCountQuery).setParameter("filter_value", "%foo%"); will(returnValue(mockCountQuery));
            oneOf(mockCountQuery).uniqueResult(); will(returnValue(Integer.valueOf(53)));
        }});

        DataResult<TestDto> results = builder
                .select("S.id, O.name")
                .from("SomeTable S, OtherTable O")
                .where("S.id = O.sid AND O.value = :value")
                .run(params, pc, TestDto.class, sessionMock);

        assertEquals(2, results.size());
        assertEquals(53, results.getTotalSize());
        assertEquals(new TestDto(1L, "one"), results.get(0));
        assertEquals(new TestDto(2L, "two"), results.get(1));
    }

    private class TestTuple implements Tuple {
        private Map<String, Object> data;

        private TestTuple(Long id, String name) {
            data = Map.of("id", id, "name", name);
        }

        @Override
        public <X> X get(TupleElement<X> tupleElementIn) {
            return null;
        }

        @Override
        public <X> X get(String sIn, Class<X> classIn) {
            return (X)data.get(sIn);
        }

        @Override
        public Object get(String sIn) {
            return data.get(sIn);
        }

        @Override
        public <X> X get(int iIn, Class<X> classIn) {
            return null;
        }

        @Override
        public Object get(int iIn) {
            return null;
        }

        @Override
        public Object[] toArray() {
            return new Object[0];
        }

        @Override
        public List<TupleElement<?>> getElements() {
            return null;
        }
    }
}

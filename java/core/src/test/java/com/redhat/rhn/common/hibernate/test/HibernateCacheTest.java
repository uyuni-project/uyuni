/*
 * Copyright (c) 2026 SUSE LCC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.common.hibernate.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.user.RhnTimeZone;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.testing.MockObjectTestCase;

import org.hibernate.Cache;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

class HibernateCacheTest extends MockObjectTestCase {

    private static List<Integer> timezoneIds = null;
    private Statistics stats;

    @BeforeAll
    static void init() {
        timezoneIds = UserFactory.lookupAllTimeZones().stream().map(RhnTimeZone::getTimeZoneId).toList();
    }

    @BeforeEach
    void setupStats() {
        Session session = HibernateFactory.getSession();
        SessionFactory sessionFactory = session.getSessionFactory();
        // clear l2 cache
        session.getSessionFactory().getCache().evictAll();
        // clear l1 cache
        session.clear();
        stats = sessionFactory.getStatistics();
        stats.setStatisticsEnabled(true);
        stats.clear();
    }

    /**
     * Test Hibernate when looking up timezones using queries.
     * Expects query cache to be used, not second-level cache.
     */
    @Test
    void testHibernateQueryCache() {
        final int iterations = 3;

        for (int i = 0; i < iterations; i++) {
            iterateTimezones(true);
            assertEquals(i + 1, stats.getConnectCount());
            assertEquals(i + 1, stats.getSessionOpenCount());
            assertEquals(i + 1, stats.getSessionOpenCount());
            assertEqualsStats(new HibernateStatsSnapshot(
                    timezoneIds.size(), // queries are executed only on the first iteration
                                        // query cache hits on subsequent iterations
                    (long) timezoneIds.size() * i,
                    timezoneIds.size(), // query cache misses only on the first iteration
                    timezoneIds.size(), // query cache puts right on the first iteration
                    0,                  // no 2nd level cache hits expected, as we're using query cache
                    0,                  // no 2nd level cache misses expected, as we're using query cache
                    timezoneIds.size()  // even if the query is cached, all unique timezone entities get cached as well
            ));
        }
    }

    /**
     * Test Hibernate when looking up timezones using session.find().
     * Expects second-level cache to be used.
     */
    @Test
    void testHibernateEntityCache() {
        final int iterations = 3;

        for (int i = 0; i < iterations; i++) {
            iterateTimezones(false);
            assertEquals(i + 1, stats.getConnectCount());
            assertEquals(i + 1, stats.getSessionOpenCount());
            assertEquals(i + 1, stats.getSessionOpenCount());
            assertEqualsStats(new HibernateStatsSnapshot(
                    // no queries expected, as we're using session.find()
                    0,
                    0,
                    0,
                    0,
                    (long) timezoneIds.size() * i, // 2nd level cache hits on subsequent iterations
                    timezoneIds.size(),            // 2nd level cache misses expected only on the first iteration
                    timezoneIds.size()             // 2nd level cache puts expected on the first iteration
            ));
        }
    }

    /**
     * Test second-level cache eviction.
     */
    @Test
    void testSecondLevelCacheEviction() {
        final int firstId = timezoneIds.get(0);
        Cache cache = HibernateFactory.getSession().getSessionFactory().getCache();
        cache.evictAllRegions();

        try (Session s1 = HibernateFactory.getSession().getSessionFactory().openSession()) {
            s1.beginTransaction();
            s1.find(RhnTimeZone.class, firstId);
            s1.getTransaction().commit();
        }

        assertTrue(cache.containsEntity(RhnTimeZone.class, firstId));
        assertEquals(0, stats.getSecondLevelCacheHitCount());
        assertEquals(1, stats.getSecondLevelCacheMissCount());
        assertEquals(1, stats.getSecondLevelCachePutCount());

        // Evict
        cache.evict(RhnTimeZone.class, firstId);

        // Reload
        try (Session s1 = HibernateFactory.getSession().getSessionFactory().openSession()) {
            s1.beginTransaction();
            s1.find(RhnTimeZone.class, firstId);
            s1.getTransaction().commit();
        }

        //
        assertEquals(0, stats.getSecondLevelCacheHitCount());
        assertEquals(2, stats.getSecondLevelCacheMissCount());
        assertEquals(2, stats.getSecondLevelCachePutCount());
    }

    /**
     * Iterate over all timezones, looking them up either via query or session.find().
     *
     * @param useQueryLookup if true, use UserFactory.getTimeZone() (query);
     *                       if false, use session.find() (2nd level cache)
     */
    private void iterateTimezones(boolean useQueryLookup) {
        try (Session s1 = HibernateFactory.getSession().getSessionFactory().openSession()) {
            s1.beginTransaction();
            for (Integer tzid : timezoneIds) {
                if (useQueryLookup) {
                    UserFactory.getTimeZone(tzid);
                }
                else {
                    s1.find(RhnTimeZone.class, tzid);
                }
            }
            s1.getTransaction().commit();
        }
    }

    /**
     * Helper method to assert that the current Hibernate statistics match the expected values
     *
     * @param snapshot the expected values to compare against
     */
    void assertEqualsStats(HibernateStatsSnapshot snapshot) {
        assertEquals(snapshot.getQueryExecutionCount(), stats.getQueryExecutionCount(),
                "Unexpected number of query executions");
        assertEquals(snapshot.getQueryCacheHitCount(), stats.getQueryCacheHitCount(),
                "Unexpected number of query cache hits");
        assertEquals(snapshot.getQueryCacheMissCount(), stats.getQueryCacheMissCount(),
                "Unexpected number of query cache misses");
        assertEquals(snapshot.getQueryCachePutCount(), stats.getQueryCachePutCount(),
                "Unexpected number of query cache puts");
        assertEquals(snapshot.getSecondLevelCacheHitCount(), stats.getSecondLevelCacheHitCount(),
                "Unexpected number of 2nd level cache hits");
        assertEquals(snapshot.getSecondLevelCacheMissCount(), stats.getSecondLevelCacheMissCount(),
                "Unexpected number of 2nd level cache misses");
        assertEquals(snapshot.getSecondLevelCachePutCount(), stats.getSecondLevelCachePutCount(),
                "Unexpected number of 2nd level cache puts");
    }

    class HibernateStatsSnapshot {
        private long queryExecutionCount = 0;
        private long queryCacheHitCount = 0;
        private long queryCacheMissCount = 0;
        private long queryCachePutCount = 0;
        private long secondLevelCacheHitCount = 0;
        private long secondLevelCacheMissCount = 0;
        private long secondLevelCachePutCount = 0;

        HibernateStatsSnapshot(
                long queryExecutionCountIn,
                long queryCacheHitCountIn,
                long queryCacheMissCountIn,
                long queryCachePutCountIn,
                long secondLevelCacheHitCountIn,
                long secondLevelCacheMissCountIn,
                long secondLevelCachePutCountIn
        ) {
            queryExecutionCount = queryExecutionCountIn;
            queryCacheHitCount = queryCacheHitCountIn;
            queryCacheMissCount = queryCacheMissCountIn;
            queryCachePutCount = queryCachePutCountIn;
            secondLevelCacheHitCount = secondLevelCacheHitCountIn;
            secondLevelCacheMissCount = secondLevelCacheMissCountIn;
            secondLevelCachePutCount = secondLevelCachePutCountIn;
        }

        public long getQueryExecutionCount() {
            return queryExecutionCount;
        }

        public long getQueryCacheHitCount() {
            return queryCacheHitCount;
        }

        public long getQueryCacheMissCount() {
            return queryCacheMissCount;
        }

        public long getQueryCachePutCount() {
            return queryCachePutCount;
        }

        public long getSecondLevelCacheHitCount() {
            return secondLevelCacheHitCount;
        }

        public long getSecondLevelCacheMissCount() {
            return secondLevelCacheMissCount;
        }

        public long getSecondLevelCachePutCount() {
            return secondLevelCachePutCount;
        }

    }
}

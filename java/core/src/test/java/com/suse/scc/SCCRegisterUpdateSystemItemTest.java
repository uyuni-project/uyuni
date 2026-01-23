/*
 * Copyright (c) 2025 SUSE LLC
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

package com.suse.scc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.suse.scc.model.SCCRegisterSystemItem;
import com.suse.scc.model.SCCUpdateSystemItem;
import com.suse.utils.Json;

import com.google.gson.reflect.TypeToken;

import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

/**
 * Tests for {@link SCCRegisterSystemItem} and {@link SCCUpdateSystemItem}.
 */
public class SCCRegisterUpdateSystemItemTest {

    private static final String LAST_SEEN_REQUEST_STRING = """
            {
               "systems":[
                  {
                     "login":"52156f60-8aa2-4165-8645-367efdc2a510-1000010265",
                     "password":"wuTmFh2RKUTQAAA013OCbzeluSteRXXclTUKFTN0lMjQ5ZANSMEtgynM2RAoxk3l",
                     "last_seen_at":"2025-01-21T01:00:00.000+01"
                  },
                  {
                     "login":"52156f60-8aa2-4165-8645-367efdc2a510-1000010266",
                     "password":"vMAZGApR4Xx6UD3uAVR5DuuOoK7fuzYR1TCCAeNTH6X7WWtQwddT8iUFGgXpsh2d",
                     "last_seen_at":"2025-01-22T01:00:00.000+01"
                  },
                  {
                     "login":"52156f60-8aa2-4165-8645-367efdc2a510-1000010267",
                     "password":"cwvK5YuRgQaUbipMRz5xEYq0KLpHtcmvxjIq4WX3EmR4oKao2w6FzZl46omCAAgL",
                     "last_seen_at":"2025-01-23T01:00:00.000+01"
                  }
               ]
            }
            """;

    private static final String CREATE_REQUEST_STRING = """
            {
                "systems":[
                   {
                      "login":"52156f60-8aa2-4165-8645-367efdc2a510-1000010002",
                      "password":"0deFZmn14ZzX6N4jYQ5ZQ6nMGAgYsMGafU8vceShmb32uWVWDKGErALICO2ui2rV",
                      "hwinfo":{
                         "cpus":0,
                         "sockets":0,
                         "mem_total":1024,
                         "arch":"i386",
                         "sap":[]
                      },
                      "products":[],
                      "regcodes":[],
                      "last_seen_at":"2025-03-01T01:00:00.000+01"
                   },
                   {
                      "login":"52156f60-8aa2-4165-8645-367efdc2a510-1000010013",
                      "password":"rNCp2TvRELSIZFmPJH9595x8ehFBjtb4wnq8jiPjzvNysRwbJEFfcfZCVqGhRDCc",
                      "hwinfo":{
                         "cpus":0,
                         "sockets":0,
                         "mem_total":1024,
                         "arch":"i386",
                         "sap":[]
                      },
                      "products":[],
                      "regcodes":[],
                      "last_seen_at":"2025-03-01T01:00:00.000+01"
                   },
                   {
                      "login":"52156f60-8aa2-4165-8645-367efdc2a510-1000010003",
                      "password":"trHD0ytf5eVCoDxUYVhKs05EpjKWPtjjUkWu6P6eJXf6EhX6O8wIPn8vMQvGOWSF",
                      "hwinfo":{
                         "cpus":0,
                         "sockets":0,
                         "mem_total":1024,
                         "arch":"i386",
                         "sap":[]
                      },
                      "products":[],
                      "regcodes":[],
                      "last_seen_at":"2025-03-01T01:00:00.000+01"
                   }
                ]
             }
            """;

    private final TypeToken<Map<String, List<SCCRegisterSystemItem>>> registerSystemTypeToken = new TypeToken<>() { };
    private final TypeToken<Map<String, List<SCCUpdateSystemItem>>> updateSystemTypeToken = new TypeToken<>() { };
    private static final String SYSTEMS_KEY = "systems";
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

    @Test
    public void ensureRegularCreateRequestParsingWorks() {
        Map<String, List<SCCRegisterSystemItem>> createRequestToRegisterSystem =
                Json.GSON.fromJson(CREATE_REQUEST_STRING, registerSystemTypeToken.getType());

        assertTrue(createRequestToRegisterSystem.containsKey(SYSTEMS_KEY));
        assertEquals(3, createRequestToRegisterSystem.get(SYSTEMS_KEY).size());
        for (SCCRegisterSystemItem jsonItem : createRequestToRegisterSystem.get(SYSTEMS_KEY)) {
            assertFalse(jsonItem.isOnlyLastSeenAt());
            assertEquals("2025-03-01", dateFormat.format(jsonItem.getLastSeenAt()));
        }
    }

    @Test
    public void ensureRegularLastSeenRequestParsingWorks() {
        Map<String, List<SCCUpdateSystemItem>> lastSeenRequestToUpdateSystem =
                Json.GSON.fromJson(LAST_SEEN_REQUEST_STRING, updateSystemTypeToken.getType());

        assertTrue(lastSeenRequestToUpdateSystem.containsKey(SYSTEMS_KEY));
        assertEquals(3, lastSeenRequestToUpdateSystem.get(SYSTEMS_KEY).size());
    }

    @Test
    public void acknowledgeInvertedCreateRequestParsingIsNotFailing() {
        Map<String, List<SCCUpdateSystemItem>> createRequestToUpdateSystem =
                Json.GSON.fromJson(CREATE_REQUEST_STRING, updateSystemTypeToken.getType());

        assertTrue(createRequestToUpdateSystem.containsKey(SYSTEMS_KEY));
        assertEquals(3, createRequestToUpdateSystem.get(SYSTEMS_KEY).size());
    }

    @Test
    public void acknowledgeInvertedLastSeenRequestParsingIsNotFailing() {
        Map<String, List<SCCRegisterSystemItem>> lastSeenRequestRegisterSystem =
                Json.GSON.fromJson(LAST_SEEN_REQUEST_STRING, registerSystemTypeToken.getType());

        assertTrue(lastSeenRequestRegisterSystem.containsKey(SYSTEMS_KEY));
        assertEquals(3, lastSeenRequestRegisterSystem.get(SYSTEMS_KEY).size());
        for (SCCRegisterSystemItem jsonItem : lastSeenRequestRegisterSystem.get(SYSTEMS_KEY)) {
            assertTrue(jsonItem.isOnlyLastSeenAt());
            assertNotEquals("2025-03-01", dateFormat.format(jsonItem.getLastSeenAt()));
        }
    }
}

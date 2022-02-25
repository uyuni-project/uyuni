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
package com.suse.manager.api.test;

import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * API handler with multiple methods for unit testing
 */
public class TestHandler extends BaseHandler {
    /**
     * Gets the ID of the logged-in user
     * @param user the logged-in user
     * @return the user ID
     */
    public Long withUser(User user) {
        return user.getId();
    }

    /**
     * Gets the values of input parameters in a map to check if they are parsed correctly
     * @param myInteger the integer parameter
     * @param myString the string parameter
     * @param myBoolean the boolean parameter
     * @return the map of the input parameters
     */
    public Map<String, Object> basicTypes(Integer myInteger, String myString, Boolean myBoolean) {
        return Map.of(
                "myInteger", myInteger,
                "myString", StringUtils.defaultString(myString, "-empty-"),
                "myBoolean", myBoolean);
    }

    /**
     * Echoes the date in the input
     * @param myDate the date parameter
     * @return the date
     */
    public Date basicDate(Date myDate) {
        return myDate;
    }

    /**
     * Sorts a list of integers into an array
     * @param myList the list of integers
     * @return the sorted array of integers
     */
    public Object[] sortIntegerList(List<Integer> myList) {
        return myList.stream().sorted().toArray();
    }

    /**
     * Sorts a list of longs
     * @param myList the list of longs
     * @return the sorted list of longs
     */
    public List<Long> sortLongList(List<Long> myList) {
        return myList.stream().sorted().collect(Collectors.toList());
    }

    /**
     * Sorts a list of strings
     * @param myList the list of strings
     * @return the sorted list of strings
     */
    public List<String> sortStringList(List<String> myList) {
        return myList.stream().sorted().collect(Collectors.toList());
    }

    /**
     * Outputs the number of parameters that this method accepts
     * @param myInteger1 the input parameter
     * @return 1, the number of parameters that the method accepts
     */
    public int overloadedEndpoint(Integer myInteger1) {
        // Report parameter count
        return 1;
    }

    /**
     * Outputs the number of parameters that this method accepts
     * @param myInteger1 the first input parameter
     * @param myInteger2 the second input parameter
     * @return 2, the number of parameters that the method accepts
     */
    public int overloadedEndpoint(Integer myInteger1, Integer myInteger2) {
        // Report parameter count
        return 2;
    }

    /**
     * Returns the keys of the input map in a set
     * @param myMap the map
     * @param numKeys number of keys for validation
     * @return the set of map keys
     */
    public Set<String> mapKeysToSet(Map<String, Object> myMap, Integer numKeys) {
        if (numKeys != myMap.size()) {
            throw new IllegalArgumentException("Key count doesn't match.");
        }
        return myMap.keySet();
    }

    /**
     * Collects all the values in a list of multiple maps into a single list
     * @param myList the list of maps
     * @return the list of all values
     */
    public List<Object> listOfMaps(List<Map<String, Object>> myList) {
        return myList.stream().flatMap(m -> m.values().stream()).collect(Collectors.toList());
    }

    /**
     * Returns a {@link TestResponse} object initialized with the input parameters
     * @param myInteger the input parameter
     * @param myString the input parameter
     * @return the custom response object
     */
    public TestResponse customResponse(Integer myInteger, String myString) {
        return new TestResponse(myInteger, myString);
    }

    /**
     * Returns a {@link TestResponse} object initialized with the input parameters
     * @param myInteger the input parameter
     * @param myString the input parameter
     * @return the custom response object
     */
    public TestResponse customResponseSubclass(Integer myInteger, String myString) {
        return new TestResponseSubclass(myInteger, myString);
    }

    /**
     * Returns a {@link TestComplexResponse} object initialized with the input parameters
     * @param myString the value to be passed to the {@link TestComplexResponse} object's constructor
     * @param nestedObjProps the map that contains the properties of the nested {@link TestResponse} object
     * @return the complex response object
     */
    public TestComplexResponse complexResponse(String myString, Map<String, Object> nestedObjProps) {
        return new TestComplexResponse(myString, new TestResponse(((Long) nestedObjProps.get("myInteger")).intValue(),
                (String) nestedObjProps.get("myString")));
    }

    /**
     * Returns a {@link List} of {@link TestResponse} objects initialized with the input parameters
     * @param myString1 the input parameter to put in the first element
     * @param myString2 the input parameter to put in the second element
     * @return the list of custom response objects
     */
    @ReadOnly
    public List<TestResponse> customResponseList(String myString1, String myString2) {
        return List.of(
                new TestResponse(1, myString1),
                new TestResponse(2, myString2));
    }

    /**
     * Returns a {@link Map} of {@link TestResponse} objects initialized with the input parameters
     * @param myString1 the input parameter to put in the first element
     * @param myString2 the input parameter to put in the second element
     * @return the map of custom response objects
     */
    @ReadOnly
    public Map<Long, TestResponse> customResponseMap(String myString1, String myString2) {
        return Map.of(
                1L, new TestResponse(1, myString1),
                2L, new TestResponse(2, myString2));
    }

    /**
     * Fails unconditionally with a {@link TestApiException}
     * @return nothing
     */
    public int failing() {
        throw new TestApiException();
    }

    /**
     * Class representing an unregistered return type
     */
    public static class WeirdReturnType { }

    /**
     * Returns an object of a type that is unregistered for serialization in the APIs
     * @return the object of an unregistered type
     */
    public Object invalidReturnType() {
        return new WeirdReturnType();
    }
}

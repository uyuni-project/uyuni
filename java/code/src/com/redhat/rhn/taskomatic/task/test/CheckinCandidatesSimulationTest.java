/**
 * Copyright (c) 2013 SUSE LLC
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
package com.redhat.rhn.taskomatic.task.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import com.redhat.rhn.taskomatic.task.checkin.SystemCheckinUtils;

/**
 * This test implements a simulation considering n idle systems (specified below as
 * numberOfSystems), i.e. no actions are being scheduled. This test should verify that:
 *
 * 1. All systems will check in between hour 12 and hour 24, it should be unknown when
 *    it will happen exactly though.
 * 2. Not all systems will be contacted at about the same time. The distribution of
 *    check in timestamps should rather cover all categories (hours).
 * 3. At maximum 22% of all systems will check in during one hour (see maxPercentage).
 */
public class CheckinCandidatesSimulationTest extends TestCase {

    // This is a value from rhn.conf
    private int thresholdDays = 1;

    // Calculations are done in seconds
    private int thresholdMax = thresholdDays * 86400;
    private int thresholdMin = Math.round(thresholdMax / 2);
    private double thresholdMean = thresholdMax;
    private double thresholdStddev = thresholdMax / 6;

    // Number of systems for the simulation
    private int numberOfSystems = 100000;

    // How often we check for systems to checkin (minutes)
    private int checkInterval = SystemCheckinUtils.CHECK_INTERVAL;

    // Counters
    private int randomsTotal = 0;
    private int randomsInvalid = 0;

    // Maximal percentage of systems checking in during one hour
    private int maxPercentage = 22;

    /**
     * Run a simulation and assert certain constraints.
     */
    public void testCheckinSimulation() {
        // Data structure to hold the results
        HashMap<Integer, List<Integer>> results = new HashMap<Integer, List<Integer>>();

        // Run the simulation for every system
        for (int s = 1; s <= numberOfSystems; s++) {
            // Start in hour 12 and get random thresholds until we checkin
            int hour = 12;
            boolean checkedIn = false;

            while (hour <= 24) {
                int randomThreshold;
                for (int minutes = 0; minutes < 60; minutes += checkInterval) {
                    randomThreshold = getRandomThreshold(
                            thresholdMean, thresholdStddev, thresholdMin, thresholdMax);
                    if (getSeconds(hour, minutes) > randomThreshold) {
                        // System checked in
                        addResult(results, hour, minutes);
                        checkedIn = true;
                        break;
                    }
                }
                if (checkedIn) {
                    break;
                }
                else {
                    hour++;
                }
            }
        }

        // Print some stuff
        printSimulationProperties();
        printRandomizerProperties();
        printRandomizerStatistics();
        printResults(results);

        // Assertions on the results
        assertAllSystemsCheckedIn(results);
        assertKeySet(results);
        assertMaxPercentage(results, maxPercentage);
    }

    /**
     * Reimplement {@link SystemCheckinUtils#getRandomThreshold()} here for counting
     * the total number of randoms generated.
     */
    private int getRandomThreshold(double mean, double stddev, long min, long max) {
        int val = 0;
        while (val < min || val > max) {
            if (val != 0) {
                randomsInvalid++;
            }
            val = SystemCheckinUtils.getRandomGaussian(mean, stddev);
            randomsTotal++;
        }
        return val;
    }

    /**
     * Add a result (given by hour and minutes) to the results {@link HashMap}.
     * @param results
     * @param hour
     * @param minutes
     */
    private void addResult(HashMap<Integer, List<Integer>> results, Integer hour,
            Integer minutes) {
        List<Integer> list = results.get(hour);
        if (list == null) {
            list = new ArrayList<Integer>();
            results.put(hour, list);
        }
        list.add(minutes);
    }

    /**
     * Assert that all systems checked in.
     */
    private void assertAllSystemsCheckedIn(HashMap<Integer, List<Integer>> results) {
        int actual = 0;
        for (int h = 12; h <= 24; h++) {
            List<Integer> l = results.get(h);
            if (l != null) {
                actual += l.size();
            }
        }
        assertEquals(numberOfSystems, actual);
    }

    /**
     * Assert elements of the results key set.
     */
    private void assertKeySet(HashMap<Integer, List<Integer>> results) {
        Integer[] hours = {12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23};
        Set<Integer> keys = results.keySet();
        assertTrue(keys.containsAll(Arrays.asList(hours)));
    }

    /**
     * Assert all percentages are below max.
     */
    private void assertMaxPercentage(HashMap<Integer, List<Integer>> results, int max) {
        // Get max percentage of all categories numbers of systems checked in
        long maxPercent = 0;
        for (int h = 12; h <= 24; h++) {
            List<Integer> l = results.get(h);
            if (l != null) {
                long percent = Math.round(getPercentage(l.size(), numberOfSystems));
                if (percent > maxPercent) {
                    maxPercent = percent;
                }
            }
        }
        System.out.println("Max percentage = " + maxPercent);
        assertTrue(maxPercent <= max);
    }

    /**
     * Print out simulation properties.
     */
    private void printSimulationProperties() {
        System.out.println("\n--> Simulation properties:");
        System.out.println("Number of systems: " + numberOfSystems);
        System.out.println("Check interval: " + checkInterval + " minutes");
    }

    /**
     * Print out randomizer properties.
     */
    private void printRandomizerProperties() {
        System.out.println("\n--> Randomizer properties:");
        System.out.println("mean = " + thresholdMean + " seconds (" +
                SystemCheckinUtils.toHours(Math.round(thresholdMean)) + " hours)");
        System.out.println("stddev = " + thresholdStddev + " seconds (" +
                SystemCheckinUtils.toHours(Math.round(thresholdStddev)) + " hours)");
        System.out.println("min = " + thresholdMin + " seconds (" +
                SystemCheckinUtils.toHours(thresholdMin) + " hours)");
        System.out.println("max = " + thresholdMax + " seconds (" +
                SystemCheckinUtils.toHours(thresholdMax) + " hours)");
    }

    /**
     * Print out randomizer statistics.
     */
    private void printRandomizerStatistics() {
        System.out.println("\n--> Randomizer statistics:");
        long thrownAway = Math.round(getPercentage(randomsInvalid, randomsTotal));
        System.out.println("randoms thrown away: " + thrownAway + "% (" + randomsInvalid +
                " of " + randomsTotal + " in total)");
    }

    /**
     * Print out percentages of systems that checked in during each hour.
     */
    private void printResults(HashMap<Integer, List<Integer>> results) {
        System.out.println("\n--> Hour statistics: ");
        System.out.println("Key set: " + results.keySet().toString());
        for (int h = 12; h <= 24; h++) {
            List<Integer> l = results.get(h);
            if (l == null) {
                System.out.println("Hour " + h + ": 0");
            }
            else {
                System.out.println("Hour " + h + ": " +
                        Math.round(getPercentage(l.size(), numberOfSystems)) +
                        "% (" + l.size() + ")");
            }
        }
    }

    /**
     * Convert a  timstamp given as hour and minutes to seconds.
     */
    private int getSeconds(int hour, int minutes) {
        return hour * 60 * 60 + minutes * 60;
    }

    /**
     * Calculate percentage value.
     */
    private double getPercentage(long number, long total) {
        return ((double) number / (double) total) * 100;
    }
}

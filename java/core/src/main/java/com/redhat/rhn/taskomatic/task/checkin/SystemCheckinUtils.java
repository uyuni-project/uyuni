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
package com.redhat.rhn.taskomatic.task.checkin;

import java.util.Random;

/**
 * Utils for {@link CheckinCandidatesResolver}.
 */
public class SystemCheckinUtils {

    // How often we check for systems to checkin (minutes)
    public static final int CHECK_INTERVAL = 20;

    // Random number generator
    private static Random rand = new Random();

    /**
     * Private constructor.
     */
    private SystemCheckinUtils() {
    }

    /**
     * Return a random integer from the interval specified by min and max.
     *
     * @param min the interval min
     * @param max the interval max
     * @return random integer from interval
     */
    public static int nextRandom(int min, int max) {
        return rand.nextInt(max - min + 1) + min;
    }

    /**
     * Get a random number from a normal distribution using the given mean and stddev.
     *
     * @param mean mean for nextGaussian()
     * @param stddev stddev for nextGaussian()
     * @return random gaussian
     */
    public static int getRandomGaussian(double mean, double stddev) {
        double val = 0;
        val = rand.nextGaussian() * stddev + mean;
        return (int) Math.round(val);
    }

    /**
     * Return a random integer from a normal distribution using given values for mean and
     * stddev (should be given in seconds). Numbers are produced until there is a result
     * that is in [min,max].
     *
     * @param mean mean
     * @param stddev standard deviation
     * @param min min threshold
     * @param max max threshold
     * @return random threshold value
     */
    public static int getRandomThreshold(double mean, double stddev, long min, long max) {
        int val = 0;
        while (val < min || val > max) {
            val = SystemCheckinUtils.getRandomGaussian(mean, stddev);
        }
        return val;
    }

    /**
     * Convert timestamps given in seconds (as long) into hours.
     *
     * @param seconds number of seconds to convert into hours
     * @return given seconds converted to hours
     */
    public static double toHours(long seconds) {
        return seconds / 60.0 / 60.0;
    }
}

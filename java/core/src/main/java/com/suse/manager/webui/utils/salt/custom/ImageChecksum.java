/*
 * Copyright (c) 2018 SUSE LLC
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
package com.suse.manager.webui.utils.salt.custom;

/**
 * ImageChecksum
 * Class for images (Containers and OS images)
 */
public class ImageChecksum {
    /**
     * Checksum
     */
    public interface Checksum {
        /**
         * @return the checksum
         */
        String getChecksum();
    }

    /**
     * MD5Checksum
     */
    public static class MD5Checksum implements Checksum {
        private String checksum;

        /**
         * Constructor for MD5 checksum
         * @param checksumIn checksum
         */
        public MD5Checksum(String checksumIn) {
            checksum = checksumIn;
        }

        @Override
        public String getChecksum() {
            return checksum;
        }

        /**
         * @return checksum with type
         */
        @Override
        public String toString() {
            return "md5:" + checksum;
        }
    }

    /**
     * SHA1Checksum
     */
    public static class SHA1Checksum implements Checksum {

        private String checksum;

        /**
         * Constructor
         * @param checksumIn checksum
         */
        public SHA1Checksum(String checksumIn) {
            checksum = checksumIn;
        }

        @Override
        public String getChecksum() {
            return checksum;
        }

        /**
         * @return checksum with type
         */
        @Override
        public String toString() {
            return "sha1:" + checksum;
        }
    }

    /**
     * SHA256Checksum
     */
    public static class SHA256Checksum implements Checksum {

        private String checksum;

        /**
         * Constructor
         * @param checksumIn checksum
         */
        public SHA256Checksum(String checksumIn) {
            checksum = checksumIn;
        }

        @Override
        public String getChecksum() {
            return checksum;
        }

        /**
         * @return checksum with type
         */
        @Override
        public String toString() {
            return "sha256:" + checksum;
        }
    }

    /**
     * SHA384Checksum
     */
    public static class SHA384Checksum implements Checksum {

        private String checksum;

        /**
         * Constructor
         * @param checksumIn checksum
         */
        public SHA384Checksum(String checksumIn) {
            checksum = checksumIn;
        }

        @Override
        public String getChecksum() {
            return checksum;
        }

        /**
         * @return checksum with type
         */
        @Override
        public String toString() {
            return "sha384:" + checksum;
        }
    }

    /**
     * SHA512Checksum
     */
    public static class SHA512Checksum implements Checksum {

        private String checksum;

        /**
         * Constructor
         * @param checksumIn checksum
         */
        public SHA512Checksum(String checksumIn) {
            checksum = checksumIn;
        }

        @Override
        public String getChecksum() {
            return checksum;
        }

        /**
         * @return checksum with type
         */
        @Override
        public String toString() {
            return "sha512:" + checksum;
        }
    }
}
